
package kuit.modi.dto.diary.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FavoriteDiaryItemResponse(
        Long id,
        LocalDate date,
        String thumbnailUrl,
        LocalDateTime created_at
) {}

