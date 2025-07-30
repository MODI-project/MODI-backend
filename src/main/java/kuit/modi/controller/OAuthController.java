package kuit.modi.controller;

import jakarta.servlet.http.HttpServletResponse;
import kuit.modi.domain.CharacterType;
import kuit.modi.domain.Member;
import kuit.modi.dto.member.GoogleUserInfo;
import kuit.modi.exception.NotFoundException;
import kuit.modi.repository.CharacterTypeRepository;
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
    private final CharacterTypeRepository characterTypeRepository;

    @GetMapping("/authorize/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String redirectUrl = googleOAuthService.getGoogleLoginUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        System.out.println("/callback/google 요청 받음");
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
            CharacterType momo = characterTypeRepository.findByName("Momo")
                    .orElseThrow(() -> new NotFoundException("캐릭터 타입 'Momo'가 존재하지 않습니다"));

            Member newMember = new Member(userInfo.email(), momo);
            member = memberRepository.save(newMember);
        }

        // jwt를 생성해서 쿠키에 포함하여 전달
        String jwt = jwtService.createToken(member.getId());
        System.out.println(jwt);
        ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(1))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 프론트엔드로 리디렉션 - 현재는 localhost로 설정, 추후 수정 필요
        String redirectUrl = "http://localhost:5173/home";
        if(isNew)
            redirectUrl = "http://localhost:5173/information-setting";

        response.sendRedirect(redirectUrl);
    }
}
