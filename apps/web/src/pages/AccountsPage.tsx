import { useEffect, useState } from 'react'
import { apiFetch } from '../lib/api.ts'

interface Account { id: number; accountName: string; accountNumber: string; balance: number; accountType: string }

const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS', 'INVESTMENT', 'CARD']
const TYPE_LABELS: Record<string, string> = {
  CHECKING: '입출금', SAVINGS: '적금', INVESTMENT: '투자', CARD: '카드',
}
const TYPE_ICONS: Record<string, string> = {
  CHECKING: '💳', SAVINGS: '🏦', INVESTMENT: '📈', CARD: '💰',
}

function fmtAmount(n: number) { return n.toLocaleString('ko-KR') + '원' }
function maskAccountNumber(s: string) { return s.length > 4 ? '****-****-' + s.slice(-4) : s }

export default function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[]>([])
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ accountName: '', accountNumber: '', accountType: 'CHECKING' })
  const [loading, setLoading] = useState(false)

  async function load() {
    const d = await apiFetch('/account-service/api/v1/accounts')
    setAccounts(d.data ?? [])
  }

  useEffect(() => { load() }, [])

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await apiFetch('/account-service/api/v1/accounts', { method: 'POST', body: form })
      setShowModal(false)
      setForm({ accountName: '', accountNumber: '', accountType: 'CHECKING' })
      load()
    } finally { setLoading(false) }
  }

  async function handleDelete(id: number) {
    if (!confirm('계좌를 삭제하시겠습니까?')) return
    await apiFetch(`/account-service/api/v1/accounts/${id}`, { method: 'DELETE' })
    load()
  }

  const totalBalance = accounts.reduce((s, a) => s + a.balance, 0)

  return (
    <>
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="text-xl font-bold text-slate-800">계좌 관리</div>
          <div className="text-xs text-slate-400 mt-0.5">총 {accounts.length}개 · 합계 {fmtAmount(totalBalance)}</div>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-400 text-white text-sm font-medium hover:bg-indigo-500 transition-colors"
        >
          + 계좌 추가
        </button>
      </div>

      {accounts.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
          <div className="text-center py-12 px-6 text-slate-400">
            <div className="text-4xl mb-3">🏦</div>
            <div className="text-sm">등록된 계좌가 없습니다. 계좌를 추가해보세요.</div>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {accounts.map(a => (
            <div key={a.id} className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6">
              <div className="flex justify-between items-start">
                <div className="w-11 h-11 rounded-xl bg-indigo-50 flex items-center justify-center text-[22px] mb-3">
                  {TYPE_ICONS[a.accountType] ?? '🏦'}
                </div>
                <button
                  onClick={() => handleDelete(a.id)}
                  className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors"
                >
                  삭제
                </button>
              </div>

              <div className="text-[11px] text-slate-400 mb-0.5">{TYPE_LABELS[a.accountType] ?? a.accountType}</div>
              <div className="text-[15px] font-bold text-slate-800 mb-1">{a.accountName}</div>
              <div className="text-xs text-slate-400 mb-4">{maskAccountNumber(a.accountNumber)}</div>

              <div className="h-px bg-slate-100 mb-3" />
              <div className="text-[11px] text-slate-400 mb-0.5">잔액</div>
              <div className="text-[22px] font-bold text-slate-800">{fmtAmount(a.balance)}</div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowModal(false)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[440px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-5">계좌 추가</div>
            <form onSubmit={handleCreate}>
              <div className="fin-group">
                <label className="fin-label">계좌 이름</label>
                <input className="fin-input" placeholder="예: 주거래 통장" value={form.accountName}
                  onChange={e => setForm(f => ({ ...f, accountName: e.target.value }))} required />
              </div>
              <div className="fin-group">
                <label className="fin-label">계좌 번호</label>
                <input className="fin-input" placeholder="000-0000-0000" value={form.accountNumber}
                  onChange={e => setForm(f => ({ ...f, accountNumber: e.target.value }))} required />
              </div>
              <div className="fin-group">
                <label className="fin-label">계좌 유형</label>
                <select className="fin-select" value={form.accountType}
                  onChange={e => setForm(f => ({ ...f, accountType: e.target.value }))}>
                  {ACCOUNT_TYPES.map(t => <option key={t} value={t}>{TYPE_LABELS[t]}</option>)}
                </select>
              </div>
              <div className="flex gap-2 justify-end mt-2">
                <button type="button"
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                  onClick={() => setShowModal(false)}>
                  취소
                </button>
                <button type="submit" disabled={loading}
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors disabled:opacity-60">
                  {loading ? '추가 중...' : '추가하기'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}
