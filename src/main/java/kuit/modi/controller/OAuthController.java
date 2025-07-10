package kuit.modi.controller;

import jakarta.servlet.http.HttpServletResponse;
import kuit.modi.domain.Member;
import kuit.modi.dto.GoogleUserInfo;
import kuit.modi.repository.MemberRepository;
import kuit.modi.service.GoogleOAuthService;
import kuit.modi.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuthController {
    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @GetMapping("/authorize/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String redirectUrl = googleOAuthService.getGoogleLoginUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String accessToken = googleOAuthService.getAccessToken(code);
        GoogleUserInfo userInfo = googleOAuthService.getUserInfo(accessToken);
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(userInfo.email());

        boolean isNew = false;
        Member member;
        if (existingMemberOpt.isPresent()) {
            member = existingMemberOpt.get();
        } else {
            // 기존 회원이 아니면 이메일만으로 임시 회원 생성
            isNew = true;
            member = memberRepository.save(new Member(userInfo.email()));
        }

        // jwt를 생성해서 쿠키에 포함하여 전달
        String jwt = jwtService.createToken(member.getId());
        ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofHours(1))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 프론트엔드로 리디렉션, isNew 여부 전달
        String redirectUrl = "https://your-frontend.com/oauth/callback?isNew=" + isNew ;
        response.sendRedirect(redirectUrl);
    }
}
