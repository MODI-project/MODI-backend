package kuit.modi.repository;

import kuit.modi.domain.Diary;
import kuit.modi.domain.Location;
import kuit.modi.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    void deleteAllByMemberId(Long memberId);
    List<Diary> findByMemberAndLocationOrderByDateDesc(Member member, Location location);
    // === 커서 기반 페이징 추가 === //
    @Query("""
        SELECT d FROM Diary d
        WHERE d.member.id = :memberId
          AND d.location.id = :locationId
          AND (
                :cursorCreatedAt IS NULL
                OR (
                     d.createdAt < :cursorCreatedAt
                     OR (d.createdAt = :cursorCreatedAt AND d.id < :cursorId)
                   )
              )
        ORDER BY d.createdAt DESC, d.id DESC
    """)
    List<Diary> findPagedDiaries(
            @Param("memberId") Long memberId,
            @Param("locationId") Long locationId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}