const BASE = import.meta.env.VITE_API_BASE_URL || ''

/**
 * Create a new sharing session.
 * POST /session/create
 * @returns {Promise<string>} sessionId e.g. "AB12CD34"
 */
export async function createSession() {
  const res = await fetch(`${BASE}/session/create`, { method: 'POST' })
  if (!res.ok) throw new Error(`Failed to create session: ${res.status}`)
  const data = await res.json()
  return data.sessionId
}

/**
 * Send a GPS location update as the host.
 * POST /session/{sessionId}/location
 * @param {string} sessionId
 * @param {number} latitude
 * @param {number} longitude
 */
export async function updateLocation(sessionId, latitude, longitude) {
  const res = await fetch(`${BASE}/session/${sessionId}/location`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ latitude, longitude }),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error(body.message || `Update failed: ${res.status}`)
  }
  return res.json()
}

/**
 * Fetch latest location for a session (REST fallback).
 * GET /session/{sessionId}/location
 * @param {string} sessionId
 * @returns {Promise<{latitude, longitude, lastUpdated}>}
 */
export async function getLocation(sessionId) {
  const res = await fetch(`${BASE}/session/${sessionId}/location`)
  if (res.status === 404) throw new Error('Session not found or no location yet')
  if (res.status === 410) throw new Error('Session has expired')
  if (!res.ok) throw new Error(`Fetch failed: ${res.status}`)
  return res.json()
}
