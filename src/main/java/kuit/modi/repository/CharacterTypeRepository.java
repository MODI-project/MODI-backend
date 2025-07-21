package kuit.modi.repository;

import kuit.modi.domain.CharacterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterTypeRepository extends JpaRepository<CharacterType, Long> {
    Optional<CharacterType> findByName(String name);
}
