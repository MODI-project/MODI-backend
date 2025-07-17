package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "style")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Style {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String font;

    @OneToOne
    @JoinColumn(name = "diary_id", unique = true, nullable = false)
    private Diary diary;

    @ManyToOne
    @JoinColumn(name = "frame_id", nullable = false)
    private Frame frame;

    public static Style create(
            String font,
            Diary diary,
            Frame frame
    ){
        Style style = new Style();
        style.setFont(font);
        style.setDiary(diary);
        style.setFrame(frame);

        return style;
    }
}