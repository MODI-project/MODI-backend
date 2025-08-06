package kuit.modi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ExceptionResponseStatus implements BaseExceptionResponseStatus {

    S3_UPLOAD_FAILED(20001, HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    S3_UPLOAD_VERIFY_FAILED(20002, HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드 후 파일이 존재하지 않습니다."),
    S3_DELETE_FAILED(20003, HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}

