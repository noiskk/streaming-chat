import { useState, useRef, useCallback } from 'react'

const API_URL = '/chat'

export function useChat() {
  const [messages, setMessages] = useState([])
  const [streaming, setStreaming] = useState(false)
  const abortRef = useRef(null)

  const sendMessage = useCallback(async (userInput) => {
    if (!userInput.trim() || streaming) return

    const userMessage = { role: 'user', content: userInput }
    setMessages(prev => [...prev, userMessage, { role: 'assistant', content: '', streaming: true }])
    setStreaming(true)

    const controller = new AbortController()
    abortRef.current = controller

    let accumulated = ''
    let lineBuffer = ''

    try {
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userInput }),
        signal: controller.signal,
      })

      const reader = res.body.getReader()
      const decoder = new TextDecoder()

      while (true) {
        const { value, done } = await reader.read()
        if (done) break

        lineBuffer += decoder.decode(value, { stream: true })
        const lines = lineBuffer.split('\n')
        lineBuffer = lines.pop()

        let eventDataLines = []
        for (const line of lines) {
          if (line.startsWith('data:')) {
            eventDataLines.push(line.slice(5))
          } else if (line.trim() === '' && eventDataLines.length > 0) {
            const token = eventDataLines.join('\n')
            eventDataLines = []
            if (!token.trim()) continue

            accumulated += token
            setMessages(prev => {
              const next = [...prev]
              next[next.length - 1] = { role: 'assistant', content: accumulated, streaming: true }
              return next
            })
          }
        }
      }

      setMessages(prev => {
        const next = [...prev]
        next[next.length - 1] = { role: 'assistant', content: accumulated, streaming: false }
        return next
      })
    } catch (err) {
      if (err.name !== 'AbortError') {
        setMessages(prev => {
          const next = [...prev]
          next[next.length - 1] = { role: 'assistant', content: '[오류가 발생했습니다]', streaming: false }
          return next
        })
      }
    } finally {
      setStreaming(false)
    }
  }, [streaming])

  const stopStreaming = useCallback(() => {
    abortRef.current?.abort()
  }, [])

  const clearHistory = useCallback(() => {
    setMessages([])
  }, [])

  return { messages, streaming, sendMessage, stopStreaming, clearHistory }
}
