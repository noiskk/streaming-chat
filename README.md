# Gemini Streaming Chat

Google Gemini 응답을 실시간으로 받아보는 스트리밍 채팅 예제입니다.  
상위 폴더 기준으로 `frontend`와 `backend`가 함께 구성되어 있으며, 프런트는 React + Vite, 백엔드는 Spring Boot WebFlux + Spring AI로 작성되어 있습니다.

## 주요 기능

- Gemini 응답 스트리밍 표시
- 이전 대화 이력을 포함한 멀티턴 채팅
- Markdown 렌더링
- 답변 복사 아이콘 버튼
- 스트리밍 중 응답 중지
- 대화 초기화

## 기술 스택

- Frontend: React 19, Vite, react-markdown, remark-gfm, rehype-raw
- Backend: Java 17, Spring Boot 3, Spring WebFlux, Spring AI, Google GenAI

## 프로젝트 구조

```text
streaming-chat/
├─ frontend/
│  ├─ src/
│  │  ├─ components/
│  │  ├─ hooks/
│  │  ├─ App.jsx
│  │  └─ App.css
│  ├─ package.json
│  └─ vite.config.js
└─ backend/
   ├─ src/main/java/com/chat/
   │  ├─ config/
   │  ├─ controller/
   │  ├─ dto/
   │  └─ service/
   ├─ src/main/resources/application.yml
   └─ build.gradle
```

## 동작 방식

1. 사용자가 프런트에서 메시지를 입력하면 `/chat`으로 `POST` 요청을 보냅니다.
2. 요청 본문에는 현재 사용자 메시지와 이전 대화 이력이 함께 포함됩니다.
3. 백엔드는 이력을 Spring AI 메시지 형식으로 변환한 뒤 Gemini 모델에 전달합니다.
4. Gemini 응답은 `text/event-stream` 형식으로 스트리밍됩니다.
5. 프런트는 응답 청크를 순서대로 읽어 assistant 메시지에 누적하고, Markdown으로 렌더링합니다.

## 실행 환경

- Node.js 18 이상
- npm
- Java 17

## 실행 방법

### 1. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

- 기본 포트: `8080`

### 2. 프런트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

- 기본 포트: `3000`

브라우저에서 `http://localhost:3000`으로 접속하면 됩니다.

## 설정

Gemini 설정은 `backend/src/main/resources/application.yml`에서 읽습니다.

```yml
spring:
  ai:
    google:
      genai:
        api-key: YOUR_API_KEY
        chat:
          options:
            model: gemini-2.5-flash
```

## 구현 포인트

- 프런트는 `useChat` 훅에서 스트리밍 응답을 직접 읽어 누적합니다.
- 사용자가 스크롤을 올려서 읽는 중이면 자동 스크롤이 강제로 내려가지 않습니다.
- assistant 메시지 아래에 복사 아이콘 버튼을 제공해 답변을 바로 복사할 수 있습니다.
- 백엔드 `/chat` 엔드포인트는 `POST` + `text/event-stream` 방식으로 동작합니다.
