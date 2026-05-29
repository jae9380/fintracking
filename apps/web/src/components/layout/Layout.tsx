import { useEffect } from 'react'
import { requestPermissionAndGetToken, listenForegroundMessages } from '../../lib/messaging.ts'
import { apiFetch } from '../../lib/api.ts'
import Sidebar from './Sidebar.tsx'
import Header from './Header.tsx'

export default function Layout({ children }: { children: React.ReactNode }) {

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken')
    if (!accessToken) return

    requestPermissionAndGetToken().then(token => {
      if (!token) return
      apiFetch('/notification-service/api/v1/notifications/settings', {
        method: 'POST',
        body: { fcmEnabled: true, emailEnabled: false, email: '', fcmToken: token },
      }).catch(e => console.error('[FCM] 토큰 등록 실패:', e))
    })

    listenForegroundMessages((payload: any) => {
      const title = payload?.notification?.title ?? '알림'
      const body  = payload?.notification?.body  ?? ''
      if (Notification.permission === 'granted') {
        new Notification(title, { body, icon: '/favicon.ico' })
      }
    })
  }, [])

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="ml-60 flex-1 flex flex-col min-h-screen">
        <Header />
        <div className="p-7 flex-1">{children}</div>
      </main>
    </div>
  )
}
