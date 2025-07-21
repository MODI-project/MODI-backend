package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diary_tag")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(DiaryTagId.class)
public class DiaryTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static DiaryTag create(
            Diary diary,
            Tag tag
    ){
        DiaryTag diaryTag = new DiaryTag();
        diaryTag.setDiary(diary);
        diaryTag.setTag(tag);

        return diaryTag;
    }
}