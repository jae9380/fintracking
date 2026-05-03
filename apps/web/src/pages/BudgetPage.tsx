import { useEffect, useState } from 'react'
import { apiFetch } from '../lib/api.ts'

interface Budget {
  id: number; categoryId: number; categoryName?: string
  amount: number; spentAmount: number; yearMonth: string
}
interface Category { id: number; name: string }

function fmtAmount(n: number) { return n.toLocaleString('ko-KR') + '원' }

function progressColor(pct: number) {
  if (pct >= 100) return 'bg-red-400'
  if (pct >= 80)  return 'bg-amber-400'
  return 'bg-indigo-400'
}
function progressTextColor(pct: number) {
  if (pct >= 100) return 'text-red-500'
  if (pct >= 80)  return 'text-amber-500'
  return 'text-indigo-500'
}
function iconBg(pct: number) {
  if (pct >= 100) return 'bg-red-50'
  if (pct >= 80)  return 'bg-amber-50'
  return 'bg-indigo-50'
}

export default function BudgetPage() {
  const now = new Date()
  const [yearMonth, setYearMonth] = useState(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
  const [budgets, setBudgets]       = useState<Budget[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [showModal, setShowModal]   = useState(false)
  const [editTarget, setEditTarget] = useState<Budget | null>(null)
  const [form, setForm] = useState({ categoryId: '', amount: '' })
  const [loading, setLoading] = useState(false)

  async function load() {
    const [bd, cd] = await Promise.all([
      apiFetch(`/budget-service/api/v1/budgets?yearMonth=${yearMonth}`),
      apiFetch('/transaction-service/api/v1/categories'),
    ])
    setBudgets(bd.data ?? [])
    setCategories(cd.data ?? [])
  }

  useEffect(() => { load() }, [yearMonth])

  const catMap = Object.fromEntries(categories.map(c => [c.id, c.name]))
  const totalBudget = budgets.reduce((s, b) => s + b.amount, 0)
  const totalSpent  = budgets.reduce((s, b) => s + b.spentAmount, 0)
  const overallPct  = totalBudget > 0 ? Math.round((totalSpent / totalBudget) * 100) : 0

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await apiFetch('/budget-service/api/v1/budgets', {
        method: 'POST',
        body: { categoryId: Number(form.categoryId), amount: Number(form.amount), yearMonth },
      })
      setShowModal(false)
      setForm({ categoryId: '', amount: '' })
      load()
    } finally { setLoading(false) }
  }

  async function handleUpdate(e: React.FormEvent) {
    e.preventDefault()
    if (!editTarget) return
    setLoading(true)
    try {
      await apiFetch(`/budget-service/api/v1/budgets/${editTarget.id}`, {
        method: 'PUT',
        body: { amount: Number(form.amount) },
      })
      setEditTarget(null)
      load()
    } finally { setLoading(false) }
  }

  async function handleDelete(id: number) {
    if (!confirm('예산을 삭제하시겠습니까?')) return
    await apiFetch(`/budget-service/api/v1/budgets/${id}`, { method: 'DELETE' })
    load()
  }

  return (
    <>
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="text-xl font-bold text-slate-800">예산 관리</div>
          <div className="text-xs text-slate-400 mt-0.5">
            총 예산 {fmtAmount(totalBudget)} · 사용 {fmtAmount(totalSpent)}
          </div>
        </div>
        <div className="flex gap-2.5">
          <input
            type="month"
            className="fin-input"
            style={{ width: 160 }}
            value={yearMonth}
            onChange={e => setYearMonth(e.target.value)}
          />
          <button
            onClick={() => setShowModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-400 text-white text-sm font-medium hover:bg-indigo-500 transition-colors"
          >
            + 예산 추가
          </button>
        </div>
      </div>

      {/* Overall progress */}
      {budgets.length > 0 && (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6 mb-5">
          <div className="flex justify-between text-[13px] mb-2">
            <span className="font-semibold text-slate-700">전체 예산 사용률</span>
            <span className="text-slate-400">{overallPct}%</span>
          </div>
          <div className="h-2.5 bg-slate-200 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-[width] duration-500 ${progressColor(overallPct)}`}
              style={{ width: `${Math.min(overallPct, 100)}%` }}
            />
          </div>
        </div>
      )}

      {/* Budget list */}
      {budgets.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
          <div className="text-center py-12 px-6 text-slate-400">
            <div className="text-4xl mb-3">📋</div>
            <div className="text-sm">이 달의 예산이 없습니다. 예산을 추가해보세요.</div>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4">
          {budgets.map(b => {
            const pct       = b.amount > 0 ? Math.min(Math.round((b.spentAmount / b.amount) * 100), 100) : 0
            const remaining = b.amount - b.spentAmount
            return (
              <div key={b.id} className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6">
                <div className="flex justify-between items-start mb-3">
                  <div className="flex items-center gap-2.5">
                    <div className={`w-10 h-10 rounded-[10px] ${iconBg(pct)} flex items-center justify-center text-xl`}>
                      {pct >= 100 ? '🚨' : pct >= 80 ? '⚠️' : '📋'}
                    </div>
                    <div>
                      <div className="font-semibold text-sm text-slate-800">{catMap[b.categoryId] ?? `카테고리 ${b.categoryId}`}</div>
                      <div className="text-[11px] text-slate-400">{b.yearMonth}</div>
                    </div>
                  </div>
                  <div className="flex gap-1.5">
                    <button
                      onClick={() => { setEditTarget(b); setForm({ categoryId: String(b.categoryId), amount: String(b.amount) }) }}
                      className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => handleDelete(b.id)}
                      className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors"
                    >
                      삭제
                    </button>
                  </div>
                </div>

                <div className="flex justify-between text-xs mb-1.5">
                  <span className="text-slate-400">{fmtAmount(b.spentAmount)} 사용</span>
                  <span className={`font-medium ${progressTextColor(pct)}`}>{pct}%</span>
                </div>
                <div className="h-2 bg-slate-200 rounded-full overflow-hidden mb-2.5">
                  <div className={`h-full rounded-full transition-[width] duration-500 ${progressColor(pct)}`} style={{ width: `${pct}%` }} />
                </div>

                <div className="flex justify-between text-[13px] mt-2">
                  <span className="text-slate-400">예산 한도</span>
                  <span className="font-semibold text-slate-800">{fmtAmount(b.amount)}</span>
                </div>
                <div className="flex justify-between text-[13px]">
                  <span className="text-slate-400">잔여 예산</span>
                  <span className={`font-semibold ${remaining < 0 ? 'text-red-500' : 'text-emerald-600'}`}>
                    {remaining < 0 ? '-' : ''}{fmtAmount(Math.abs(remaining))}
                  </span>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* Create modal */}
      {showModal && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowModal(false)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[440px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-5">예산 추가</div>
            <form onSubmit={handleCreate}>
              <div className="fin-group">
                <label className="fin-label">카테고리</label>
                <select className="fin-select" value={form.categoryId}
                  onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))} required>
                  <option value="">카테고리 선택</option>
                  {categories.filter(c => !budgets.find(b => b.categoryId === c.id))
                    .map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="fin-group">
                <label className="fin-label">예산 금액</label>
                <input className="fin-input" type="number" placeholder="0" value={form.amount}
                  onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} required min={1} />
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button"
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                  onClick={() => setShowModal(false)}>
                  취소
                </button>
                <button type="submit" disabled={loading}
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors disabled:opacity-60">
                  추가하기
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit modal */}
      {editTarget && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setEditTarget(null)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[440px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-5">예산 수정</div>
            <form onSubmit={handleUpdate}>
              <div className="fin-group">
                <label className="fin-label">카테고리</label>
                <input className="fin-input" value={catMap[editTarget.categoryId] ?? `카테고리 ${editTarget.categoryId}`} disabled />
              </div>
              <div className="fin-group">
                <label className="fin-label">예산 금액</label>
                <input className="fin-input" type="number" value={form.amount}
                  onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} required min={1} />
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button"
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                  onClick={() => setEditTarget(null)}>
                  취소
                </button>
                <button type="submit" disabled={loading}
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors disabled:opacity-60">
                  저장하기
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}
