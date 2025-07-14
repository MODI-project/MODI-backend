package kuit.modi.controller;

import kuit.modi.dto.CreateDiaryRequest;
import kuit.modi.service.DiaryService;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok().build();
    }
}

