package kuit.modi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StyledSummaryRequest {
    private String style;
    private String summary;
}
