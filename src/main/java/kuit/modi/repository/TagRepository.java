package kuit.modi.repository;

import kuit.modi.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findAllByNameIn(List<String> names);

    @Query("""
    SELECT t.name 
    FROM DiaryTag dt 
    JOIN dt.tag t 
    GROUP BY t.name 
    ORDER BY COUNT(t.name) DESC 
    """)
    List<String> findTopTagNames(Pageable pageable);

}
