import { app } from './firebase.ts'
import { getMessaging, getToken, onMessage } from 'firebase/messaging'

const messaging = getMessaging(app)

export async function requestPermissionAndGetToken(): Promise<string | null> {
  try {
    const permission = await Notification.requestPermission()
    if (permission !== 'granted') return null

    const swRegistration = await navigator.serviceWorker.register(
      '/firebase-messaging-sw.js'
    )

    return await getToken(messaging, {
      vapidKey: import.meta.env.VITE_FIREBASE_VAPID_KEY,
      serviceWorkerRegistration: swRegistration,
    })
  } catch (e) {
    console.error('[FCM] 토큰 발급 실패:', e)
    return null
  }
}

export function listenForegroundMessages(callback: (payload: unknown) => void) {
  onMessage(messaging, callback)
}
