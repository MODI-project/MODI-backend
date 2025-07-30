package kuit.modi.service;

import jakarta.transaction.Transactional;
import kuit.modi.domain.*;
import kuit.modi.dto.diary.request.CreateDiaryRequest;
import kuit.modi.dto.diary.request.UpdateDiaryRequest;
import kuit.modi.exception.NotFoundException;
import kuit.modi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final ToneRepository toneRepository;
    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;
    private final FrameRepository frameRepository;
    private final TagRepository tagRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    @Transactional
    public void createDiary(Member member, CreateDiaryRequest request, MultipartFile imageFile) {
        LocalDateTime parsedDate = LocalDateTime.parse(request.date());
        LocalDateTime now = LocalDateTime.now();

        Emotion emotion = emotionRepository.findByName(request.emotion())
                .orElseThrow(() -> new IllegalArgumentException("감정 정보가 유효하지 않습니다."));

        Tone tone = toneRepository.findByName(request.tone()) // 톤 정보 없을 시 생성
                .orElseGet(() -> {
                    Tone newTone = Tone.create(request.tone());
                    return toneRepository.save(newTone);
                });

        Frame frame = frameRepository.findByName(request.frame())
                .orElseThrow(() -> new IllegalArgumentException("프레임 정보가 유효하지 않습니다."));

        Location location = locationRepository.findByAddress(request.address()) // 위치 정보 없을 시 생성
                .orElseGet(() -> {
                    Location newLocation = Location.create(request.address(), request.latitude(), request.longitude());
                    return locationRepository.save(newLocation);
                });

        Diary diary = Diary.create(
                request.content(),
                request.summary(),
                null,
                parsedDate,
                member,
                emotion,
                tone,
                location,
                now,
                now
        );

        // Style 생성 (Frame은 엔티티로 전달)
        Style style = Style.create(request.font(), diary, frame);
        diary.setStyle(style);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = s3Service.uploadFile(imageFile);
            Image image = Image.create(imageUrl, diary); // S3 URL 저장
            diary.setImage(image);
        }

        List<Tag> tags = request.tags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.create(tagName)))
                )
                .toList();

        List<DiaryTag> diaryTags = tags.stream()
                .map(tag -> DiaryTag.create(diary, tag))
                .toList();

        diary.getDiaryTags().addAll(diaryTags);
        diaryRepository.save(diary);
    }

    @Transactional
    public void updateDiary(Long diaryId, UpdateDiaryRequest request, MultipartFile imageFile) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("기록을 찾을 수 없습니다."));

        diary.setContent(request.content());
        diary.setSummary(request.summary());
        diary.setSummary_toned(null); // Todo 언어스타일 적용된 요약본은 지금은 null로 넣어놨어요!
        diary.setDate(LocalDateTime.parse(request.date()));
        diary.setUpdatedAt(LocalDateTime.now());

        diary.setEmotion(emotionRepository.findByName(request.emotion())
                .orElseThrow(() -> new IllegalArgumentException("감정 정보가 유효하지 않습니다.")));
        diary.setTone(toneRepository.findByName(request.tone())
                .orElseThrow(() -> new IllegalArgumentException("톤 정보가 유효하지 않습니다.")));

        Location location = locationRepository
                .findByAddressAndLatitudeAndLongitude(request.address(), request.latitude(), request.longitude())
                .orElseGet(() -> {
                    Location newLocation = Location.create(request.address(), request.latitude(), request.longitude());
                    return locationRepository.save(newLocation);
                });
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

        if (imageFile != null && !imageFile.isEmpty()) {
            String newUrl = s3Service.uploadFile(imageFile); // S3에 새 이미지 등록

            if (diary.getImage() != null) {
                String oldUrl = diary.getImage().getUrl();  // 기존 URL 먼저 보관
                s3Service.deleteFileFromUrl(oldUrl);        // S3에서 기존 이미지 삭제
                diary.getImage().setUrl(newUrl);            // 기존 image의 url 필드만 변경
            } else {
                Image newImage = Image.create(newUrl, diary);
                diary.setImage(newImage);
            }
        }
        
        if (request.frameId() != null) {
            Frame frame = frameRepository.findById(request.frameId())
                    .orElseThrow(() -> new IllegalArgumentException("프레임 정보가 유효하지 않습니다."));
            Style style = Style.create(request.font(), diary, frame);
            diary.setStyle(style);
        } else {
            diary.setStyle(null);
        }
    }

    @Transactional
    public void updateFavorite(Long diaryId, boolean favorite) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("해당 일기를 찾을 수 없습니다."));

        diary.setFavorite(favorite);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new NotFoundException("해당 기록을 찾을 수 없습니다."));

        // S3에서 이미지 먼저 삭제
        if (diary.getImage() != null) {
            String url = diary.getImage().getUrl();
            s3Service.deleteFileFromUrl(url);
        }

        // 연관된 Image 엔티티는 cascade로 DB에서 자동 삭제됨
        diaryRepository.delete(diary);
    }

}