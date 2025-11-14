package kuit.modi.dto.reminder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReminderPagedResponse {
    private List<DiaryReminderResponse> items;
    private String nextCursor;
}
