package com.chat.service;

import com.chat.dto.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {

    private final ChatModel chatModel;

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public Flux<String> stream(String message, List<Message> history) {
        // 1. Spring AI 규격에 맞게 대화 기록 변환
        List<org.springframework.ai.chat.messages.Message> springAiMessages = new ArrayList<>();

        // 시스템 지침 설정
//        String systemInstructions = "당신은 친절한 AI 조언자입니다. 모든 답변은 50자 이내로 간결하게 답하세요.";
//        springAiMessages.add(new SystemMessage(systemInstructions));

        for (Message msg : history) {
            if ("assistant".equals(msg.role())) {
                springAiMessages.add(new AssistantMessage(msg.content()));
            } else {
                springAiMessages.add(new UserMessage(msg.content()));
            }
        }

        // 2. 현재 사용자의 새로운 메시지 추가
        springAiMessages.add(new UserMessage(message));

        // 3. Prompt 생성
        Prompt prompt = new Prompt(springAiMessages);
        AtomicReference<StringBuilder> accumulated = new AtomicReference<>(new StringBuilder());

        // 4. ChatModel을 이용한 스트리밍 호출
        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        // Spring AI 버전에 따라 getText() 또는 getContent()를 사용합니다.
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