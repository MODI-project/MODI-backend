package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character_type")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CharacterType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // 사용할지 여부는 나중에 결정
}