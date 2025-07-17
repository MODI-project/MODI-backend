package kuit.modi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuit.modi.domain.Diary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
        return em.createQuery(
                        "SELECT d FROM Diary d " +
                                "LEFT JOIN FETCH d.emotion " +
                                "LEFT JOIN FETCH d.image " +
                                "LEFT JOIN FETCH d.diaryTags dt " +
                                "LEFT JOIN FETCH dt.tag " +
                                "WHERE d.member.id = :memberId AND d.date = :date " +
                                "ORDER BY d.createdAt ASC", Diary.class
                )
                .setParameter("memberId", memberId)
                .setParameter("date", date)
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

}
