package kuit.modi.repository;

import kuit.modi.domain.Frame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FrameRepository extends JpaRepository<Frame, Long> {
    Optional<Frame> findByName(String name);
}