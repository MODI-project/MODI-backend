package kuit.modi.openai;

import kuit.modi.dto.ai.response.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    public String ask(String systemPrompt, String userPrompt) {

        WebClient client = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        return client.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAiResponse.class)
                .block()
                .getFirstMessage();
    }
}
