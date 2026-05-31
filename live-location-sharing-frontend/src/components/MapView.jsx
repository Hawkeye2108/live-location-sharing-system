import { useEffect, useRef } from 'react'
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Fix Leaflet's broken default icon URLs when bundled with Vite
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl:       'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl:     'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

/**
 * Creates a custom Leaflet DivIcon with an animal emoji bubble and label.
 *
 * @param {string}  emoji    — animal emoji
 * @param {string}  label    — text shown below the pin
 * @param {boolean} isOwn    — renders a blue ring for the host's own pin
 */
function makeAnimalIcon(emoji, label, isOwn = false) {
  const html = `
    <div class="map-pin">
      <div class="map-pin-bubble ${isOwn ? 'own' : ''}">${emoji}</div>
      <div class="map-pin-tail"></div>
      <div class="map-pin-label">${label}</div>
    </div>
  `
  return L.divIcon({
    html,
    className:   '',   // prevent Leaflet's default white box
    iconSize:    [60, 65],
    iconAnchor:  [30, 55],
    popupAnchor: [0, -55],
  })
}

// ── Sub-component: smoothly pan map to a new center ───────────
function MapPanner({ center }) {
  const map = useMap()
  const prevCenter = useRef(null)

  useEffect(() => {
    if (!center) return
    const { lat, lng } = center

    if (!prevCenter.current) {
      // First position: fly to it
      map.setView([lat, lng], 15)
    } else {
      // Subsequent: smooth pan (no zoom change)
      map.panTo([lat, lng], { animate: true, duration: 0.8 })
    }
    prevCenter.current = center
  }, [center, map])

  return null
}

/**
 * Full-screen map with:
 *  - OpenStreetMap tiles
 *  - Own position pin (📍, blue ring)
 *  - One animal pin per tracked session
 *
 * Props:
 *   ownPosition   — { lat, lng } | null
 *   trackedUsers  — [{ sessionId, animal, location: { latitude, longitude } }]
 */
export default function MapView({ ownPosition, trackedUsers }) {
  // Default center: New Delhi (fallback before GPS kicks in)
  const defaultCenter = [28.6139, 77.209]

  const ownIcon = makeAnimalIcon('📍', 'You', true)

  return (
    <div className="map-container">
      <MapContainer
        center={defaultCenter}
        zoom={13}
        zoomControl={true}
        scrollWheelZoom={true}
        style={{ height: '100%', width: '100%' }}
      >
        {/* OpenStreetMap tiles — completely free, no API key needed */}
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          maxZoom={19}
        />

        {/* Own GPS position */}
        {ownPosition && (
          <>
            <MapPanner center={ownPosition} />
            <Marker
              position={[ownPosition.lat, ownPosition.lng]}
              icon={ownIcon}
            />
          </>
        )}

        {/* Tracked users */}
        {trackedUsers.map(user => {
          const loc = user.location
          if (!loc?.latitude || !loc?.longitude) return null

          const icon = makeAnimalIcon(
            user.animal.emoji,
            user.animal.name,
            false
          )

          return (
            <Marker
              key={user.sessionId}
              position={[loc.latitude, loc.longitude]}
              icon={icon}
            />
          )
        })}
      </MapContainer>
    </div>
  )
}
