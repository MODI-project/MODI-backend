
package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
//일별 상세 일기 조회 응답 DTO
@Getter
@AllArgsConstructor
public class DailyDiaryDetailResponse {

    private MainDiaryDto main;
    private AdjacentDiaryDto previous;
    private AdjacentDiaryDto next;

    // 메인 일기 상세 정보
    @Getter
    @AllArgsConstructor
    public static class MainDiaryDto {
        private Long id;
        private String summary;
        private LocalDateTime datetime;
        private String thumbnailUrl;
        private List<String> tags;
        private Long frameId;
    }

    // 좌우 일기 요약 정보
    @Getter
    @AllArgsConstructor
    public static class AdjacentDiaryDto {
        private Long id;
        private String summary;
        private LocalDateTime datetime;
        private String thumbnailUrl;
        private Long frameId;
    }
}
