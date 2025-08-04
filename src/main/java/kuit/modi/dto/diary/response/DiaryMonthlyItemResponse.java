package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryMonthlyItemResponse {
    private Long id;
    private LocalDate date;
    private String thumbnailUrl;
    private String emotion;
    private LocalDateTime created_at;
}
