package kuit.modi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DiaryMonthlyItemDto {
    private Long id;
    private LocalDate date;
    private String thumbnailUrl;
    private String emotion;
}
