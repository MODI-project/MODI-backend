package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "monthly_emotion_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "emotion_id", "month_start_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MonthlyEmotionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emotion_id", nullable = false)
    private Emotion emotion;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(nullable = false)
    private int count;
}