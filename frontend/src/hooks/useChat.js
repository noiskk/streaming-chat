import { useState, useRef, useCallback } from 'react'

const API_URL = '/chat'

export function useChat() {
  const [messages, setMessages] = useState([])
  const [streaming, setStreaming] = useState(false)
  const historyRef = useRef([])
  const abortRef = useRef(null)

  const sendMessage = useCallback(async (userInput) => {
    if (!userInput.trim() || streaming) return

    const userMessage = { role: 'user', content: userInput }
    setMessages(prev => [...prev, userMessage, { role: 'assistant', content: '', streaming: true }])
    setStreaming(true)

    const controller = new AbortController()
    abortRef.current = controller

    let accumulated = ''

    try {
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userInput, history: historyRef.current }),
        signal: controller.signal,
      })

      const reader = res.body.getReader()
      const decoder = new TextDecoder()

      while (true) {
        const { value, done } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const token = line.slice(5)
            accumulated += token
            setMessages(prev => {
              const next = [...prev]
              next[next.length - 1] = { role: 'assistant', content: accumulated, streaming: true }
              return next
            })
          }
        }
      }

      historyRef.current = [
        ...historyRef.current,
        { role: 'user', content: userInput },
        { role: 'assistant', content: accumulated },
      ]

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
    historyRef.current = []
  }, [])

  return { messages, streaming, sendMessage, stopStreaming, clearHistory }
}
