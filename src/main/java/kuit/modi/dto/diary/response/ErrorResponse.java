package kuit.modi.dto.diary.response;

import kuit.modi.exception.BaseExceptionResponseStatus;

public record ErrorResponse(int code, String message, String reason) {
    public static ErrorResponse of(BaseExceptionResponseStatus status) {
        return new ErrorResponse(
                status.getCode(),
                status.getMessage(),
                status.getHttpStatus().name()
        );
    }
}
