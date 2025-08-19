package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diary")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)  // 요약 원본
    private String summary;

    // 언어스타일 적용된 요약, 이 값은 nullable함.(언어스타일 적용 안 할 수도 있기 때문)
    @Column(name = "summary_toned", length = 200)
    private String summary_toned;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean favorite = false;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "emotion_id", nullable = false)
    private Emotion emotion;

    @ManyToOne
    @JoinColumn(name = "tone_id", nullable = false)
    private Tone tone;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // mappedBy가 사용된 필드는 연관관계의 주인이 아님.(즉, diary가 이들의 FK 관리 안 함)
    // cascade = ALL, orphanRemoval = true는 Diary 삭제 시 관련 항목도 같이 삭제된다는 의미
    // cascade 설정이 있으면 여기서 저장하면 상대 테이블에도 같이 저장됨

    // Diary → DiaryTag: 1:N 단방향
    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryTag> diaryTags = new ArrayList<>();

    // Diary → Style: 1:1 (역방향은 Style에서 처리)
    @OneToOne(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Style style;

    // Diary → Image: 1:1 (역방향은 Image에서 처리)
    @OneToOne(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    public static Diary create(
            String content,
            String summary,
            String summary_toned,
            LocalDateTime date,
            Member member,
            Emotion emotion,
            Tone tone,
            Location location,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Diary diary = new Diary();
        diary.setContent(content);
        diary.setSummary(summary);
        diary.setSummary_toned(summary_toned);
        diary.setDate(date);
        diary.setMember(member);
        diary.setEmotion(emotion);
        diary.setTone(tone);
        diary.setLocation(location);
        diary.setCreatedAt(createdAt);
        diary.setUpdatedAt(updatedAt);
        diary.setFavorite(false); // 기본값

        return diary;
    }
}