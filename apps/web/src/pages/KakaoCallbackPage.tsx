import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

export default function KakaoCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [error, setError] = useState('')

  useEffect(() => {
    const code = searchParams.get('code')
    if (!code) {
      setError('인가 코드가 없습니다.')
      return
    }

    fetch('/auth-service/api/v1/auth/oauth2/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code }),
    })
      .then(res => res.json())
      .then(data => {
        if (!data?.data?.accessToken) {
          setError(data.message ?? '카카오 로그인에 실패했습니다.')
          return
        }
        localStorage.setItem('accessToken', data.data.accessToken)
        localStorage.setItem('refreshToken', data.data.refreshToken)
        navigate('/', { replace: true })
      })
      .catch(() => setError('서버에 연결할 수 없습니다.'))
  }, [])

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-slate-50 to-pink-100">
        <div className="bg-white rounded-3xl shadow-lg px-9 py-10 w-full max-w-[420px] text-center">
          <div className="text-4xl mb-4">⚠️</div>
          <div className="text-slate-700 font-medium mb-5">{error}</div>
          <button
            onClick={() => navigate('/login', { replace: true })}
            className="inline-flex items-center px-5 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors"
          >
            로그인으로 돌아가기
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-slate-50 to-pink-100">
      <div className="bg-white rounded-3xl shadow-lg px-9 py-10 w-full max-w-[420px] text-center">
        <div className="text-4xl mb-4">🟡</div>
        <div className="text-slate-600 text-sm">카카오 로그인 처리 중...</div>
      </div>
    </div>
  )
}
