package com.chat.client;

import com.chat.dto.Message;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@Profile("dummy")
public class DummyLLMClient implements LLMClient {

    private static final Map<String, String> RESPONSES = Map.of(
        "안녕", "안녕하세요! 저는 Gemini 챗봇입니다. 무엇을 도와드릴까요?",
        "날씨", "오늘 날씨는 맑고 기온은 18도입니다. 나들이 가기 좋은 날씨네요!",
        "자바", "Java는 1995년 Sun Microsystems에서 만든 객체지향 프로그래밍 언어입니다. JVM 위에서 동작하며 플랫폼 독립성이 특징입니다.",
        "스프링", "Spring Framework는 Java 기반의 엔터프라이즈 애플리케이션 개발을 위한 프레임워크입니다. IoC, DI, AOP 등의 핵심 개념을 제공합니다.",
        "웹플럭스", "Spring WebFlux는 리액티브 스트림 기반의 비동기 논블로킹 웹 프레임워크입니다. Reactor 라이브러리를 사용하며 높은 동시성을 지원합니다.",
        "리액터", "Project Reactor는 JVM 기반의 리액티브 프로그래밍 라이브러리입니다. Flux와 Mono 두 가지 퍼블리셔를 제공합니다.",
        "gemini", "Gemini는 Google DeepMind가 개발한 멀티모달 AI 모델입니다. 텍스트, 이미지, 오디오 등 다양한 형태의 입력을 처리할 수 있습니다."
    );

    private static final String DEFAULT_RESPONSE =
        "죄송합니다. 해당 질문에 대한 답변을 준비하지 못했습니다. 다른 질문을 해주세요!";

    @Override
    public Flux<String> stream(String message, List<Message> history) {
        String response = RESPONSES.entrySet().stream()
            .filter(entry -> message.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(DEFAULT_RESPONSE);

        String[] words = response.split(" ");

        return Flux.fromArray(words)
            .delayElements(Duration.ofMillis(100))
            .map(word -> word + " ");
    }
}
