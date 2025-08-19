package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; // 필드명은 url이지만, 실제 내용은 key를 담고 있음.

    @OneToOne
    @JoinColumn(name = "diary_id", unique = true, nullable = false)
    private Diary diary;

    public static Image create(
            String url,
            Diary diary
    ){
        Image image = new Image();
        image.setUrl(url);
        image.setDiary(diary);

        return image;
    }
}