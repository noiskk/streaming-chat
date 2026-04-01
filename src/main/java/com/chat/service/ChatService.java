package com.chat.service;

import com.chat.client.LLMClient;
import com.chat.dto.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final LLMClient llmClient;

    public ChatService(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public Flux<String> stream(String message, List<Message> history) {
        AtomicReference<StringBuilder> accumulated = new AtomicReference<>(new StringBuilder());

        return llmClient.stream(message, history)
            .doOnNext(token -> accumulated.get().append(token))
            .doOnComplete(() -> {
                String assistantReply = accumulated.get().toString().trim();
                if (!assistantReply.isEmpty()) {
                    history.add(new Message("user", message));
                    history.add(new Message("assistant", assistantReply));
                }
            })
            .onErrorResume(e -> {
                System.err.println("=== LLM ERROR: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                String msg = e.getMessage() != null && e.getMessage().contains("429")
                    ? "[요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요]"
                    : "[오류가 발생했습니다]";
                return Flux.just(msg);
            });
    }
}
