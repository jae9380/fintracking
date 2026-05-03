import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'

export default function LoginPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await fetch('/auth-service/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      const data = await res.json()
      if (!res.ok) { setError(data.message ?? '로그인에 실패했습니다.'); return }
      localStorage.setItem('accessToken', data.data.accessToken)
      localStorage.setItem('refreshToken', data.data.refreshToken)
      navigate('/')
    } catch {
      setError('서버에 연결할 수 없습니다.')
    } finally {
      setLoading(false)
    }
  }

  async function handleKakaoLogin() {
    alert('카카오 OAuth2 연동이 필요합니다.')
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-slate-50 to-pink-100">
      <div className="bg-white rounded-3xl shadow-lg px-9 py-10 w-full max-w-[420px]">
        <div className="text-center mb-7">
          <div className="text-[36px] mb-2">💰</div>
          <h1 className="text-2xl font-bold text-indigo-600">FinTracking</h1>
          <p className="text-[13px] text-slate-400 mt-1">개인 자산을 스마트하게 관리하세요</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="fin-group">
            <label className="fin-label">이메일</label>
            <input
              className="fin-input"
              type="email"
              placeholder="example@email.com"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              required
            />
          </div>
          <div className="fin-group">
            <label className="fin-label">비밀번호</label>
            <input
              className="fin-input"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
              required
            />
          </div>

          {error && (
            <div className="px-3.5 py-2.5 rounded-xl bg-red-50 text-red-600 text-[13px] mb-4">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="w-full flex items-center justify-center px-5 py-3.5 rounded-xl bg-indigo-400 text-white text-[15px] font-semibold transition-colors hover:bg-indigo-500 disabled:opacity-60 mb-3"
            disabled={loading}
          >
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>

        <div className="h-px bg-slate-200 my-5" />

        <button
          onClick={handleKakaoLogin}
          className="flex items-center justify-center gap-2.5 w-full px-5 py-3 rounded-xl bg-[#FEE500] text-[#191919] text-sm font-semibold"
        >
          <span className="text-lg">🟡</span>
          카카오로 로그인
        </button>

        <p className="text-center mt-5 text-[13px] text-slate-400">
          계정이 없으신가요?{' '}
          <Link to="/signup" className="text-indigo-600 font-semibold">
            회원가입
          </Link>
        </p>
      </div>
    </div>
  )
}
