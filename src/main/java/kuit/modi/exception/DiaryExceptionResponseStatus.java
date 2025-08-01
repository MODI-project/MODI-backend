package kuit.modi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryExceptionResponseStatus implements BaseExceptionResponseStatus {
    DIARY_NOT_FOUND(10001, HttpStatus.NOT_FOUND, "해당 기록을 찾을 수 없습니다."),
    INVALID_DATE(10002, HttpStatus.BAD_REQUEST, "유효하지 않은 날짜입니다."),
    INVALID_YEAR_MONTH(10003, HttpStatus.BAD_REQUEST, "잘못된 연/월 형식입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
