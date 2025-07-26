package kuit.modi.dto.diary.response;

import java.util.List;

// 월간 통계 응답 DTO
public record DiaryStatisticsResponse(
        int totalCount,                    // 해당 월 전체 일기 수
        List<RankItem> topEmotions,        // 감정별 상위 4개
        List<RankItem> topTones,           // 어투별 상위 4개
        List<RankItem> topLocations        // 위치별 상위 4개
) {
    // 순위 항목 DTO (구조: 이름 + 개수)
    public record RankItem(
            String name,
            int count
    ) {}
}
