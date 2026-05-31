import { useState } from 'react'
import { getLocation } from '../services/api'
import { ANIMALS } from '../constants/animals'

/**
 * Modal to start tracking another user.
 * User enters session ID + picks an animal avatar.
 *
 * Props:
 *   onTrack({ sessionId, animal, location }) — called on success
 *   onClose()                                — close without adding
 *   alreadyTracking                          — Set of currently tracked sessionIds
 */
export default function TrackModal({ onTrack, onClose, alreadyTracking }) {
  const [sessionId, setSessionId]   = useState('')
  const [animal, setAnimal]         = useState(ANIMALS[0])
  const [loading, setLoading]       = useState(false)
  const [error, setError]           = useState(null)

  async function handleSubmit() {
    const sid = sessionId.trim().toUpperCase()

    if (!sid) { setError('Please enter a session ID'); return }
    if (sid.length < 6) { setError('Session IDs are at least 6 characters'); return }
    if (alreadyTracking.has(sid)) { setError('You are already tracking this session'); return }

    setLoading(true)
    setError(null)

    try {
      // Fetch the initial location so we can center the map immediately
      const location = await getLocation(sid)
      onTrack({ sessionId: sid, animal, location })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="overlay">
      <div className="modal modal-wide">
        <h2 className="modal-title" style={{ fontSize: '1.2rem', marginBottom: 6 }}>
          Track Someone
        </h2>
        <p className="modal-subtitle" style={{ marginBottom: 20 }}>
          Enter their session ID and choose an avatar for their pin.
        </p>

        {error && <div className="error-msg">{error}</div>}

        <label className="input-label">Session ID</label>
        <input
          className="input"
          placeholder="e.g. AB12CD34"
          value={sessionId}
          onChange={e => setSessionId(e.target.value.toUpperCase())}
          onKeyDown={e => e.key === 'Enter' && handleSubmit()}
          maxLength={16}
          autoFocus
        />

        <span className="avatar-grid-label">Choose a pin avatar</span>
        <div className="avatar-grid">
          {ANIMALS.map(a => (
            <button
              key={a.id}
              className={`avatar-option ${animal.id === a.id ? 'selected' : ''}`}
              onClick={() => setAnimal(a)}
              title={a.name}
            >
              <span className="animal-emoji">{a.emoji}</span>
              <span className="animal-name">{a.name}</span>
            </button>
          ))}
        </div>

        <button
          className="btn btn-primary"
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading
            ? <><span className="spinner" /> Locating…</>
            : `${animal.emoji} Start Tracking`
          }
        </button>

        <button
          className="btn btn-secondary btn-gap"
          onClick={onClose}
        >
          Cancel
        </button>
      </div>
    </div>
  )
}
