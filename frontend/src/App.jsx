import { useEffect, useRef } from 'react'
import { useChat } from './hooks/useChat'
import ChatMessage from './components/ChatMessage'
import ChatInput from './components/ChatInput'
import './App.css'

export default function App() {
  const { messages, streaming, sendMessage, stopStreaming, clearHistory } = useChat()
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  return (
    <div className="app">
      <header className="app__header">
        <h1>Gemini Chat</h1>
        <button className="app__clear-btn" onClick={clearHistory} disabled={streaming}>
          대화 초기화
        </button>
      </header>

      <main className="app__messages">
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

      <footer className="app__footer">
        <ChatInput onSend={sendMessage} onStop={stopStreaming} streaming={streaming} />
      </footer>
    </div>
  )
}
