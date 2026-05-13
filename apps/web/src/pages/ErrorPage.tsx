import { useNavigate } from 'react-router-dom'

export default function ErrorPage() {
  const navigate = useNavigate()

  return (
    <div className="flex flex-col items-center justify-center h-full py-24 text-center">
      <div className="text-6xl mb-5">🔍</div>
      <div className="text-xl font-bold text-slate-800 mb-2">페이지를 찾을 수 없습니다</div>
      <div className="text-sm text-slate-400 mb-8">요청하신 페이지가 존재하지 않거나 이동되었습니다</div>
      <button
        onClick={() => navigate('/')}
        className="inline-flex items-center px-5 py-2.5 rounded-xl bg-indigo-400 text-white text-sm font-medium hover:bg-indigo-500 transition-colors"
      >
        홈으로 이동
      </button>
    </div>
  )
}
