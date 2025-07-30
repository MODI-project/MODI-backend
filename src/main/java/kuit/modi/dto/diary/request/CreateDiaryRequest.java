package kuit.modi.dto.diary.request;

import java.util.List;

public record CreateDiaryRequest(
        String content,
        String summary,
        String date,
        String address,
        Double latitude,
        Double longitude,
        String emotion,
        String tone,
        List<String> tags,
        String font,
        String frame
) {}
