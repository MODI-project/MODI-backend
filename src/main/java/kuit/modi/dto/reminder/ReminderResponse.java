package kuit.modi.dto.reminder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReminderResponse {
    private Long id;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private String address;

    @JsonProperty("lastVisit")
    private LocalDateTime lastVisit;

    private String emotion;
}
