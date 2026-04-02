import { useEffect, useRef, useState } from "react";
import { useChat } from "./hooks/useChat";
import ChatMessage from "./components/ChatMessage";
import ChatInput from "./components/ChatInput";
import "./App.css";

export default function App() {
  const { messages, streaming, sendMessage, stopStreaming, clearHistory } =
    useChat();
  const messagesRef = useRef(null);
  const bottomRef = useRef(null);
  const shouldAutoScrollRef = useRef(true);

  const [showScrollToBottom, setShowScrollToBottom] = useState(false);

  const handleMessagesScroll = () => {
    const container = messagesRef.current;
    if (!container) return;

    const distanceFromBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight;

    const isNearBottom = distanceFromBottom < 80;
    shouldAutoScrollRef.current = isNearBottom;

    setShowScrollToBottom(distanceFromBottom > 200);
  };

  const scrollToBottom = () => {
    shouldAutoScrollRef.current = true;
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const handleSendMessage = async (userInput) => {
    shouldAutoScrollRef.current = true;
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    await sendMessage(userInput);
  };

  useEffect(() => {
    if (!shouldAutoScrollRef.current) return;
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <div className="app">
      <header className="app__header">
        <h1>Gemini Chat</h1>
        <button
          className="app__clear-btn"
          onClick={clearHistory}
          disabled={streaming}
        >
          대화 초기화
        </button>
      </header>

      <main
        ref={messagesRef}
        className="app__messages"
        onScroll={handleMessagesScroll}
      >
        {messages.length === 0 && (
          <div className="app__empty">
            <p>무엇이든 물어보세요</p>
          </div>
        )}

        {messages.map((msg, i) => (
          <ChatMessage key={i} message={msg} />
        ))}

        <div ref={bottomRef} />
      </main>

      {showScrollToBottom && (
        <button
          className="scroll-to-bottom-btn"
          onClick={scrollToBottom}
          type="button"
          aria-label="맨 아래로 이동"
        >
          ↓
        </button>
      )}

      <footer className="app__footer">
        <ChatInput
          onSend={handleSendMessage}
          onStop={stopStreaming}
          streaming={streaming}
        />
      </footer>
    </div>
  );
}
