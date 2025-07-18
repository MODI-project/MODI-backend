package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.*;
import kuit.modi.exception.InvalidDateException;
import kuit.modi.exception.InvalidYearMonthException;
import kuit.modi.service.DiaryQueryService;
import kuit.modi.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryQueryService diaryQueryService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDiary(
            @RequestPart("data") CreateDiaryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        diaryService.createDiary(request, image);
        return ResponseEntity.ok(new DiaryCreateResponse("기록 생성이 완료되었습니다."));
    }

    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDiary(
            @PathVariable Long diaryId,
            @RequestPart("data") UpdateDiaryRequest request,   // JSON 부분
            @RequestPart(value = "image", required = false) MultipartFile imageFile  // 파일 부분
    ) {
        diaryService.updateDiary(diaryId, request, imageFile);
        return ResponseEntity.ok(new DiaryUpdateResponse(diaryId, "기록 수정이 완료되었습니다."));
    }

    @PostMapping("/{diaryId}/favorite")
    public ResponseEntity<?> updateFavorite(
            @PathVariable Long diaryId,
            @RequestParam boolean favorite) {

        diaryService.updateFavorite(diaryId, favorite);
        return ResponseEntity.ok(
                new FavoriteUpdateResponse("즐겨찾기 " + (favorite ? "등록" : "해제") + " 완료되었습니다.")
        );
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok(new DiaryDeleteResponse("기록 삭제가 완료되었습니다."));
    }

    //일기 상세 조회
    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponse> getDiaryDetail(
            @AuthenticationPrincipal Member member,
            @PathVariable Long diaryId
    ) {
        DiaryDetailResponse response = diaryQueryService.getDiaryDetail(diaryId, member);
        return ResponseEntity.ok(response);
    }

    //일기 날짜 기반 조회 (메인 + 이전/다음)
    @GetMapping(params = "date")
    public ResponseEntity<DailyDiaryDetailResponse> getDailyDiaryDetail(
            @AuthenticationPrincipal Member member,
            @RequestParam String date
    ) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException();
        }

        DailyDiaryDetailResponse response = diaryQueryService.getDailyDetail(parsedDate, member);
        return ResponseEntity.ok(response);
    }

    // 특정 연/월의 일기 목록 조회 (월별 보기)
    @GetMapping(params = {"year", "month"})
    public ResponseEntity<List<DiaryMonthlyItemDto>> getMonthlyDiaries(
            @AuthenticationPrincipal Member member,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (year != null && month != null) {
            List<DiaryMonthlyItemDto> diaries = diaryQueryService.getMonthlyDiaries(year, month, member);
            return ResponseEntity.ok(diaries);
        }

        // 다른 GET 쿼리(예: ?date=2025-07-17)와 구분이 필요하다면 여기에 분기 추가
        throw new InvalidYearMonthException(); // 예시
    }

    // 즐겨찾기한 일기 목록 조회
    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteDiaryItemDto>> getFavoriteDiaries(
            @AuthenticationPrincipal Member member
    ) {
        List<FavoriteDiaryItemDto> favorites = diaryQueryService.getFavoriteDiaries(member);
        return ResponseEntity.ok(favorites);
    }



}



