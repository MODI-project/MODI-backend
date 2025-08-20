package kuit.modi.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LanguageStyleRequest {
    private String emotion;
    private String content;
}
