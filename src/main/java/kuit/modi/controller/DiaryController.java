package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.diary.request.CreateDiaryRequest;
import kuit.modi.dto.diary.request.UpdateDiaryRequest;
import kuit.modi.dto.diary.response.*;
import kuit.modi.dto.reminder.DiaryReminderResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryQueryService diaryQueryService;

    // 일기 생성
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDiary(
            @AuthenticationPrincipal Member member,
            @RequestPart("data") CreateDiaryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long createdId = diaryService.createDiary(member, request, image);
        return ResponseEntity.ok(new DiaryCreateResponse(createdId, "기록 생성이 완료되었습니다."));
    }

    // 일기 수정
    @PutMapping(value = "/{diaryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDiary(
            @PathVariable Long diaryId,
            @RequestPart("data") UpdateDiaryRequest request,   // JSON 부분
            @RequestPart(value = "image", required = false) MultipartFile imageFile  // 파일 부분
    ) {
        diaryService.updateDiary(diaryId, request, imageFile);
        return ResponseEntity.ok(new DiaryUpdateResponse(diaryId, "기록 수정이 완료되었습니다."));
    }

    // 일기 즐겨찾기 설정
    @PostMapping("/{diaryId}/favorite")
    public ResponseEntity<?> updateFavorite(
            @PathVariable Long diaryId,
            @RequestParam boolean favorite) {

        diaryService.updateFavorite(diaryId, favorite);
        return ResponseEntity.ok(
                new FavoriteUpdateResponse("즐겨찾기 " + (favorite ? "등록" : "해제") + " 완료되었습니다.")
        );
    }

    // 일기 삭제
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok(new DiaryDeleteResponse("기록 삭제가 완료되었습니다."));
    }

    // 홈화면 전체 일기 조회용
    @GetMapping
    public ResponseEntity<?> getDiaryAll(
            @AuthenticationPrincipal Member member
    ) {
        DiaryAllResponse response = diaryQueryService.getDiaryAll(member);
        return ResponseEntity.ok(response);
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

    // 일별 기록 조회용 월 전체 기록 응답
    @GetMapping("/daily")
    public ResponseEntity<DiaryAllResponse> getDailyDiaryDetail(
            @AuthenticationPrincipal Member member,
            @RequestParam int year,
            @RequestParam int month
    ) {
        DiaryAllResponse response = diaryQueryService.getDailyDetailMonthly(year, month, member);
        return ResponseEntity.ok(response);
    }

    // 특정 연/월의 일기 목록 조회 (월별 보기)
    @GetMapping(params = {"year", "month"})
    public ResponseEntity<List<DiaryMonthlyItemResponse>> getMonthlyDiaries(
            @AuthenticationPrincipal Member member,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (year != null && month != null) {
            List<DiaryMonthlyItemResponse> diaries = diaryQueryService.getMonthlyDiaries(year, month, member);
            return ResponseEntity.ok(diaries);
        }

        // 다른 GET 쿼리(예: ?date=2025-07-17)와 구분이 필요하다면 여기에 분기 추가
        throw new InvalidYearMonthException(); // 예시
    }

    // 즐겨찾기한 일기 목록 조회
    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteDiaryItemResponse>> getFavoriteDiaries(
            @AuthenticationPrincipal Member member
    ) {
        List<FavoriteDiaryItemResponse> favorites = diaryQueryService.getFavoriteDiaries(member);
        return ResponseEntity.ok(favorites);
    }

    // 월간 통계 조회 API
    @GetMapping("/statistics")
    public ResponseEntity<DiaryStatisticsResponse> getMonthlyStatistics(
            @AuthenticationPrincipal Member member,
            @RequestParam int year,
            @RequestParam int month
    ) {
        DiaryStatisticsResponse response = diaryQueryService.getMonthlyStatistics(year, month, member);
        return ResponseEntity.ok(response);
    }

    // 특정 태그 기반으로 일기 검색 (날짜별 이미지 리스트)
    @GetMapping(params = "tagId")
    public ResponseEntity<List<DiaryTagSearchItemResponse>> getDiariesByTag(
            @AuthenticationPrincipal Member member,
            @RequestParam Long tagId
    ) {
        List<DiaryTagSearchItemResponse> results = diaryQueryService.getDiariesByTag(tagId, member);
        return ResponseEntity.ok(results);
    }

    // 많이 쓰이는 태그 조회
    @GetMapping("/tags/popular")
    public ResponseEntity<List<String>> getPopularTags() {
        List<String> tags = diaryQueryService.getPopularTags();
        return ResponseEntity.ok(tags);
    }

    // 지도 조회
    @GetMapping("/nearby")
    public ResponseEntity<List<DiaryNearbyResponse>> getNearbyDiaries(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @AuthenticationPrincipal Member member) {

        List<DiaryNearbyResponse> diaries = diaryQueryService.getNearbyDiaries(swLat, swLng, neLat, neLng, member);
        return ResponseEntity.ok(diaries);
    }

    // 리마인더 알림용 요청 - 반경 100m 기준
    /*
    @GetMapping("/reminder")
    public ResponseEntity<List<DiaryReminderResponse>> getReminderDiaries(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        List<DiaryReminderResponse> response = diaryQueryService.getReminderDiaries(latitude, longitude);
        return ResponseEntity.ok(response);
    }
    */
}



