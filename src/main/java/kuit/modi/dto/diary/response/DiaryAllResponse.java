package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 전체 일기 목록 응답 DTO
@Getter
@AllArgsConstructor
public class DiaryAllResponse {
    private List<DiaryHomeResponse> diaries;
}