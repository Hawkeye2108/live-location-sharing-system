import { useState, useEffect, useRef } from 'react'

/**
 * Continuously watches the device GPS position.
 *
 * Returns:
 *   position  — { lat, lng } | null
 *   gpsStatus — 'waiting' | 'active' | 'denied' | 'unavailable'
 *   error     — string | null
 */
export function useGps() {
  const [position, setPosition]   = useState(null)
  const [gpsStatus, setGpsStatus] = useState('waiting')
  const [error, setError]         = useState(null)
  const watchIdRef                = useRef(null)

  useEffect(() => {
    if (!navigator.geolocation) {
      setGpsStatus('unavailable')
      setError('Geolocation is not supported by this browser')
      return
    }

    watchIdRef.current = navigator.geolocation.watchPosition(
      (pos) => {
        setPosition({ lat: pos.coords.latitude, lng: pos.coords.longitude })
        setGpsStatus('active')
        setError(null)
      },
      (err) => {
        if (err.code === err.PERMISSION_DENIED) {
          setGpsStatus('denied')
          setError('Location permission denied. Please allow location access.')
        } else {
          setGpsStatus('unavailable')
          setError('Unable to determine location: ' + err.message)
        }
      },
      {
        enableHighAccuracy: true,
        maximumAge: 5000,    // accept cached positions up to 5s old
        timeout: 15000,
      }
    )

    return () => {
      if (watchIdRef.current !== null) {
        navigator.geolocation.clearWatch(watchIdRef.current)
      }
    }
  }, [])

  return { position, gpsStatus, error }
}
