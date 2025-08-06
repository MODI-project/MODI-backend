
package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.diary.response.*;
import kuit.modi.dto.diary.response.DiaryDetailResponse.EmotionDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.LocationDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.TagDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.ToneDto;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.exception.DiaryNotFoundException;
import kuit.modi.exception.InvalidYearMonthException;
import kuit.modi.repository.DiaryQueryRepository;
import kuit.modi.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

//일기 조회 전용 서비스:wq
@Service
@RequiredArgsConstructor
public class DiaryQueryService {

    private final DiaryQueryRepository diaryQueryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public DiaryAllResponse getDiaryAll(Member member) {
        List<Diary> diaries = diaryQueryRepository.findAllByMemberId(member.getId());

        List<DiaryHomeResponse> responses = diaries.stream()
                .map(this::toHomeResponse)
                .toList();

        return new DiaryAllResponse(responses);
    }

    private DiaryHomeResponse toHomeResponse(Diary diary) {
        String photoUrl = diary.getImage() != null ? diary.getImage().getUrl() : null;
        String emotion = diary.getEmotion() != null ? diary.getEmotion().getName() : null;

        List<String> tags = diary.getDiaryTags().stream()
                .map(dt -> dt.getTag().getName())
                .toList();

        return new DiaryHomeResponse(
                diary.getId(),
                diary.getDate().toLocalDate(),
                photoUrl,
                diary.getSummary(),
                emotion,
                tags,
                diary.getCreatedAt()
        );
    }

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
        Long frameId = (diary.getStyle() == null || diary.getStyle().getFrame() == null)
                ? null : diary.getStyle().getFrame().getId();

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
                frameId,
                imageUrls,
                diary.isFavorite(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }

    //메인홈 일별보기 기능을 위한 월 전체 응답
    @Transactional(readOnly = true)
    public DiaryAllResponse getDailyDetailMonthly(int year, int month, Member member) {
        List<Diary> diaries = diaryQueryRepository.findByYearMonthForDaily(member.getId(), year, month);

        List<DiaryHomeResponse> responses = diaries.stream()
                .map(this::toHomeResponse)
                .toList();

        return new DiaryAllResponse(responses);
    }

    private DailyDiaryDetailResponse.MainDiaryDto toMainDto(Diary diary) {
        List<String> tags = diary.getDiaryTags().stream()
                .map(dt -> dt.getTag().getName())
                .limit(3)
                .toList();

        Long frameId = (diary.getStyle() != null && diary.getStyle().getFrame() != null)
                ? diary.getStyle().getFrame().getId()
                : null;

        return new DailyDiaryDetailResponse.MainDiaryDto(
                diary.getId(),
                diary.getSummary(),
                diary.getDate(),
                diary.getImage() != null ? diary.getImage().getUrl() : null,
                tags,
                frameId
        );
    }

    // 메인 화면 좌/우 일기 변환
    private DailyDiaryDetailResponse.AdjacentDiaryDto toAdjacentDto(Diary diary) {
        Long frameId = (diary.getStyle() != null && diary.getStyle().getFrame() != null)
                ? diary.getStyle().getFrame().getId()
                : null;

        return new DailyDiaryDetailResponse.AdjacentDiaryDto(
                diary.getId(),
                diary.getSummary(),
                diary.getCreatedAt(),
                diary.getImage() != null ? diary.getImage().getUrl() : null,
                frameId
        );
    }

    //특정 연/월에 해당하는 일기 목록 조회
    @Transactional(readOnly = true)
    public List<DiaryMonthlyItemResponse> getMonthlyDiaries(int year, int month, Member member) {
        if (month < 1 || month > 12) {
            throw new InvalidYearMonthException();
        }

        List<Diary> diaries = diaryQueryRepository.findByYearMonth(member.getId(), year, month);

        return diaries.stream()
                .map(diary -> new DiaryMonthlyItemResponse(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : null,
                        diary.getEmotion().getName(),
                        diary.getCreatedAt()
                ))
                .toList();
    }

    //즐겨찾기한 일기 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteDiaryItemResponse> getFavoriteDiaries(Member member) {
        List<Diary> diaries = diaryQueryRepository.findFavorites(member.getId());

        return diaries.stream()
                .map(diary -> new FavoriteDiaryItemResponse(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : null,
                        diary.getCreatedAt()
                ))
                .toList();
    }

    // Object[] -> RankItem으로 변환 (상위 4개만)
    private List<DiaryStatisticsResponse.RankItem> mapStats(List<Object[]> rawStats) {
        return rawStats.stream()
                .limit(4)
                .map(obj -> new DiaryStatisticsResponse.RankItem(
                        (String) obj[0],
                        ((Long) obj[1]).intValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public DiaryStatisticsResponse getMonthlyStatistics(int year, int month, Member member) {
        Long memberId = member.getId();

        // 전체 일기 수
        int totalCount = (int) diaryQueryRepository.countMonthlyDiaries(memberId, year, month);

        // 감정 통계
        List<DiaryStatisticsResponse.RankItem> topEmotions =
                mapStats(diaryQueryRepository.findEmotionStats(memberId, year, month));

        // 어투 통계
        List<DiaryStatisticsResponse.RankItem> topTones =
                mapStats(diaryQueryRepository.findToneStats(memberId, year, month));

        // 위치 통계
        List<DiaryStatisticsResponse.RankItem> topLocations =
                mapStats(diaryQueryRepository.findLocationStats(memberId, year, month));

        return new DiaryStatisticsResponse(
                totalCount,
                topEmotions,
                topTones,
                topLocations
        );
    }

    @Transactional(readOnly = true)
    public List<DiaryTagSearchItemResponse> getDiariesByTag(Long tagId, Member member) {
        Long memberId = member.getId();

        // (날짜, 이미지 URL) 튜플 리스트
        List<Object[]> rawResults = diaryQueryRepository.findByTagIdWithImage(memberId, tagId);

        // 날짜 기준 그룹
        Map<LocalDate, List<String>> grouped = rawResults.stream()
                .collect(Collectors.groupingBy(
                        obj -> ((LocalDateTime) obj[0]).toLocalDate(),
                        TreeMap::new, // 정렬된 Map 유지
                        Collectors.mapping(obj -> (String) obj[1], Collectors.toList())
                ));

        // 그룹된 데이터 DTO로 변환
        return grouped.entrySet().stream()
                .map(entry -> new DiaryTagSearchItemResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<String> getPopularTags() {
        return tagRepository.findTopTagNames(PageRequest.of(0, 10)); // 상위 10개
    }

    // 지도 조회용
    public List<DiaryNearbyResponse> getNearbyDiaries(double swLat, double swLng, double neLat, double neLng, Member member) {
        List<Diary> diaries = diaryQueryRepository.findNearbyDiaries(member.getId(), swLat, swLng, neLat, neLng);

        return diaries.stream()
                .map(diary -> {
                    String thumbnailUrl = (diary.getImage() != null && diary.getImage().getUrl() != null)
                            ? diary.getImage().getUrl()
                            : "https://cdn.modi.com/default-thumb.jpg";

                    return new DiaryNearbyResponse(
                            diary.getId(),
                            diary.getDate(),
                            diary.getEmotion().getName(),  // 감정 이름을 가져온다고 가정
                            new DiaryNearbyResponse.LocationDto(
                                    diary.getLocation().getId(),
                                    diary.getLocation().getAddress(),
                                    diary.getLocation().getLatitude(),
                                    diary.getLocation().getLongitude()
                            ),
                            thumbnailUrl
                    );
                })
                .collect(Collectors.toList());
    }

}