import { Client } from '@stomp/stompjs'
// import SockJS from 'sockjs-client'
import SockJS from 'sockjs-client/dist/sockjs.min.js'

let stompClient = null
// Map of sessionId → STOMP subscription object
const subscriptions = new Map()

/**
 * Initialises and returns a connected STOMP client (singleton).
 * Reuses an existing active connection if one already exists.
 */
function getClient() {
  return new Promise((resolve, reject) => {
    // Reuse if already connected
    if (stompClient && stompClient.connected) {
      resolve(stompClient)
      return
    }

    const client = new Client({
      // SockJS provides the WebSocket transport with fallbacks
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,

      onConnect: () => {
        console.log('[STOMP] Connected')
        stompClient = client
        resolve(client)
      },

      onStompError: (frame) => {
        console.error('[STOMP] Error:', frame)
        reject(new Error(frame.headers?.message || 'STOMP error'))
      },

      onDisconnect: () => {
        console.log('[STOMP] Disconnected')
      },
    })

    client.activate()
  })
}

/**
 * Subscribe to live location updates for a session.
 * Calls onLocation({ latitude, longitude, lastUpdated }) whenever a push arrives.
 *
 * @param {string}   sessionId
 * @param {Function} onLocation  callback(locationSnapshot)
 */
export async function subscribeToSession(sessionId, onLocation) {
  // If already subscribed, skip
  if (subscriptions.has(sessionId)) return

  const client = await getClient()
  const topic = `/topic/session/${sessionId}`

  const sub = client.subscribe(topic, (message) => {
    try {
      const snapshot = JSON.parse(message.body)
      onLocation(snapshot)
    } catch (err) {
      console.error('[STOMP] Failed to parse message:', err)
    }
  })

  subscriptions.set(sessionId, sub)
  console.log(`[STOMP] Subscribed to ${topic}`)
}

/**
 * Unsubscribe from a session topic and clean up.
 * @param {string} sessionId
 */
export function unsubscribeFromSession(sessionId) {
  const sub = subscriptions.get(sessionId)
  if (sub) {
    sub.unsubscribe()
    subscriptions.delete(sessionId)
    console.log(`[STOMP] Unsubscribed from session ${sessionId}`)
  }
}

/**
 * Disconnect the STOMP client entirely (e.g. on app unmount).
 */
export function disconnectStomp() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
    subscriptions.clear()
  }
}
