package com.chat.client;

import com.chat.dto.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LLMClient {
    Flux<String> stream(String message, List<Message> history);
}
