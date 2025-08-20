
package kuit.modi.service;

import kuit.modi.domain.*;
import kuit.modi.dto.diary.response.*;
import kuit.modi.dto.diary.response.DiaryDetailResponse.EmotionDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.LocationDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.TagDto;
import kuit.modi.dto.diary.response.DiaryDetailResponse.ToneDto;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;
import kuit.modi.repository.DiaryQueryRepository;
import kuit.modi.repository.DiaryTagRepository;
import kuit.modi.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

//일기 조회 전용 서비스
@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryQueryService {

    private final DiaryQueryRepository diaryQueryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public DiaryAllResponse getDiaryAll(Member member) {
        List<Diary> diaries = diaryQueryRepository.findAllByMemberId(member.getId());

        List<DiaryHomeResponse> responses = diaries.stream()
                .map(this::toHomeResponse)
                .toList();

        return new DiaryAllResponse(responses);
    }

    private DiaryHomeResponse toHomeResponse(Diary diary) {
        String photoUrl = diary.getImage() != null
                ? s3Service.getFileUrl(diary.getImage().getUrl())
                : null;
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
                .orElseThrow(() -> new CustomException(DiaryExceptionResponseStatus.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(member.getId())) {
            throw new CustomException(DiaryExceptionResponseStatus.DIARY_NOT_FOUND);
        }

        return toResponse(diary);
    }

    //특정 연/월에 해당하는 일기 목록 조회
    @Transactional(readOnly = true)
    public List<DiaryMonthlyItemResponse> getMonthlyDiaries(int year, int month, Member member) {
        if (month < 1 || month > 12) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_YEAR_MONTH);
        }

        List<Diary> diaries = diaryQueryRepository.findByYearMonth(member.getId(), year, month);

        return diaries.stream()
                .map(diary -> new DiaryMonthlyItemResponse(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? s3Service.getFileUrl(diary.getImage().getUrl()) : null,
                        diary.getEmotion().getName(),
                        diary.getCreatedAt()
                ))
                .toList();
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
                ? List.of(s3Service.getFileUrl(diary.getImage().getUrl()))
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

    // 메인홈 일별보기 기능을 위한 월 전체 응답
    @Transactional(readOnly = true)
    public DiaryAllResponse getDailyDetailMonthly(int year, int month, Member member) {
        if (month < 1 || month > 12) {
            throw new CustomException(DiaryExceptionResponseStatus.INVALID_YEAR_MONTH);
        }

        List<Diary> diaries = diaryQueryRepository.findByYearMonthForDaily(member.getId(), year, month);

        List<DiaryHomeResponse> responses = diaries.stream()
                .map(this::toHomeResponse)
                .toList();

        return new DiaryAllResponse(responses);
    }

    //즐겨찾기한 일기 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteDiaryItemResponse> getFavoriteDiaries(Member member) {
        List<Diary> diaries = diaryQueryRepository.findFavorites(member.getId());

        return diaries.stream()
                .map(diary -> new FavoriteDiaryItemResponse(
                        diary.getId(),
                        diary.getDate().toLocalDate(),
                        diary.getImage() != null ? s3Service.getFileUrl(diary.getImage().getUrl()) : null,
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
    public List<DiaryTagSearchItemResponse> getDiariesByTagName(String rawTagName, Member member) {
        Long memberId = member.getId();

        // 0) tagName 정규화: URL 디코딩 + trim + 앞의 '#' 제거 + 중간의 이중공백 정리
        String tagName = normalizeTagName(rawTagName);
        if (tagName.isBlank()) return List.of();

        // 1) 한 방 EXISTS 조회
        List<Object[]> rows = diaryQueryRepository.findByMemberAndTagNameWithImage(memberId, tagName);

        // 2) 날짜 → 일기 → 이미지 리스트
        Map<LocalDate, Map<Long, List<String>>> byDate = new TreeMap<>();
        for (Object[] r : rows) {
            Long diaryId = (Long) r[0];

            LocalDate date;
            Object dateObj = r[1];
            if (dateObj instanceof LocalDate ld) date = ld;
            else if (dateObj instanceof LocalDateTime ldt) date = ldt.toLocalDate();
            else throw new IllegalStateException("Unsupported date type: " + (dateObj == null ? "null" : dateObj.getClass()));

            String keyOrUrl = (String) r[2];

            byDate.computeIfAbsent(date, d -> new LinkedHashMap<>())
                    .computeIfAbsent(diaryId, id -> new ArrayList<>())
                    .add(keyOrUrl);
        }

        // 3) DTO 변환 (S3 key면 presign)
        List<DiaryTagSearchItemResponse> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<Long, List<String>>> e : byDate.entrySet()) {
            LocalDate date = e.getKey();
            List<DiaryImageGroupResponse> groups = e.getValue().entrySet().stream()
                    .map(entry -> {
                        Long id = entry.getKey();
                        List<String> urls = entry.getValue().stream()
                                .filter(Objects::nonNull)
                                .map(v -> isUrl(v) ? v : s3Service.getFileUrl(v))
                                .collect(Collectors.toList());
                        return new DiaryImageGroupResponse(id, urls);
                    })
                    .toList();
            result.add(new DiaryTagSearchItemResponse(date, groups));
        }

        // 디버그 로그 (필요하면 살리고, 끝나면 제거)
        log.info("tagName='{}' memberId={} -> dates={}, rows={}",
                tagName, memberId, result.size(), rows.size());

        return result;
    }

    private String normalizeTagName(String s) {
        if (s == null) return "";
        String dec = URLDecoder.decode(s, StandardCharsets.UTF_8);
        dec = dec.trim();
        if (dec.startsWith("#")) dec = dec.substring(1); // DB가 # 없이 저장돼 있으면 필수
        // 필요시 추가 정규화: dec = dec.replaceAll("\\s+", " ");
        return dec;
    }

    private boolean isUrl(String v) {
        if (v == null) return false;
        String s = v.toLowerCase(Locale.ROOT);
        return s.startsWith("http://") || s.startsWith("https://");
    }

    public List<String> getPopularTags(Long memberId) {
        return tagRepository.findTopTagNamesByMemberId(memberId, PageRequest.of(0, 10)); // 상위 10개
    }

    // 지도 조회용
    public List<DiaryNearbyResponse> getNearbyDiaries(double swLat, double swLng, double neLat, double neLng, Member member) {
        List<Diary> diaries = diaryQueryRepository.findNearbyDiaries(member.getId(), swLat, swLng, neLat, neLng);

        return diaries.stream()
                .map(diary -> {
                    String thumbnailUrl = (diary.getImage() != null && diary.getImage().getUrl() != null)
                            ? s3Service.getFileUrl(diary.getImage().getUrl())
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
                diary.getImage() != null ? s3Service.getFileUrl(diary.getImage().getUrl()) : null,
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
                diary.getImage() != null ? s3Service.getFileUrl(diary.getImage().getUrl()) : null,
                frameId
        );
    }

}