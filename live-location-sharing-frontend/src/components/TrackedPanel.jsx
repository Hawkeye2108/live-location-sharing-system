import { unsubscribeFromSession } from '../services/stompClient'

/**
 * Floating panel (top-right) showing all currently tracked sessions.
 * Each card shows animal avatar, session ID, live status, and a remove button.
 *
 * Props:
 *   tracked  — array of { sessionId, animal, location }
 *   onRemove(sessionId) — called when user removes a tracked session
 */
export default function TrackedPanel({ tracked, onRemove }) {
  if (tracked.length === 0) return null

  function handleRemove(sessionId) {
    unsubscribeFromSession(sessionId)
    onRemove(sessionId)
  }

  return (
    <div className="tracked-panel">
      <span className="tracked-panel-title">Tracking ({tracked.length})</span>
      {tracked.map(t => (
        <div key={t.sessionId} className="tracked-card">
          <span className="tracked-avatar">{t.animal.emoji}</span>
          <div className="tracked-info">
            <div className="tracked-name">{t.animal.name}</div>
            <div className="tracked-sid">{t.sessionId}</div>
            <div className="tracked-status">
              <span className="tracked-status-dot" />
              Live
            </div>
          </div>
          <button
            className="btn btn-danger"
            onClick={() => handleRemove(t.sessionId)}
            title="Stop tracking"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  )
}
