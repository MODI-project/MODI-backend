package kuit.modi.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class MemberResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String character;
}
