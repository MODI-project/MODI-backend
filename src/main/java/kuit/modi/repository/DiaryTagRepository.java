package kuit.modi.repository;

import kuit.modi.domain.DiaryTag;
import kuit.modi.domain.DiaryTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, DiaryTagId> {

    // tagId로 매핑된 diaryId 전부 조회 (멤버 필터 포함)
    @Query("""
           select distinct dt.diary.id
           from DiaryTag dt
           where dt.tag.id = :tagId
             and dt.diary.member.id = :memberId
           """)
    List<Long> findDiaryIdsByTagIdAndMemberId(Long tagId, Long memberId);
}