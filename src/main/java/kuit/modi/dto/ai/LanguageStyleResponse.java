package kuit.modi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class LanguageStyleResponse {
    private List<String> styles;
}