package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "monthly_tag_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "tag_id", "month_start_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MonthlyTagStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(nullable = false)
    private int count;

    @Column(name = "last_used_at", nullable = false)
    private LocalDate lastUsedAt;
}