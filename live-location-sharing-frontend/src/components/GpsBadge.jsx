/**
 * Small floating badge (top-left) showing GPS signal status.
 *
 * Props:
 *   status — 'waiting' | 'active' | 'denied' | 'unavailable'
 */
export default function GpsBadge({ status }) {
  const config = {
    waiting:     { icon: '🛰️',  label: 'Getting GPS…',    color: 'var(--text-muted)'  },
    active:      { icon: '📡',  label: 'GPS Active',       color: 'var(--success)'     },
    denied:      { icon: '🚫',  label: 'GPS Denied',       color: 'var(--danger)'      },
    unavailable: { icon: '❌',  label: 'GPS Unavailable',  color: 'var(--danger)'      },
  }

  const { icon, label, color } = config[status] || config.waiting

  return (
    <div className="gps-badge">
      <span>{icon}</span>
      <span style={{ color }}>{label}</span>
    </div>
  )
}
