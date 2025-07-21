package kuit.modi.repository;

import kuit.modi.domain.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Long> {
}