import { useLocation, useNavigate } from 'react-router-dom'

const PAGE_TITLES: Record<string, string> = {
  '/':              '대시보드',
  '/accounts':      '계좌 관리',
  '/transactions':  '거래 내역',
  '/budget':        '예산 관리',
  '/notifications': '알림',
}

export default function Header() {
  const location = useLocation()
  const navigate = useNavigate()
  const title = PAGE_TITLES[location.pathname] ?? '대시보드'

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-7 sticky top-0 z-[99]">
      <h2 className="text-base font-bold text-slate-800">{title}</h2>

      <div className="flex items-center gap-2">
        <button
          onClick={() => navigate('/notifications')}
          className="w-9 h-9 rounded-[10px] flex items-center justify-center bg-slate-50 border border-slate-200 relative text-base"
        >
          🔔
          <span className="absolute top-1.5 right-1.5 w-[7px] h-[7px] rounded-full bg-red-400 border-2 border-white" />
        </button>
        <div className="w-9 h-9 rounded-[10px] bg-indigo-50 flex items-center justify-center text-base">
          👤
        </div>
      </div>
    </header>
  )
}
