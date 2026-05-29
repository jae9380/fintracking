import { useLocation, useNavigate } from 'react-router-dom'
import { apiFetch } from '../../lib/api.ts'

const PAGE_TITLES: Record<string, string> = {
  '/':              '대시보드',
  '/accounts':      '계좌 관리',
  '/transactions':  '거래 내역',
  '/budget':        '예산 관리',
  '/notifications': '알림',
}

export default function Header() {
  const location = useLocation()
  const navigate  = useNavigate()
  const title = PAGE_TITLES[location.pathname] ?? '대시보드'

  async function handleLogout() {
    try {
      // FCM 토큰 해제
      await apiFetch('/notification-service/api/v1/notifications/settings', {
        method: 'POST',
        body: { fcmEnabled: false, emailEnabled: false, email: '', fcmToken: null },
      })
    } catch {
      // 토큰 해제 실패해도 로그아웃은 진행
    }

    try {
      await apiFetch('/auth-service/api/v1/auth/logout', { method: 'POST' })
    } catch {
      // 서버 로그아웃 실패해도 클라이언트 정리는 진행
    }

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    navigate('/login')
  }

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
        <button
          onClick={handleLogout}
          className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
        >
          로그아웃
        </button>
      </div>
    </header>
  )
}
