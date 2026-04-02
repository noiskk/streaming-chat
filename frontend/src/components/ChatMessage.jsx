import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeRaw from 'rehype-raw'

export default function ChatMessage({ message }) {
  const isUser = message.role === 'user'

  return (
    <div className={`message ${isUser ? 'message--user' : 'message--assistant'}`}>
      <div className="message__bubble">
        <span className="message__role">{isUser ? '나' : 'AI'}</span>
        <div className="message__content">
          <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeRaw]}>
            {message.content}
          </ReactMarkdown>
          {message.streaming && <span className="cursor">▍</span>}
        </div>
      </div>
    </div>
  )
}
