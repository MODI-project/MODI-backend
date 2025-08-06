package kuit.modi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryExceptionResponseStatus implements BaseExceptionResponseStatus {

    DIARY_NOT_FOUND(10001, HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."),
    INVALID_DATE(10002, HttpStatus.BAD_REQUEST, "유효하지 않은 날짜입니다."),
    INVALID_TONE(10003, HttpStatus.BAD_REQUEST, "톤 정보가 유효하지 않습니다."),
    INVALID_EMOTION(10004, HttpStatus.BAD_REQUEST, "감정 정보가 유효하지 않습니다."),
    INVALID_FRAME(10005, HttpStatus.BAD_REQUEST, "프레임 정보가 유효하지 않습니다."),
    INVALID_LOCATION(10006, HttpStatus.BAD_REQUEST, "위치 정보가 유효하지 않습니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
