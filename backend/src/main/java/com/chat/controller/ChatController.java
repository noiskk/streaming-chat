package com.chat.controller;

import com.chat.dto.ChatRequest;
import com.chat.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    // [테스트 시나리오 1] 하드코딩된 시크릿 키 추가 (소나큐브 탐지 대상)
    private static final String FAKE_AWS_KEY = "AKIAIOSFODNN7EXAMPLE";

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        return chatService.stream(request.message());
    }
}
