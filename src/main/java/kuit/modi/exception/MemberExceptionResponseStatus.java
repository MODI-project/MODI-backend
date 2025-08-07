package kuit.modi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberExceptionResponseStatus implements BaseExceptionResponseStatus {

    MEMBER_NOT_FOUND(30001, HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    INVALID_CHARACTER_TYPE(30002, HttpStatus.BAD_REQUEST, "잘못된 캐릭터 유형입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}