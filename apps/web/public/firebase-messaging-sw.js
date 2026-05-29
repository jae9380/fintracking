  importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js')
  importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js')

  firebase.initializeApp({
    apiKey: "AIzaSyAzf5UKlLeRBXfUetUzv6igFkBUeiKoFYw",
    authDomain: "fintracking-91724.firebaseapp.com",
    projectId: "fintracking-91724",
    storageBucket: "fintracking-91724.firebasestorage.app",
    messagingSenderId: "1000652029127",
    appId: "1:1000652029127:web:6d80de278652cc030992e2"
  })

  const messaging = firebase.messaging()

  messaging.onBackgroundMessage(function(payload) {
    self.registration.showNotification(
      payload.notification?.title || '알림',
      { body: payload.notification?.body, icon: '/favicon.ico' }
    )
  })

  self.addEventListener('notificationclick', function(event) {
    event.notification.close()
    event.waitUntil(
      clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function(clientList) {
        if (clientList.length > 0) clientList[0].focus()
        else clients.openWindow(self.location.origin)
      })
    )
  })