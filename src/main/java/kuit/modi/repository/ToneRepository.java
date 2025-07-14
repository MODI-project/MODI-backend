package kuit.modi.repository;

import kuit.modi.domain.Tone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToneRepository extends JpaRepository<Tone, Long> {
    Optional<Tone> findByName(String name);
}