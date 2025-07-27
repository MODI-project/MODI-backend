package kuit.modi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuit.modi.domain.Diary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DiaryQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /*
     * diaryId에 해당하는 Diary를 연관 엔티티와 함께 조회
     * - Emotion, Tone, Location, Image, Tag 포함 fetch
     */
    public Optional<Diary> findDetailById(Long diaryId) {
        List<Diary> result = em.createQuery(
                        "SELECT DISTINCT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.tone " +
                                "LEFT JOIN FETCH d.location " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.id = :diaryId", Diary.class)
                .setParameter("diaryId", diaryId)
                .getResultList();

        return result.stream().findFirst();
    }

    /*
     * 특정 날짜의 일기를 createdAt 기준으로 정렬하여 조회
     */
    public List<Diary> findDiariesByDate(Long memberId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.member.id = :memberId AND d.date BETWEEN :start AND :end " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getResultList();
    }

    /*
     * 특정 연&월의 일기 목록을 시간 기준으로 정렬하여 조회
     * - Image와 Emotion 포함 fetch
     * - 작성 순 정렬
     */
    public List<Diary> findByYearMonth(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.emotion " +
                                "WHERE d.member.id = :memberId " +
                                "AND FUNCTION('YEAR', d.date) = :year " +
                                "AND FUNCTION('MONTH', d.date) = :month " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //즐겨찾기한 일기 목록 조회 - 로그인한 사용자의 favorite=true 조건
    public List<Diary> findFavorites(Long memberId) {
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.image " +
                                "WHERE d.member.id = :memberId " +
                                "AND d.favorite = true " +
                                "ORDER BY d.createdAt DESC", Diary.class
                )
                .setParameter("memberId", memberId)
                .getResultList();
    }

    //감정별 통계 조
    public List<Object[]> findEmotionStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT e.name, COUNT(d) FROM Diary d " +
                                "JOIN d.emotion e " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY e.name ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

   //어투별 통계 조회
    public List<Object[]> findToneStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT t.name, COUNT(d) FROM Diary d " +
                                "JOIN d.tone t " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY t.name ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //위치별 통계 조회
    public List<Object[]> findLocationStats(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT l.address, COUNT(d) FROM Diary d " +
                                "JOIN d.location l " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
                                "GROUP BY l.address ORDER BY COUNT(d) DESC", Object[].class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }

    //전체 일기 수 카운트
    public long countMonthlyDiaries(Long memberId, int year, int month) {
        return em.createQuery(
                        "SELECT COUNT(d) FROM Diary d " +
                                "WHERE d.member.id = :memberId " +
                                "AND YEAR(d.date) = :year AND MONTH(d.date) = :month", Long.class
                )
                .setParameter("memberId", memberId)
                .setParameter("year", year)
                .setParameter("month", month)
                .getSingleResult();
    }

    //특정 태그에 해당하는 사용자의 일기 조회- 작성일 기준 오름차순
    public List<Object[]> findByTagIdWithImage(Long memberId, Long tagId) {
        return em.createQuery(
                        "SELECT d.date, i.url FROM Diary d " +
                                "JOIN d.image i " +
                                "JOIN d.diaryTags dt " +
                                "WHERE d.member.id = :memberId " +
                                "AND dt.tag.id = :tagId " +
                                "ORDER BY d.date ASC, d.createdAt ASC", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("tagId", tagId)
                .getResultList();
    }


}
