import { useState } from 'react'

export default function ChatInput({ onSend, onStop, streaming }) {
  const [input, setInput] = useState('')

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!input.trim()) return
    onSend(input)
    setInput('')
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSubmit(e)
    }
  }

  return (
    <form className="chat-input" onSubmit={handleSubmit}>
      <textarea
        className="chat-input__textarea"
        value={input}
        onChange={e => setInput(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="메시지를 입력하세요... (Enter: 전송, Shift+Enter: 줄바꿈)"
        rows={1}
        disabled={streaming}
      />
      {streaming ? (
        <button type="button" className="chat-input__btn chat-input__btn--stop" onClick={onStop}>
          중지
        </button>
      ) : (
        <button type="submit" className="chat-input__btn" disabled={!input.trim()}>
          전송
        </button>
      )}
    </form>
  )
}
