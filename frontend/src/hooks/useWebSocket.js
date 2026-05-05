import { useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// In dev: '/ws' (Vite proxy)
// In prod: full backend URL + /ws
const WS_URL = import.meta.env.VITE_WS_URL
  || (import.meta.env.VITE_API_BASE_URL
    ? `${import.meta.env.VITE_API_BASE_URL}/ws`
    : '/ws')

export function useWebSocket(ticketId, onUpdate) {
  const clientRef = useRef(null)

  useEffect(() => {
    if (!ticketId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/complaint/${ticketId}`, (msg) => {
          try {
            const update = JSON.parse(msg.body)
            onUpdate(update)
          } catch {}
        })
      }
    })

    client.activate()
    clientRef.current = client

    return () => { client.deactivate() }
  }, [ticketId])
}