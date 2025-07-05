package kuit.modi.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class User {

    private Long id;
    private String nickname;
    private String email;
    private String password;    // 소셜로그인만 할 경우 따로 저장할 필요 없을 듯??
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CharacterType characterType;

    // 소셜 로그인 후 이메일 정보만 저장, 나머지 필드는 회원가입 나머지 절차에서 처리
    public User(String email) {
        this.email = email;
    }
}
