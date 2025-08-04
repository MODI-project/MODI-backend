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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.mode}")
    private String mode;

    @Value("${app.frontend.local}")
    private String localRedirectBase;

    @Value("${app.frontend.prod}")
    private String prodRedirectBase;

    @GetMapping("/authorize/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String redirectUrl = googleOAuthService.getGoogleLoginUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        //System.out.println("/callback/google 요청 받음");
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

        // jwt 생성
        String jwt = jwtService.createToken(member.getId());
        System.out.println(jwt);

        // 환경에 따른 분기 처리
        boolean isLocal = mode.equalsIgnoreCase("local");

        ResponseCookie cookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(!isLocal) // 로컬은 false, 배포는 true
                .path("/")
                .sameSite(isLocal ? "Lax" : "None") // 로컬은 기본값, 배포는 cross-origin 허용
                .maxAge(Duration.ofHours(24))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // 리디렉션 URL 설정
        String baseRedirect = isLocal ? localRedirectBase : prodRedirectBase;
        String redirectUrl = baseRedirect + (isNew ? "/information-setting" : "/home");

        response.sendRedirect(redirectUrl);
    }
}
