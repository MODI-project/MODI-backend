package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 생성 일시
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 주소(동)
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    // 최근 방문 날짜
    @Column(nullable = false)
    private LocalDateTime lastVisit;

    // 가장 최근 기록의 감정
    @ManyToOne
    @JoinColumn(name = "emotion_id", nullable = false)
    private Emotion emotion;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
