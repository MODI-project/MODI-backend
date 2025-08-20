package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryImageGroupResponse {
    private Long diaryId;           // 일기 ID
    private List<String> imageUrls; // 해당 일기의 이미지 URL 리스트
}