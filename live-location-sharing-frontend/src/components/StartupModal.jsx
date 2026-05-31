import { useState } from 'react'
import { createSession } from '../services/api'

/**
 * Shown on first load.
 * User clicks "Create My Session" → API call → sessionId displayed → "Enter the Map"
 *
 * Props:
 *   onReady(sessionId) — called when user is ready to start sharing
 */
export default function StartupModal({ onReady }) {
  const [step, setStep]         = useState('intro')   // 'intro' | 'created'
  const [sessionId, setSessionId] = useState(null)
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)
  const [copied, setCopied]     = useState(false)

  async function handleCreate() {
    setLoading(true)
    setError(null)
    try {
      const id = await createSession()
      setSessionId(id)
      setStep('created')
    } catch (err) {
      setError(err.message || 'Failed to create session. Is the backend running?')
    } finally {
      setLoading(false)
    }
  }

  function handleCopy() {
    navigator.clipboard.writeText(sessionId)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="overlay">
      <div className="modal">
        <div className="modal-logo">📍</div>
        <h1 className="modal-title">LiveTrack</h1>
        <p className="modal-subtitle">
          Share your live location with anyone using a session code.
          No accounts, no sign-ups.
        </p>

        {error && <div className="error-msg">{error}</div>}

        {step === 'intro' && (
          <button
            className="btn btn-primary"
            onClick={handleCreate}
            disabled={loading}
          >
            {loading ? <><span className="spinner" /> Creating session…</> : '🚀 Create My Session'}
          </button>
        )}

        {step === 'created' && sessionId && (
          <>
            <div className="session-box">
              <div>
                <div className="session-label">Your Session ID</div>
                <div className="session-id">{sessionId}</div>
              </div>
              <button
                className={`copy-btn ${copied ? 'copied' : ''}`}
                onClick={handleCopy}
                title="Copy to clipboard"
              >
                {copied ? '✓' : '⎘'}
              </button>
            </div>

            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 20, lineHeight: 1.5 }}>
              Share this code with people you want to let track you.
              Your GPS will broadcast while the map is open.
            </p>

            <button
              className="btn btn-primary"
              onClick={() => onReady(sessionId)}
            >
              🗺️ Enter the Map
            </button>
          </>
        )}
      </div>
    </div>
  )
}
