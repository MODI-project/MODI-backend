package kuit.modi.controller;

import kuit.modi.dto.UserRequest;
import kuit.modi.dto.UserResponse;
import kuit.modi.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest requestDto) {
        UserResponse member = memberService.completeSignup(requestDto);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = memberService.getUser(id);
        return ResponseEntity.ok(user);
    }
}
