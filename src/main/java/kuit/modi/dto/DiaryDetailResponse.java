package kuit.modi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//일기 상세 조회 DTO

@Getter
@AllArgsConstructor
public class DiaryDetailResponse {

    private Long id;
    private String content;
    private String summary;
    private LocalDate date;
    private EmotionDto emotion;
    private ToneDto tone;
    private List<TagDto> tags;
    private LocationDto location;
    private String font;
    private Long frameId;
    private List<String> imageUrls;
    private boolean favorites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 감정 정보를 담는 서브 DTO
    @Getter
    @AllArgsConstructor
    public static class EmotionDto {
        private Long id;
        private String name;
    }

    // 어투 정보를 담는 서브 DTO
    @Getter
    @AllArgsConstructor
    public static class ToneDto {
        private Long id;
        private String name;
    }

    // 태그 정보를 담는 서브 DTO
    @Getter
    @AllArgsConstructor
    public static class TagDto {
        private Long id;
        private String name;
    }

    //위치 정보를 담는 서브 DTO
    @Getter
    @AllArgsConstructor
    public static class LocationDto {
        private Long id;
        private String address;
        private Double latitude;
        private Double longitude;
    }
}