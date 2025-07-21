package kuit.modi.dto;

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
