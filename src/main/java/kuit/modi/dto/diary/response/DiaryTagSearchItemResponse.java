package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryTagSearchItemResponse {
    private LocalDate date;                         // 날짜
    private List<DiaryImageGroupResponse> diaries;  // 해당 날짜의 모든 일기 + 이미지 그룹
}