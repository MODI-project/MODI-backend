package kuit.modi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OpenAiExceptionResponseStatus implements BaseExceptionResponseStatus {

    API_ERROR(40001, HttpStatus.BAD_GATEWAY, "OpenAI API 요청 중 오류가 발생했습니다."),
    TIMEOUT(40002, HttpStatus.GATEWAY_TIMEOUT, "OpenAI API 요청이 시간 내에 완료되지 않았습니다."),
    INVALID_RESPONSE(40003, HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI API 응답 형식이 올바르지 않습니다."),
    UNKNOWN_ERROR(40004, HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
