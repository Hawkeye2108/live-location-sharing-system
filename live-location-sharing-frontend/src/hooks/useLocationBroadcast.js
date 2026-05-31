import { useEffect, useRef } from 'react'
import { updateLocation } from '../services/api'

const BROADCAST_INTERVAL_MS = 3000 // send every 3 seconds

/**
 * Broadcasts the host's GPS position to the backend on a fixed interval.
 * Only runs when both sessionId and position are available.
 *
 * @param {string|null}  sessionId  — host's own session ID
 * @param {{lat,lng}|null} position — current GPS position from useGps
 */
export function useLocationBroadcast(sessionId, position) {
  // Hold the latest position in a ref so the interval always uses fresh data
  const positionRef = useRef(position)

  useEffect(() => {
    positionRef.current = position
  }, [position])

  useEffect(() => {
    if (!sessionId || !positionRef.current) return

    const intervalId = setInterval(async () => {
      const pos = positionRef.current
      if (!pos) return

      try {
        await updateLocation(sessionId, pos.lat, pos.lng)
        console.debug(`[broadcast] lat=${pos.lat.toFixed(5)} lng=${pos.lng.toFixed(5)}`)
      } catch (err) {
        // Non-fatal: log and keep trying on next tick
        console.error('[broadcast] Failed to send location:', err.message)
      }
    }, BROADCAST_INTERVAL_MS)

    return () => clearInterval(intervalId)
  }, [sessionId]) // only re-subscribe if sessionId changes
}
