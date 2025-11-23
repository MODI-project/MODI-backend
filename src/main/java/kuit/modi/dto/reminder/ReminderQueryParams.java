package kuit.modi.dto.reminder;

import java.time.Instant;

public record ReminderQueryParams(
        String address,
        Instant since,
        Instant until,
        Integer limit,
        String cursor
) {}

