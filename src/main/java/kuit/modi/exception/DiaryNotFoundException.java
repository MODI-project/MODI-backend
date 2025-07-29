package kuit.modi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//일기 ID 없음 -> 404
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DiaryNotFoundException extends RuntimeException {
    public DiaryNotFoundException() {
        super("Diary not found");
    }
}

