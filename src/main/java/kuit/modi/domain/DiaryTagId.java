package kuit.modi.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryTagId implements Serializable {

    private Long diary;
    private Long tag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiaryTagId)) return false;
        DiaryTagId that = (DiaryTagId) o;
        return Objects.equals(diary, that.diary) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diary, tag);
    }
}