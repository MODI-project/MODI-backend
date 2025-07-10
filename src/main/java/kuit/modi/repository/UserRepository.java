package kuit.modi.repository;

import kuit.modi.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    Optional<Member> findByEmail(String email){
        return Optional.of(new Member("temp"));
    }
    boolean existsByNickname(String nickname){
        return true;
    }
}
