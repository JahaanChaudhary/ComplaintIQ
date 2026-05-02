export function StatCard({ title, value, subtitle, icon: Icon, color = 'blue', onClick }) {
  const colors = {
    blue: 'bg-blue-50 text-blue-600', red: 'bg-red-50 text-red-600',
    green: 'bg-green-50 text-green-600', orange: 'bg-orange-50 text-orange-600',
    purple: 'bg-purple-50 text-purple-600', yellow: 'bg-yellow-50 text-yellow-600'
  }
  return (
    <div className={`card flex items-center gap-4 ${onClick ? 'cursor-pointer hover:shadow-md transition-shadow' : ''}`}
      onClick={onClick}>
      {Icon && (
        <div className={`p-3 rounded-xl ${colors[color]}`}>
          <Icon size={24} />
        </div>
      )}
      <div>
        <p className="text-sm text-gray-500">{title}</p>
        <p className="text-2xl font-bold text-gray-900">{value ?? '-'}</p>
        {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
      </div>
    </div>
  )
}
