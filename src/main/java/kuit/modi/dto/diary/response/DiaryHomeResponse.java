package kuit.modi.dto.diary.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DiaryHomeResponse(
        Long id,
        LocalDate date,
        String photoUrl,
        String summary,
        String emotion,
        List<String> tags,
        LocalDateTime created_at
) {}
