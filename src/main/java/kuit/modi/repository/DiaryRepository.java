package kuit.modi.repository;

import kuit.modi.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteAllByMemberId(Long memberId);
}