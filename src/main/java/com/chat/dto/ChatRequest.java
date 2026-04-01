package com.chat.dto;

import java.util.List;

public record ChatRequest(String message, List<Message> history) {}
