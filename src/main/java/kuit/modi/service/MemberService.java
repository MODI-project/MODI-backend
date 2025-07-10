package kuit.modi.service;

import kuit.modi.domain.Member;
import kuit.modi.dto.UserRequest;
import kuit.modi.dto.UserResponse;
import kuit.modi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public UserResponse createUser(UserRequest requestDto) {
        return new UserResponse();
    }

    public UserResponse getUser(Long id) {
        return new UserResponse();
    }

    public UserResponse completeSignup(UserRequest requestDto) {

        return new UserResponse();
    }
}
