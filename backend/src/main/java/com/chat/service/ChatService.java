package com.chat.service;

import com.chat.dto.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final ChatModel chatModel;
    private final List<Message> history = new ArrayList<>();

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Flux<String> stream(String message) {
        List<org.springframework.ai.chat.messages.Message> springAiMessages = new ArrayList<>();

        for (Message msg : history) {
            if ("assistant".equals(msg.role())) {
                springAiMessages.add(new AssistantMessage(msg.content()));
            } else {
                springAiMessages.add(new UserMessage(msg.content()));
            }
        }
        springAiMessages.add(new UserMessage(message));

        Prompt prompt = new Prompt(springAiMessages);
        AtomicReference<StringBuilder> accumulated = new AtomicReference<>(new StringBuilder());

        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty())
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
                    String msg = e.getMessage() != null && e.getMessage().contains("429")
                            ? "[요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요]"
                            : "[오류가 발생했습니다]";
                    return Flux.just(msg);
                });
    }
}
