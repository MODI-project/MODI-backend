package kuit.modi.dto.diary.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

//태그 기반 검색 결과 DTO
//특정 태그로 조회된 일기들을 날짜 단위로 그룹화하 -> 이미지 URL 리스트로 한번에

@Getter
@AllArgsConstructor
public class DiaryTagSearchItemResponse {

    private LocalDate date;
    private List<String> imageUrls;  // 해당 날짜의 이미지 URL 리스트
}
