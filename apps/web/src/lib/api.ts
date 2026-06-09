// import { handleMock } from './mock.ts'

const MOCK_MODE = false

const BASE_URL = ''

// 동시에 여러 401이 발생할 때 reissue를 한 번만 실행하도록 직렬화
let reissuePromise: Promise<string | null> | null = null

function forceLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  window.location.href = '/login'
}

async function reissueToken(): Promise<string | null> {
  if (reissuePromise) return reissuePromise

  reissuePromise = (async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) return null
    try {
      const res = await fetch(`${BASE_URL}/auth-service/api/v1/auth/reissue`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${refreshToken}`, 'Content-Type': 'application/json' },
      })
      if (!res.ok) return null
      const data = await res.json()
      const payload = data?.data
      const newToken: string | null = payload?.accessToken ?? null
      if (newToken) localStorage.setItem('accessToken', newToken)
      if (payload?.refreshToken) localStorage.setItem('refreshToken', payload.refreshToken)
      return newToken
    } catch {
      return null
    } finally {
      reissuePromise = null
    }
  })()

  return reissuePromise
}

export async function apiFetch(
  path: string,
  options: { method?: string; body?: unknown } = {},
) {
  const token = localStorage.getItem('accessToken')
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    })
  } catch {
    throw new Error('Network error')
  }

  // 401: 토큰 만료 → reissue 후 재시도 (동시 요청은 동일한 reissue 결과 공유)
  if (res.status === 401) {
    const newToken = await reissueToken()
    if (newToken) {
      headers['Authorization'] = `Bearer ${newToken}`
      try {
        const retry = await fetch(`${BASE_URL}${path}`, {
          method: options.method ?? 'GET',
          headers,
          body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
        })
        return retry.json()
      } catch {
        throw new Error('Network error on retry')
      }
    }
    forceLogout()
    throw new Error('Unauthorized')
  }

  // 403: 권한 없음 → 로그아웃
  if (res.status === 403) {
    forceLogout()
    throw new Error('Forbidden')
  }

  return res.json()
}
