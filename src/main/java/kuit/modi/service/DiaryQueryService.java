
package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.DailyDiaryDetailResponse;
import kuit.modi.dto.DiaryDetailResponse;
import kuit.modi.dto.DiaryDetailResponse.*;
import kuit.modi.dto.DiaryMonthlyItemDto;
import kuit.modi.dto.FavoriteDiaryItemDto;
import kuit.modi.exception.DiaryNotFoundException;
import kuit.modi.exception.InvalidYearMonthException;
import kuit.modi.repository.DiaryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
//일기 조회 전용 서비스
@Service
@RequiredArgsConstructor
public class DiaryQueryService {

    private final DiaryQueryRepository diaryQueryRepository;

    /*
     * diaryId에 해당하는 일기를 상세 조회합니다.
     * - 연관 정보 포함 (감정, 어투, 태그, 위치, 이미지, 스타일 등)
     * - 사용자(member)의 일기인지 확인
     */
    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(Long diaryId, Member member) {
        Diary diary = diaryQueryRepository.findDetailById(diaryId)
                .orElseThrow(DiaryNotFoundException::new);

        if (!diary.getMember().getId().equals(member.getId())) {
            throw new DiaryNotFoundException();
        }

        return toResponse(diary);
    }

    // Diary 엔티티 -> DiaryDetailResponse
    private DiaryDetailResponse toResponse(Diary diary) {
        EmotionDto emotion = new EmotionDto(diary.getEmotion().getId(), diary.getEmotion().getName());
        ToneDto tone = new ToneDto(diary.getTone().getId(), diary.getTone().getName());

        List<TagDto> tags = diary.getDiaryTags().stream()
                .map(dt -> new TagDto(dt.getTag().getId(), dt.getTag().getName()))
                .toList();

        Location location = diary.getLocation();
        LocationDto locationDto = new LocationDto(
                location.getId(), location.getAddress(), location.getLatitude(), location.getLongitude()
        );

        List<String> imageUrls = diary.getImage() != null
                ? List.of(diary.getImage().getUrl())
                : List.of();

        String font = diary.getStyle() == null ? null : diary.getStyle().getFont();
        String frame = (diary.getStyle() == null || diary.getStyle().getFrame() == null)
                ? null : diary.getStyle().getFrame().getName();



        return new DiaryDetailResponse(
                diary.getId(),
                diary.getContent(),
                diary.getSummary(),
                diary.getDate().toLocalDate(),
                emotion,
                tone,
                tags,
                locationDto,
                font,
                frame,
                imageUrls,
                diary.isFavorite(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }

    //특정 날짜의 일기 중 하나를 메인으로, 양 옆 일기도 함께 조회
    @Transactional(readOnly = true)
    public DailyDiaryDetailResponse getDailyDetail(LocalDate date, Member member) {
        List<Diary> diaries = diaryQueryRepository.findDiariesByDate(member.getId(), date);

        if (diaries.isEmpty()) {
            throw new DiaryNotFoundException();
        }

        // 메인 일기는 첫 번째
        Diary main = diaries.get(0);
        DailyDiaryDetailResponse.MainDiaryDto mainDto = toMainDto(main);

        // 이전/다음은 인덱스 1, 2 기준
        DailyDiaryDetailResponse.AdjacentDiaryDto previous = null;
        DailyDiaryDetailResponse.AdjacentDiaryDto next = null;

        if (diaries.size() > 1) {
            next = toAdjacentDto(diaries.get(1));
        }
        if (diaries.size() > 2) {
            previous = toAdjacentDto(diaries.get(2));
        }

        return new DailyDiaryDetailResponse(mainDto, previous, next);
    }

    private DailyDiaryDetailResponse.MainDiaryDto toMainDto(Diary diary) {
        List<String> tags = diary.getDiaryTags().stream()
                .map(dt -> dt.getTag().getName())
                .limit(3)
                .toList();

        return new DailyDiaryDetailResponse.MainDiaryDto(
                diary.getId(),
                diary.getSummary(),
                diary.getCreatedAt(),
                diary.getImage() != null ? diary.getImage().getUrl() : null,
                tags
        );
    }

    private DailyDiaryDetailResponse.AdjacentDiaryDto toAdjacentDto(Diary diary) {
        return new DailyDiaryDetailResponse.AdjacentDiaryDto(
                diary.getId(),
                diary.getSummary(),
                diary.getCreatedAt(),
                diary.getImage() != null ? diary.getImage().getUrl() : null
        );
    }

    //특정 연/월에 해당하는 일기 목록 조회
    @Transactional(readOnly = true)
    public List<DiaryMonthlyItemDto> getMonthlyDiaries(int year, int month, Member member) {
        if (month < 1 || month > 12) {
            throw new InvalidYearMonthException();
        }

        List<Diary> diaries = diaryQueryRepository.findByYearMonth(member.getId(), year, month);

        return diaries.stream()
                .map(diary -> new DiaryMonthlyItemDto(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : null,
                        diary.getEmotion().getName()
                ))
                .toList();
    }

    //즐겨찾기한 일기 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteDiaryItemDto> getFavoriteDiaries(Member member) {
        List<Diary> diaries = diaryQueryRepository.findFavorites(member.getId());

        return diaries.stream()
                .map(diary -> new FavoriteDiaryItemDto(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : null
                ))
                .toList();
    }



}