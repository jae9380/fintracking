import { NavLink, useNavigate } from 'react-router-dom'

const NAV_ITEMS = [
  { to: '/',              label: '대시보드',   icon: '📊', end: true },
  { to: '/accounts',      label: '계좌 관리',  icon: '🏦' },
  { to: '/transactions',  label: '거래 내역',  icon: '💳' },
  { to: '/budget',        label: '예산 관리',  icon: '📋' },
  { to: '/notifications', label: '알림',       icon: '🔔' },
]

export default function Sidebar() {
  const navigate = useNavigate()

  function handleLogout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    navigate('/login')
  }

  return (
    <aside className="w-60 bg-white border-r border-slate-200 fixed top-0 left-0 bottom-0 flex flex-col z-[100]">
      {/* Logo */}
      <div className="px-5 pt-6 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-[10px] bg-indigo-400 flex items-center justify-center text-lg shrink-0">
            💰
          </div>
          <div>
            <div className="font-bold text-[15px] text-slate-800">FinTracking</div>
            <div className="text-[11px] text-slate-400">자산 관리</div>
          </div>
        </div>
      </div>

      <div className="h-px bg-slate-200 mx-4" />

      {/* Nav */}
      <nav className="px-3 py-2 flex-1">
        {NAV_ITEMS.map(({ to, label, icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm mb-0.5 transition-all duration-150 ${
                isActive
                  ? 'font-semibold text-indigo-600 bg-indigo-50'
                  : 'font-normal text-slate-500 hover:bg-slate-50'
              }`
            }
          >
            <span className="text-base">{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Logout */}
      <div className="px-4 pb-5">
        <div className="h-px bg-slate-200 mb-3" />
        <button
          onClick={handleLogout}
          className="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm text-slate-500 w-full transition-all duration-150 hover:bg-slate-50"
        >
          <span className="text-base">🚪</span>
          로그아웃
        </button>
      </div>
    </aside>
  )
}
