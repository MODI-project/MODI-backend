package kuit.modi.controller;

import kuit.modi.dto.diary.response.ErrorResponse;
import kuit.modi.exception.BaseExceptionResponseStatus;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.DiaryExceptionResponseStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        BaseExceptionResponseStatus status = ex.getStatus();
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ErrorResponse.of(status));
    }

    // 쿼리/경로 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName(); // 파라미터/패스변수 이름
        DiaryExceptionResponseStatus s;

        if ("year".equals(name) || "month".equals(name)) {
            s = DiaryExceptionResponseStatus.INVALID_YEAR_MONTH;
        } else if ("swLat".equals(name) || "swLng".equals(name) || "neLat".equals(name) || "neLng".equals(name)) {
            s = DiaryExceptionResponseStatus.INVALID_LOCATION;
        } else if ("diaryId".equals(name)) {
            s = DiaryExceptionResponseStatus.DIARY_NOT_FOUND;
        } else {
            s = DiaryExceptionResponseStatus.INVALID_DATE;
        }

        return ResponseEntity.status(s.getHttpStatus()).body(ErrorResponse.of(s));
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        DiaryExceptionResponseStatus s;

        if ("year".equals(name) || "month".equals(name)) {
            s = DiaryExceptionResponseStatus.INVALID_YEAR_MONTH;
        } else if ("swLat".equals(name) || "swLng".equals(name) || "neLat".equals(name) || "neLng".equals(name)) {
            s = DiaryExceptionResponseStatus.INVALID_LOCATION;
        } else {
            s = DiaryExceptionResponseStatus.INVALID_DATE;
        }

        return ResponseEntity.status(s.getHttpStatus()).body(ErrorResponse.of(s));
    }

    // 요청 본문/쿼리 파싱 실패(날짜/숫자 포맷 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        var s = DiaryExceptionResponseStatus.INVALID_DATE;
        return ResponseEntity.status(s.getHttpStatus()).body(ErrorResponse.of(s));
    }

}
