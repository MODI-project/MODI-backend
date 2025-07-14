package kuit.modi.dto;

import java.util.List;

public record CreateDiaryRequest(
        Long memberId,
        String content,
        String summary,
        String date,
        String address,
        String emotion,
        String tone,
        List<String> tags,
        String font,
        String frame
) {}
