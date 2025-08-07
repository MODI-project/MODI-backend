package kuit.modi.repository;

import kuit.modi.domain.Diary;
import kuit.modi.domain.Location;
import kuit.modi.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteAllByMemberId(Long memberId);
    List<Diary> findByMemberAndLocationOrderByDateDesc(Member member, Location location);
}