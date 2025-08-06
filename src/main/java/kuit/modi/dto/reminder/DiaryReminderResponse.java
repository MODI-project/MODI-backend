package kuit.modi.dto.reminder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryReminderResponse {
    private Long id;
    private LocalDateTime datetime;
    private String thumbnailUrl;
}