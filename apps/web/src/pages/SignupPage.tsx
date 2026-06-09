import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', confirmPassword: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }
    setLoading(true)
    try {
      const res = await fetch('/auth-service/api/v1/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, password: form.password }),
      })
      const data = await res.json()
      if (!res.ok) { setError(data.message ?? '회원가입에 실패했습니다.'); return }
      navigate('/login')
    } catch {
      setError('서버에 연결할 수 없습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-slate-50 to-pink-100">
      <div className="bg-white rounded-3xl shadow-lg px-9 py-10 w-full max-w-[420px]">
        <div className="text-center mb-7">
          <div className="text-[36px] mb-2">💰</div>
          <h1 className="text-2xl font-bold text-indigo-600">FinTracking</h1>
          <p className="text-[13px] text-slate-400 mt-1">새 계정을 만들어 시작하세요</p>
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
              placeholder="8자 이상 입력하세요"
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
              required
            />
          </div>
          <div className="fin-group">
            <label className="fin-label">비밀번호 확인</label>
            <input
              className="fin-input"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              value={form.confirmPassword}
              onChange={e => setForm(f => ({ ...f, confirmPassword: e.target.value }))}
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
            className="w-full flex items-center justify-center px-5 py-3.5 rounded-xl bg-indigo-400 text-white text-[15px] font-semibold transition-colors hover:bg-indigo-500 disabled:opacity-60"
            disabled={loading}
          >
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>

        <p className="text-center mt-5 text-[13px] text-slate-400">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="text-indigo-600 font-semibold">
            로그인
          </Link>
        </p>
      </div>
    </div>
  )
}
