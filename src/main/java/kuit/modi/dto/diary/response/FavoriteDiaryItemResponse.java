
package kuit.modi.dto.diary.response;

import java.time.LocalDate;

public record FavoriteDiaryItemResponse(
        Long id,
        LocalDate date,
        String thumbnailUrl
) {}

