package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.member.MemberRequest;
import kuit.modi.dto.member.MemberResponse;
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
    public ResponseEntity<MemberResponse> createUser(
            @AuthenticationPrincipal Member member,
            @RequestBody MemberRequest memberRequest) {

        Member updated = memberService.completeSignup(member.getId(), memberRequest);

        MemberResponse response = new MemberResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getNickname(),
                updated.getCharacterType().getName()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getUserInfo(@AuthenticationPrincipal Member member) {
        MemberResponse response = new MemberResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCharacterType().getName());
        return ResponseEntity.ok(response);
    }
}
