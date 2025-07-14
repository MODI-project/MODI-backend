package kuit.modi.service;

import kuit.modi.dto.UserRequest;
import kuit.modi.dto.UserResponse;
import kuit.modi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final MemberRepository memberRepository;


    public UserResponse createUser(UserRequest requestDto) {
        return new UserResponse();
    }

    public UserResponse getUser(Long id) {
        return new UserResponse();
    }
}
