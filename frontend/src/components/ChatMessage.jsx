export default function ChatMessage({ message }) {
  const isUser = message.role === 'user'

  return (
    <div className={`message ${isUser ? 'message--user' : 'message--assistant'}`}>
      <div className="message__bubble">
        <span className="message__role">{isUser ? '나' : 'AI'}</span>
        <p className="message__content">
          {message.content}
          {message.streaming && <span className="cursor">▍</span>}
        </p>
      </div>
    </div>
  )
}
