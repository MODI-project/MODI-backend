package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.UserRequest;
import kuit.modi.dto.UserResponse;
import kuit.modi.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal Member member,
            @RequestBody UserRequest userRequest) {

        Member updated = memberService.completeSignup(member.getId(), userRequest);

        UserResponse response = new UserResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getNickname(),
                updated.getCharacterType().getId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal Member member) {
        UserResponse response = new UserResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCharacterType().getId());
        return ResponseEntity.ok(response);
    }
}
