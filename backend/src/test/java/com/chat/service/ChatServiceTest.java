package com.chat.service;

import com.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private ChatService chatService;

    @Mock
    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatModel);
    }

    @Test
    void testStreamSuccess() {
        // Mock AI Response
        Generation generation = new Generation("Hello from AI");
        ChatResponse response = new ChatResponse(java.util.List.of(generation));
        
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(response));

        // Test ChatService.stream()
        Flux<String> result = chatService.stream("Hi");

        StepVerifier.create(result)
                .expectNext("Hello from AI")
                .verifyComplete();
    }

    @Test
    void testStreamEmptyResponse() {
        // Mock Empty AI Response
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.empty());

        Flux<String> result = chatService.stream("Hi");

        StepVerifier.create(result)
                .verifyComplete();
    }
}
