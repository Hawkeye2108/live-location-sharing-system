import { useState } from 'react'

/**
 * Bottom floating bar visible on the map.
 * Shows: live dot | "Your Session:" | sessionId | copy | divider | "Track Someone" button
 *
 * Props:
 *   sessionId           — host's own session ID
 *   onTrackSomeone()    — opens the track modal
 */
export default function Hud({ sessionId, onTrackSomeone }) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(sessionId)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="hud">
      <div className="hud-session">
        <span className="hud-dot" />
        <span>Your Session:</span>
        <span className="hud-session-id">{sessionId}</span>
        <button
          className={`copy-btn ${copied ? 'copied' : ''}`}
          onClick={handleCopy}
          title="Copy session ID"
          style={{ padding: '4px 8px', fontSize: '0.85rem' }}
        >
          {copied ? '✓ Copied' : '⎘ Copy'}
        </button>
      </div>

      <div className="hud-divider" />

      <button className="hud-track-btn" onClick={onTrackSomeone}>
        + Track Someone
      </button>
    </div>
  )
}
