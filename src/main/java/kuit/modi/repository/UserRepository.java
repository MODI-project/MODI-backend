package kuit.modi.repository;

import kuit.modi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    Optional<User> findByEmail(String email){
        return Optional.of(new User("temp"));
    }
    boolean existsByNickname(String nickname){
        return true;
    }
}
