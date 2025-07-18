
package kuit.modi.dto;

import java.time.LocalDate;

public record FavoriteDiaryItemDto(
        Long id,
        LocalDate date,
        String thumbnailUrl
) {}

