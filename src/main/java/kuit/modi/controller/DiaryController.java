package kuit.modi.controller;

import kuit.modi.dto.*;
import kuit.modi.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

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
}

