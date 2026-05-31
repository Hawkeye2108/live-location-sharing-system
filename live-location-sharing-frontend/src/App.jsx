import { useState, useEffect, useCallback } from 'react'
import StartupModal  from './components/StartupModal.jsx'
import TrackModal    from './components/TrackModal.jsx'
import MapView       from './components/MapView.jsx'
import Hud           from './components/Hud.jsx'
import GpsBadge      from './components/GpsBadge.jsx'
import TrackedPanel  from './components/TrackedPanel.jsx'
import { useGps }    from './hooks/useGps.js'
import { useLocationBroadcast } from './hooks/useLocationBroadcast.js'
import { subscribeToSession, disconnectStomp } from './services/stompClient.js'

export default function App() {
  // ── Session state ────────────────────────────────────────────
  const [sessionId, setSessionId]         = useState(null)   // own host session ID
  const [showStartup, setShowStartup]     = useState(true)   // startup modal visibility
  const [showTrackModal, setShowTrackModal] = useState(false) // track-someone modal

  // ── GPS & broadcasting ───────────────────────────────────────
  const { position, gpsStatus }   = useGps()
  useLocationBroadcast(sessionId, position)  // auto-sends GPS every 3s

  // ── Tracked users: [{ sessionId, animal, location }] ─────────
  const [trackedUsers, setTrackedUsers]   = useState([])

  // ── Called when startup modal finishes ───────────────────────
  function handleSessionReady(sid) {
    setSessionId(sid)
    setShowStartup(false)
  }

  // ── Called when user wants to track someone ──────────────────
  const handleAddTracked = useCallback(async ({ sessionId: sid, animal, location }) => {
    // Add the entry immediately with the fetched initial location
    setTrackedUsers(prev => [...prev, {
      sessionId: sid,
      animal,
      location: { latitude: location.latitude, longitude: location.longitude },
    }])

    setShowTrackModal(false)

    // Subscribe to WebSocket for live updates
    await subscribeToSession(sid, (snapshot) => {
      setTrackedUsers(prev => prev.map(u =>
        u.sessionId === sid
          ? { ...u, location: { latitude: snapshot.latitude, longitude: snapshot.longitude } }
          : u
      ))
    })
  }, [])

  // ── Remove a tracked session ─────────────────────────────────
  function handleRemoveTracked(sid) {
    setTrackedUsers(prev => prev.filter(u => u.sessionId !== sid))
  }

  // ── Cleanup STOMP on unmount ──────────────────────────────────
  useEffect(() => {
    return () => disconnectStomp()
  }, [])

  // ── Set of currently tracked IDs (for duplicate prevention) ──
  const trackingSet = new Set(trackedUsers.map(u => u.sessionId))

  return (
    <>
      {/* ── Full-screen map (always mounted so it's ready) ── */}
      <MapView
        ownPosition={position}
        trackedUsers={trackedUsers}
      />

      {/* ── Startup modal (until session is created) ── */}
      {showStartup && (
        <StartupModal onReady={handleSessionReady} />
      )}

      {/* ── UI overlays shown only after session is created ── */}
      {!showStartup && sessionId && (
        <>
          {/* GPS status badge — top left */}
          <GpsBadge status={gpsStatus} />

          {/* Tracked users panel — top right */}
          <TrackedPanel
            tracked={trackedUsers}
            onRemove={handleRemoveTracked}
          />

          {/* Bottom HUD bar */}
          <Hud
            sessionId={sessionId}
            onTrackSomeone={() => setShowTrackModal(true)}
          />
        </>
      )}

      {/* ── Track-someone modal ── */}
      {showTrackModal && (
        <TrackModal
          onTrack={handleAddTracked}
          onClose={() => setShowTrackModal(false)}
          alreadyTracking={trackingSet}
        />
      )}
    </>
  )
}
