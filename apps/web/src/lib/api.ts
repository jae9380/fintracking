import { handleMock } from './mock.ts'

// DUMMY: 백엔드 연동 시 false로 변경
const MOCK_MODE = true

const BASE_URL = ''

export async function apiFetch(
  path: string,
  options: { method?: string; body?: unknown } = {},
) {
  // DUMMY: 목업 모드에서는 실제 API 호출 대신 더미 핸들러 사용
  if (MOCK_MODE) {
    await new Promise(r => setTimeout(r, 80)) // 실제 API 느낌을 위한 딜레이
    return handleMock(path, options.method ?? 'GET', options.body)
  }

  const token = localStorage.getItem('accessToken')
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  })

  // 401: 토큰 만료 → reissue 시도
  if (res.status === 401) {
    const refreshToken = localStorage.getItem('refreshToken')
    if (refreshToken) {
      const reissued = await fetch(`${BASE_URL}/auth-service/api/v1/auth/reissue`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${refreshToken}` },
      })
      if (reissued.ok) {
        const data = await reissued.json()
        localStorage.setItem('accessToken', data.data)
        headers['Authorization'] = `Bearer ${data.data}`
        const retry = await fetch(`${BASE_URL}${path}`, {
          method: options.method ?? 'GET',
          headers,
          body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
        })
        return retry.json()
      }
    }
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    window.location.href = '/login'
    throw new Error('Unauthorized')
  }

  return res.json()
}
