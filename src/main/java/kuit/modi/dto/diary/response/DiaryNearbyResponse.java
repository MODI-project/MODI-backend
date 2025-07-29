package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryNearbyResponse {
    private Long id;
    private LocalDateTime datetime;
    private String emotion;
    private LocationDto location;
    private String thumbnailUrl;

    @Getter
    @AllArgsConstructor
    public static class LocationDto {
        private Long id;
        private String address;
        private double latitude;
        private double longitude;
    }
}