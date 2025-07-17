package kuit.modi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kuit.modi.domain.Diary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//일기 상세 조회 레포
@Repository
public class DiaryQueryRepository {

    @PersistenceContext
    private EntityManager em;

    /*
     * diaryId에 해당하는 Diary를 연관 엔티티와 함께 조회합니다.
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
}
