package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.CreateDiaryRequest;
import kuit.modi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public void createDiary(CreateDiaryRequest request, MultipartFile imageFile) {
        LocalDate parsedDate = LocalDate.parse(request.date(), DateTimeFormatter.ISO_DATE);
        LocalDateTime now = LocalDateTime.now();

        Emotion emotion = emotionRepository.findByName(request.emotion())
                .orElseThrow(() -> new IllegalArgumentException("감정 정보가 유효하지 않습니다."));
        Tone tone = toneRepository.findByName(request.tone())
                .orElseThrow(() -> new IllegalArgumentException("톤 정보가 유효하지 않습니다."));
        Location location = locationRepository.findByAddress(request.address())
                .orElseThrow(() -> new IllegalArgumentException("주소 정보가 유효하지 않습니다."));
        Frame frame = frameRepository.findByName(request.frame()) // 문자열 → Frame 엔티티
                .orElseThrow(() -> new IllegalArgumentException("프레임 정보가 유효하지 않습니다."));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 유효하지 않습니다."));

        Diary diary = Diary.create(
                request.content(),
                request.summary(),
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
            String storedName = imageFile.getOriginalFilename();
            Image image = Image.create(storedName, diary);
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
}