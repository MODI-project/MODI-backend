package kuit.modi.service;

import jakarta.transaction.Transactional;
import kuit.modi.domain.*;
import kuit.modi.dto.diary.request.CreateDiaryRequest;
import kuit.modi.dto.diary.request.UpdateDiaryRequest;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;
import kuit.modi.exception.S3ExceptionResponseStatus;
import kuit.modi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final ToneRepository toneRepository;
    private final LocationRepository locationRepository;
    private final FrameRepository frameRepository;
    private final TagRepository tagRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createDiary(Member member, CreateDiaryRequest request, MultipartFile imageFile) {
        LocalDateTime parsedDate = LocalDateTime.parse(request.date());
        LocalDateTime now = LocalDateTime.now();

        Emotion emotion = emotionRepository.findByName(request.emotion())
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.INVALID_EMOTION));

        Tone tone = toneRepository.findByName(request.tone()) // 톤 정보 없을 시 생성
                .orElseGet(() -> {
                    Tone newTone = Tone.create(request.tone());
                    return toneRepository.save(newTone);
                });

        Frame frame = frameRepository.findByName(request.frame())
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.INVALID_FRAME));

        Location location = locationRepository.findByAddress(request.address()) // 위치 정보 없을 시 생성
                .orElseGet(() -> {
                    Location newLocation = Location.create(request.address(), request.latitude(), request.longitude());
                    return locationRepository.save(newLocation);
                });

        Diary diary = Diary.create(
                request.content(), request.summary(), null,
                parsedDate, member, emotion, tone, location,
                now, now
        );

        // Style 생성 (Frame은 엔티티로 전달)
        diary.setStyle(Style.create(request.font(), diary, frame));

        if (imageFile != null && !imageFile.isEmpty()) {
            S3Service.UploadResult uploaded = s3Service.uploadFile(imageFile);
            // DB에는 url 대신 key만 저장
            diary.setImage(Image.create(uploaded.key(), diary));
        }

        List<Tag> tags = request.tags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.create(tagName))))
                .toList();

        diary.getDiaryTags().addAll(tags.stream()
                .map(tag -> DiaryTag.create(diary, tag))
                .toList());

        return diaryRepository.save(diary).getId();
    }

    @Transactional
    public void updateDiary(Long diaryId, UpdateDiaryRequest request, MultipartFile imageFile) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.DIARY_NOT_FOUND));

        diary.setContent(request.content());
        diary.setSummary(request.summary());
        diary.setSummary_toned(null);
        diary.setDate(LocalDateTime.parse(request.date()));
        diary.setUpdatedAt(LocalDateTime.now());

        Emotion emotion = emotionRepository.findByName(request.emotion())
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.INVALID_EMOTION));
        diary.setEmotion(emotion);

        Tone tone = toneRepository.findByName(request.tone()) // 톤 정보 없을 시 생성
                .orElseGet(() -> {
                    Tone newTone = Tone.create(request.tone());
                    return toneRepository.save(newTone);
                });
        diary.setTone(tone);

        Location location = locationRepository.findByAddressAndLatitudeAndLongitude( // 위치 정보 없을 시 생성
                        request.address(), request.latitude(), request.longitude())
                .orElseGet(() -> locationRepository.save(Location.create(request.address(), request.latitude(), request.longitude())));
        diary.setLocation(location);

        // 기존 DiaryTag 제거
        List<DiaryTag> toRemove = new ArrayList<>(diary.getDiaryTags());
        diary.getDiaryTags().clear(); // 연관 관계 끊기
        diaryTagRepository.deleteAllInBatch(toRemove); // DB에서 삭제

        // 새로운 Tag 확보
        List<Tag> tags = request.tags().stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.create(name))))
                .toList();

        // DiaryTag 다시 설정
        List<DiaryTag> newDiaryTags = tags.stream()
                .map(tag -> DiaryTag.create(diary, tag))
                .toList();

        diary.getDiaryTags().addAll(newDiaryTags);

        //이미지 파일 수정
        if (imageFile != null && !imageFile.isEmpty()) {
            S3Service.UploadResult uploaded = s3Service.uploadFile(imageFile); //S3에 파일 업로드

            if (diary.getImage() != null) { // diary에 등록된 기존 이미지가 있을 경우
                String oldKey = diary.getImage().getUrl(); // 필드명은 url이지만 내용은 key임
                s3Service.deleteByKey(oldKey);
                // 기존 이미지 삭제 후 새 이미지로 교체
                diary.getImage().setUrl(uploaded.key());
            } else { // 기존 이미지가 없었을 경우 객체 새로 생성하여 등록
                Image newImage = Image.create(uploaded.key(), diary);
                diary.setImage(newImage);
            }
        }

        if(request.frame() == null){
            throw new CustomException(DiaryExceptionResponseStatus.MISSING_FRAME);
        } else {
            Frame frame = frameRepository.findById(request.frame())
                    .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.INVALID_FRAME));
            Style style = diary.getStyle();
            style.setFont(request.font());
            style.setFrame(frame);
        }
    }

    @Transactional
    public void updateFavorite(Long diaryId, boolean favorite) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.DIARY_NOT_FOUND));
        diary.setFavorite(favorite);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        log.info("deleteDiary start id={}", diaryId);

        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> {
            log.warn("diary not found id={}", diaryId);
            return new CustomException(DiaryExceptionResponseStatus.DIARY_NOT_FOUND);
        });

        String imageUrl = (diary.getImage() != null) ? diary.getImage().getUrl() : null;
        if (imageUrl != null) {
            log.info("S3 delete try url={}", imageUrl);
            try {
                s3Service.deleteFileFromUrl(imageUrl);
                log.info("S3 delete ok");
            } catch (Exception e) {
                throw new CustomException(S3ExceptionResponseStatus.S3_DELETE_FAILED);
            }
        } else {
            log.debug("no image attached, skip S3 delete");
        }

        diaryRepository.delete(diary);
    }
}