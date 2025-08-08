package kuit.modi.controller;

import kuit.modi.dto.diary.response.ErrorResponse;
import kuit.modi.exception.BaseExceptionResponseStatus;
import kuit.modi.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        BaseExceptionResponseStatus status = ex.getStatus();
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(ErrorResponse.of(status));
    }
}
