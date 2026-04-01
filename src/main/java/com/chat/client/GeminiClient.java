package com.chat.client;

import com.chat.dto.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Primary
@Profile("!dummy")
public class GeminiClient implements LLMClient {

    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/{model}:streamGenerateContent?alt=sse&key={apiKey}";

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiClient(WebClient webClient,
                        @Value("${gemini.api.key}") String apiKey,
                        @Value("${gemini.api.model}") String model) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public Flux<String> stream(String message, List<Message> history) {
        List<Map<String, Object>> contents = buildContents(message, history);
        Map<String, Object> requestBody = Map.of("contents", contents);

        return webClient.post()
            .uri(ENDPOINT, model, apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(status -> status.isError(), response ->
                response.bodyToMono(String.class)
                    .doOnNext(body -> System.err.println("=== Gemini API Error Body: " + body))
                    .flatMap(body -> Mono.error(new RuntimeException(response.statusCode() + ": " + body)))
            )
            .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
            .mapNotNull(ServerSentEvent::data)
            .filter(data -> !data.isBlank() && !data.equals("[DONE]"))
            .mapNotNull(this::extractText)
            .filter(text -> !text.isBlank());
    }

    private List<Map<String, Object>> buildContents(String message, List<Message> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        for (Message msg : history) {
            String role = msg.role().equals("assistant") ? "model" : "user";
            contents.add(Map.of(
                "role", role,
                "parts", List.of(Map.of("text", msg.content()))
            ));
        }

        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", message))
        ));

        return contents;
    }

    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode text = root
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");
            return text.isMissingNode() ? null : text.asText();
        } catch (Exception e) {
            return null;
        }
    }
}
