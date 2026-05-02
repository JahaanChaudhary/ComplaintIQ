import { useEffect, useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useWebSocket(ticketId, onUpdate) {
  const clientRef = useRef(null)

  useEffect(() => {
    if (!ticketId) return

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
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
