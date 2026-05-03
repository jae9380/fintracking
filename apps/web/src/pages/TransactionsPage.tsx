import { useEffect, useState } from 'react'
import { apiFetch } from '../lib/api.ts'

interface Account     { id: number; accountName: string }
interface Category    { id: number; name: string; type: string }
interface Transaction {
  id: number; type: 'INCOME' | 'EXPENSE' | 'TRANSFER'
  amount: number; description: string
  categoryId: number; accountId: number; toAccountId: number | null
  transactionDate: string
}

const TYPE_LABELS = { INCOME: '수입', EXPENSE: '지출', TRANSFER: '이체' }
const TX_BG:    Record<string, string> = { INCOME: 'bg-emerald-50', EXPENSE: 'bg-red-50',    TRANSFER: 'bg-indigo-50' }
const TX_ICON:  Record<string, string> = { INCOME: '📈',            EXPENSE: '📉',            TRANSFER: '🔄' }
const TX_BADGE: Record<string, string> = {
  INCOME:   'bg-emerald-50 text-emerald-700',
  EXPENSE:  'bg-red-50 text-red-600',
  TRANSFER: 'bg-indigo-50 text-indigo-600',
}
const TX_AMOUNT: Record<string, string> = {
  INCOME: 'text-emerald-600', EXPENSE: 'text-red-600', TRANSFER: 'text-indigo-500',
}

function fmtAmount(n: number) { return n.toLocaleString('ko-KR') + '원' }

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [accounts, setAccounts]         = useState<Account[]>([])
  const [categories, setCategories]     = useState<Category[]>([])
  const [filterAccount, setFilterAccount] = useState('')
  const [filterType, setFilterType]       = useState('')
  const [showModal, setShowModal]         = useState(false)
  const [form, setForm] = useState({
    accountId: '', toAccountId: '', categoryId: '',
    type: 'EXPENSE', amount: '', description: '', transactionDate: '',
  })
  const [loading, setLoading] = useState(false)

  async function load() {
    const params = filterAccount ? `?accountId=${filterAccount}` : ''
    const [td, ad, cd] = await Promise.all([
      apiFetch(`/transaction-service/api/v1/transactions${params}`),
      apiFetch('/account-service/api/v1/accounts'),
      apiFetch('/transaction-service/api/v1/categories'),
    ])
    setTransactions(td.data ?? [])
    setAccounts(ad.data ?? [])
    setCategories(cd.data ?? [])
  }

  useEffect(() => { load() }, [filterAccount])

  const filtered = filterType ? transactions.filter(t => t.type === filterType) : transactions

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await apiFetch('/transaction-service/api/v1/transactions', {
        method: 'POST',
        body: {
          accountId: Number(form.accountId),
          toAccountId: form.toAccountId ? Number(form.toAccountId) : null,
          categoryId: form.categoryId ? Number(form.categoryId) : null,
          type: form.type,
          amount: Number(form.amount),
          description: form.description,
          transactionDate: form.transactionDate,
        },
      })
      setShowModal(false)
      setForm({ accountId: '', toAccountId: '', categoryId: '', type: 'EXPENSE', amount: '', description: '', transactionDate: '' })
      load()
    } finally { setLoading(false) }
  }

  async function handleDelete(id: number) {
    if (!confirm('거래를 삭제하시겠습니까?')) return
    await apiFetch(`/transaction-service/api/v1/transactions/${id}`, { method: 'DELETE' })
    load()
  }

  const catMap = Object.fromEntries(categories.map(c => [c.id, c.name]))
  const accMap = Object.fromEntries(accounts.map(a => [a.id, a.accountName]))

  return (
    <>
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="text-xl font-bold text-slate-800">거래 내역</div>
          <div className="text-xs text-slate-400 mt-0.5">총 {filtered.length}건</div>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-400 text-white text-sm font-medium hover:bg-indigo-500 transition-colors"
        >
          + 거래 추가
        </button>
      </div>

      {/* Filters */}
      <div className="flex gap-2.5 items-center flex-wrap mb-4">
        <select className="fin-select" style={{ width: 160 }} value={filterAccount} onChange={e => setFilterAccount(e.target.value)}>
          <option value="">전체 계좌</option>
          {accounts.map(a => <option key={a.id} value={a.id}>{a.accountName}</option>)}
        </select>
        <select className="fin-select" style={{ width: 120 }} value={filterType} onChange={e => setFilterType(e.target.value)}>
          <option value="">전체 유형</option>
          <option value="INCOME">수입</option>
          <option value="EXPENSE">지출</option>
          <option value="TRANSFER">이체</option>
        </select>
      </div>

      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
        {filtered.length === 0 ? (
          <div className="text-center py-12 px-6 text-slate-400">
            <div className="text-4xl mb-3">💳</div>
            <div className="text-sm">거래 내역이 없습니다</div>
          </div>
        ) : filtered.map(t => (
          <div key={t.id} className="flex items-center px-6 py-3.5 border-b border-slate-100 gap-3 last:border-b-0">
            <div className={`w-[38px] h-[38px] rounded-[10px] shrink-0 ${TX_BG[t.type]} flex items-center justify-center text-[17px]`}>
              {TX_ICON[t.type]}
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-slate-800">{t.description || '거래'}</span>
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${TX_BADGE[t.type]}`}>
                  {TYPE_LABELS[t.type]}
                </span>
              </div>
              <div className="text-xs text-slate-400 mt-0.5">
                {accMap[t.accountId] ?? `계좌 ${t.accountId}`}
                {t.categoryId ? ` · ${catMap[t.categoryId] ?? '카테고리'}` : ''}
                {' · '}{t.transactionDate}
              </div>
            </div>
            <div className={`font-bold text-[15px] mr-2 ${TX_AMOUNT[t.type]}`}>
              {t.type === 'EXPENSE' ? '-' : '+'}{fmtAmount(t.amount)}
            </div>
            <button
              onClick={() => handleDelete(t.id)}
              className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors"
            >
              삭제
            </button>
          </div>
        ))}
      </div>

      {showModal && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowModal(false)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[440px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-5">거래 추가</div>
            <form onSubmit={handleCreate}>
              <div className="fin-group">
                <label className="fin-label">거래 유형</label>
                <select className="fin-select" value={form.type}
                  onChange={e => setForm(f => ({ ...f, type: e.target.value }))}>
                  <option value="INCOME">수입</option>
                  <option value="EXPENSE">지출</option>
                  <option value="TRANSFER">이체</option>
                </select>
              </div>
              <div className="fin-group">
                <label className="fin-label">계좌</label>
                <select className="fin-select" value={form.accountId}
                  onChange={e => setForm(f => ({ ...f, accountId: e.target.value }))} required>
                  <option value="">계좌 선택</option>
                  {accounts.map(a => <option key={a.id} value={a.id}>{a.accountName}</option>)}
                </select>
              </div>
              {form.type === 'TRANSFER' && (
                <div className="fin-group">
                  <label className="fin-label">이체 대상 계좌</label>
                  <select className="fin-select" value={form.toAccountId}
                    onChange={e => setForm(f => ({ ...f, toAccountId: e.target.value }))} required>
                    <option value="">계좌 선택</option>
                    {accounts.filter(a => String(a.id) !== form.accountId)
                      .map(a => <option key={a.id} value={a.id}>{a.accountName}</option>)}
                  </select>
                </div>
              )}
              {form.type !== 'TRANSFER' && (
                <div className="fin-group">
                  <label className="fin-label">카테고리</label>
                  <select className="fin-select" value={form.categoryId}
                    onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))} required>
                    <option value="">카테고리 선택</option>
                    {categories.filter(c => c.type === form.type || c.type === 'DEFAULT')
                      .map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              )}
              <div className="fin-group">
                <label className="fin-label">금액</label>
                <input className="fin-input" type="number" placeholder="0" value={form.amount}
                  onChange={e => setForm(f => ({ ...f, amount: e.target.value }))} required min={1} />
              </div>
              <div className="fin-group">
                <label className="fin-label">거래일</label>
                <input className="fin-input" type="date" value={form.transactionDate}
                  onChange={e => setForm(f => ({ ...f, transactionDate: e.target.value }))} required />
              </div>
              <div className="fin-group">
                <label className="fin-label">메모 (선택)</label>
                <input className="fin-input" placeholder="거래 메모" value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
              </div>
              <div className="flex gap-2 justify-end">
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
