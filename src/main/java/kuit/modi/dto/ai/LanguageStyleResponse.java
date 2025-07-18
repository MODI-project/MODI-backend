package kuit.modi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class LanguageStyleResponse {
    private List<String> styles;
}