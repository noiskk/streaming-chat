import { useState } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeRaw from 'rehype-raw'

function stabilizeStreamingMarkdown(content, streaming) {
  if (!streaming) return content

  const fenceMatches = content.match(/```/g) ?? []

  // Streaming chunks often arrive before the closing fence, which breaks
  // the markdown parser for the whole block. Close it temporarily for render only.
  if (fenceMatches.length % 2 === 1) {
    return `${content}\n\`\`\``
  }

  return content
}

export default function ChatMessage({ message }) {
  const isUser = message.role === 'user'
  const [copied, setCopied] = useState(false)
  const renderedContent = isUser
    ? message.content
    : stabilizeStreamingMarkdown(message.content, message.streaming)

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(message.content)
      setCopied(true)
      window.setTimeout(() => setCopied(false), 1500)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className={`message ${isUser ? 'message--user' : 'message--assistant'}`}>
      <div className="message__bubble">
        <div className="message__meta">
          <span className="message__role">{isUser ? '나' : 'AI'}</span>
        </div>
        <div className="message__content">
          <ReactMarkdown remarkPlugins={[remarkGfm]} rehypePlugins={[rehypeRaw]}>
            {renderedContent}
          </ReactMarkdown>
          {message.streaming && <span className="cursor">▍</span>}
        </div>
        {!isUser && (
          <div className="message__actions">
            <button
              type="button"
              className="message__icon-btn"
              onClick={handleCopy}
              disabled={!message.content}
              aria-label={copied ? '복사됨' : '답변 복사'}
              title={copied ? '복사됨' : '답변 복사'}
            >
              {copied ? (
                <svg viewBox="0 0 20 20" aria-hidden="true">
                  <path
                    d="M7.5 13.2 4.8 10.5l-1.1 1.1 3.8 3.8 8-8-1.1-1.1z"
                    fill="currentColor"
                  />
                </svg>
              ) : (
                <svg viewBox="0 0 20 20" aria-hidden="true">
                  <path
                    d="M6 2.5A2.5 2.5 0 0 0 3.5 5v8A2.5 2.5 0 0 0 6 15.5h6A2.5 2.5 0 0 0 14.5 13V5A2.5 2.5 0 0 0 12 2.5zm0 1.5h6c.55 0 1 .45 1 1v8c0 .55-.45 1-1 1H6c-.55 0-1-.45-1-1V5c0-.55.45-1 1-1"
                    fill="currentColor"
                  />
                  <path
                    d="M9 16.5h5A2.5 2.5 0 0 0 16.5 14V7h-1.5v7c0 .55-.45 1-1 1H9z"
                    fill="currentColor"
                  />
                </svg>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
