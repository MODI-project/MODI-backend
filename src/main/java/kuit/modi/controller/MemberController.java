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
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getUserInfo(@AuthenticationPrincipal Member member) {
        MemberResponse response = new MemberResponse(member.getId(), member.getEmail(), member.getNickname(), member.getCharacterType().getName());
        return ResponseEntity.ok(response);
    }

    // 회원가입, 회원정보수정
    @RequestMapping(
            path = {"", "/me"},
            method = {RequestMethod.POST, RequestMethod.PUT}
    )
    public ResponseEntity<MemberResponse> updateUser(
            @AuthenticationPrincipal Member member,
            @RequestBody MemberRequest memberRequest) {

        Member updated = memberService.update(member.getId(), memberRequest);

        MemberResponse response = new MemberResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getNickname(),
                updated.getCharacterType().getName()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal Member member) {
        memberService.deleteById(member.getId());
        return ResponseEntity.ok("User successfully deleted.");
    }
}
