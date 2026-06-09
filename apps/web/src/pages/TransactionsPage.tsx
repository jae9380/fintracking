import { useEffect, useMemo, useState } from 'react'
import { apiFetch } from '../lib/api.ts'

interface Account     { id: number; accountName: string }
interface Category    { id: number; name: string; type: string }
interface Transaction {
  id: number; type: 'INCOME' | 'EXPENSE' | 'TRANSFER'
  amount: number; description: string
  categoryId: number; accountId: number; toAccountId: number | null
  transactionDate: string
}

const TYPE_LABELS  = { INCOME: '수입', EXPENSE: '지출', TRANSFER: '이체' }
const TX_BG:    Record<string, string> = { INCOME: 'bg-emerald-50', EXPENSE: 'bg-red-50',   TRANSFER: 'bg-indigo-50' }
const TX_ICON:  Record<string, string> = { INCOME: '📈',            EXPENSE: '📉',           TRANSFER: '🔄' }
const TX_BADGE: Record<string, string> = {
  INCOME:   'bg-emerald-50 text-emerald-700',
  EXPENSE:  'bg-red-50 text-red-600',
  TRANSFER: 'bg-indigo-50 text-indigo-600',
}
const TX_AMOUNT: Record<string, string> = {
  INCOME: 'text-emerald-600', EXPENSE: 'text-red-600', TRANSFER: 'text-indigo-500',
}
const DOW_LABELS = ['월', '화', '수', '목', '금', '토', '일']

function fmtAmount(n: number) { return n.toLocaleString('ko-KR') + '원' }

function buildCalendarCells(yearMonth: string): (number | null)[] {
  const [y, m] = yearMonth.split('-').map(Number)
  const offset = (new Date(y, m - 1, 1).getDay() + 6) % 7
  const days   = new Date(y, m, 0).getDate()
  return [...Array(offset).fill(null), ...Array.from({ length: days }, (_, i) => i + 1)]
}

function prevMonth(ym: string) {
  const [y, m] = ym.split('-').map(Number)
  const d = new Date(y, m - 2, 1)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function nextMonth(ym: string) {
  const [y, m] = ym.split('-').map(Number)
  const d = new Date(y, m, 1)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function toDateStr(y: number, m: number, d: number) {
  return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [accounts, setAccounts]         = useState<Account[]>([])
  const [categories, setCategories]     = useState<Category[]>([])
  const [filterAccount, setFilterAccount] = useState('')
  const [filterType, setFilterType]       = useState('')
  const [viewMode, setViewMode]           = useState<'list' | 'calendar'>('list')
  const [calendarMonth, setCalendarMonth] = useState(() => {
    const n = new Date()
    return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, '0')}`
  })
  const [selectedDate, setSelectedDate] = useState<string | null>(null)
  const [selectedTx,   setSelectedTx]   = useState<Transaction | null>(null)
  const [selectedIds,  setSelectedIds]  = useState<Set<number>>(new Set())
  const [showModal,    setShowModal]    = useState(false)
  const [showCatModal, setShowCatModal] = useState(false)
  const [form, setForm] = useState({
    accountId: '', toAccountId: '', categoryId: '',
    type: 'EXPENSE', amount: '', description: '', transactionDate: '',
  })
  const [catForm, setCatForm] = useState({ name: '', type: 'EXPENSE' })
  const [loading, setLoading]       = useState(false)
  const [catLoading, setCatLoading] = useState(false)

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
    setSelectedIds(new Set())
  }

  useEffect(() => { load() }, [filterAccount])

  const filtered = filterType ? transactions.filter(t => t.type === filterType) : transactions

  // 날짜별 거래 맵 (캘린더용)
  const txByDate = useMemo(() => {
    const map: Record<string, Transaction[]> = {}
    filtered.forEach(t => {
      ;(map[t.transactionDate] ??= []).push(t)
    })
    return map
  }, [filtered])

  // 캘린더에서 선택된 날짜의 거래
  const selectedDateTxs = selectedDate ? (txByDate[selectedDate] ?? []) : []

  const allSelected  = filtered.length > 0 && selectedIds.size === filtered.length
  const someSelected = selectedIds.size > 0 && !allSelected

  function toggleSelect(id: number) {
    setSelectedIds(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }
  function toggleAll() {
    setSelectedIds(allSelected ? new Set() : new Set(filtered.map(t => t.id)))
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    try {
      await apiFetch('/transaction-service/api/v1/transactions', {
        method: 'POST',
        body: {
          accountId:   Number(form.accountId),
          toAccountId: form.toAccountId ? Number(form.toAccountId) : null,
          categoryId:  form.categoryId  ? Number(form.categoryId)  : null,
          type: form.type, amount: Number(form.amount),
          description: form.description, transactionDate: form.transactionDate,
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
    if (selectedTx?.id === id) setSelectedTx(null)
    load()
  }

  async function handleBulkDelete() {
    if (selectedIds.size === 0) return
    if (!confirm(`선택한 ${selectedIds.size}건의 거래를 삭제하시겠습니까?`)) return
    await apiFetch('/transaction-service/api/v1/transactions/bulk', {
      method: 'DELETE',
      body: { ids: Array.from(selectedIds) },
    })
    load()
  }

  async function handleCreateCategory(e: React.FormEvent) {
    e.preventDefault()
    setCatLoading(true)
    try {
      await apiFetch('/transaction-service/api/v1/categories', {
        method: 'POST', body: { name: catForm.name, type: catForm.type },
      })
      setCatForm({ name: '', type: 'EXPENSE' })
      load()
    } finally { setCatLoading(false) }
  }

  async function handleDeleteCategory(id: number) {
    if (!confirm('카테고리를 삭제하시겠습니까?')) return
    await apiFetch(`/transaction-service/api/v1/categories/${id}`, { method: 'DELETE' })
    load()
  }

  const catMap = Object.fromEntries(categories.map(c => [c.id, c.name]))
  const accMap = Object.fromEntries(accounts.map(a => [a.id, a.accountName]))

  // ── 거래 행 컴포넌트 (목록/캘린더 공통) ──────────────────────────────
  function TxRow({ t, showCheckbox = false }: { t: Transaction; showCheckbox?: boolean }) {
    return (
      <div
        className={`flex items-center px-6 py-3.5 border-b border-slate-100 gap-3 last:border-b-0 transition-colors cursor-pointer
          ${showCheckbox && selectedIds.has(t.id) ? 'bg-indigo-50/50' : 'hover:bg-slate-50/60'}`}
        onClick={() => setSelectedTx(t)}
      >
        {showCheckbox && (
          <input type="checkbox" className="w-4 h-4 rounded accent-indigo-500 cursor-pointer shrink-0"
            checked={selectedIds.has(t.id)}
            onClick={e => e.stopPropagation()}
            onChange={() => toggleSelect(t.id)} />
        )}
        <div className={`w-[38px] h-[38px] rounded-[10px] shrink-0 ${TX_BG[t.type]} flex items-center justify-center text-[17px]`}>
          {TX_ICON[t.type]}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-slate-800 truncate">{t.description || '거래'}</span>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium shrink-0 ${TX_BADGE[t.type]}`}>
              {TYPE_LABELS[t.type]}
            </span>
          </div>
          <div className="text-xs text-slate-400 mt-0.5 truncate">
            {accMap[t.accountId] ?? `계좌 ${t.accountId}`}
            {t.categoryId ? ` · ${catMap[t.categoryId] ?? '카테고리'}` : ''}
            {' · '}{t.transactionDate}
          </div>
        </div>
        <div className={`font-bold text-[15px] mr-2 shrink-0 ${TX_AMOUNT[t.type]}`}>
          {t.type === 'EXPENSE' ? '-' : '+'}{fmtAmount(t.amount)}
        </div>
        <button
          onClick={e => { e.stopPropagation(); handleDelete(t.id) }}
          className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors shrink-0"
        >
          삭제
        </button>
      </div>
    )
  }

  // ── 캘린더 뷰 ────────────────────────────────────────────────────────
  const [calY, calM] = calendarMonth.split('-').map(Number)
  const calCells = buildCalendarCells(calendarMonth)

  function CalendarView() {
    return (
      <div>
        {/* 월 네비게이션 */}
        <div className="flex items-center justify-between mb-4">
          <button onClick={() => { setCalendarMonth(prevMonth(calendarMonth)); setSelectedDate(null) }}
            className="inline-flex items-center px-3 py-1.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors">
            ← 이전달
          </button>
          <span className="font-semibold text-slate-800">{calY}년 {calM}월</span>
          <button onClick={() => { setCalendarMonth(nextMonth(calendarMonth)); setSelectedDate(null) }}
            className="inline-flex items-center px-3 py-1.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors">
            다음달 →
          </button>
        </div>

        {/* 요일 헤더 */}
        <div className="bg-white rounded-t-2xl border border-b-0 border-slate-100 shadow-sm grid grid-cols-7">
          {DOW_LABELS.map(d => (
            <div key={d} className="text-center text-xs font-semibold text-slate-400 py-2.5">{d}</div>
          ))}
        </div>

        {/* 날짜 셀 */}
        <div className="bg-white rounded-b-2xl border border-slate-100 shadow-sm grid grid-cols-7">
          {calCells.map((day, i) => {
            if (!day) return <div key={`e-${i}`} className="h-20 border-b border-r border-slate-100 last:border-r-0" />
            const dateStr  = toDateStr(calY, calM, day)
            const dayTxs   = txByDate[dateStr] ?? []
            const income   = dayTxs.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0)
            const expense  = dayTxs.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0)
            const isSelected = selectedDate === dateStr
            const isToday  = dateStr === toDateStr(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate())
            const col      = i % 7
            const isLastRow = i >= calCells.length - 7

            return (
              <div
                key={dateStr}
                onClick={() => setSelectedDate(isSelected ? null : dateStr)}
                className={`h-20 p-1.5 border-b border-r border-slate-100 cursor-pointer transition-colors
                  ${col === 6 ? 'border-r-0' : ''}
                  ${isLastRow ? 'border-b-0' : ''}
                  ${isSelected ? 'bg-indigo-50' : 'hover:bg-slate-50'}`}
              >
                <div className={`text-xs font-semibold w-6 h-6 flex items-center justify-center rounded-full mb-0.5
                  ${isToday ? 'bg-indigo-400 text-white' : 'text-slate-600'}`}>
                  {day}
                </div>
                {income > 0 && (
                  <div className="text-[10px] text-emerald-600 font-medium truncate leading-tight">
                    +{income.toLocaleString('ko-KR')}
                  </div>
                )}
                {expense > 0 && (
                  <div className="text-[10px] text-red-500 font-medium truncate leading-tight">
                    -{expense.toLocaleString('ko-KR')}
                  </div>
                )}
                {dayTxs.length > 0 && (
                  <div className="flex gap-0.5 mt-0.5 flex-wrap">
                    {dayTxs.slice(0, 3).map(t => (
                      <span key={t.id} className={`w-1.5 h-1.5 rounded-full
                        ${t.type === 'INCOME' ? 'bg-emerald-400' : t.type === 'EXPENSE' ? 'bg-red-400' : 'bg-indigo-400'}`} />
                    ))}
                    {dayTxs.length > 3 && <span className="text-[9px] text-slate-400">+{dayTxs.length - 3}</span>}
                  </div>
                )}
              </div>
            )
          })}
        </div>

        {/* 선택된 날짜의 거래 목록 */}
        {selectedDate && (
          <div className="mt-4">
            <div className="text-sm font-semibold text-slate-700 mb-2 px-1">
              {selectedDate} 거래 {selectedDateTxs.length}건
            </div>
            {selectedDateTxs.length === 0 ? (
              <div className="bg-white rounded-2xl border border-slate-100 shadow-sm text-center py-8 text-slate-400 text-sm">
                이 날 거래가 없습니다
              </div>
            ) : (
              <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
                {selectedDateTxs.map(t => <TxRow key={t.id} t={t} />)}
              </div>
            )}
          </div>
        )}
      </div>
    )
  }

  return (
    <>
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="text-xl font-bold text-slate-800">거래 내역</div>
          <div className="text-xs text-slate-400 mt-0.5">총 {filtered.length}건</div>
        </div>
        <div className="flex gap-2.5">
          {viewMode === 'list' && selectedIds.size > 0 && (
            <button onClick={handleBulkDelete}
              className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-sm font-medium text-red-500 border border-red-200 hover:bg-red-50 transition-colors">
              선택 삭제 ({selectedIds.size})
            </button>
          )}
          <button onClick={() => setShowCatModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors">
            카테고리 관리
          </button>
          <button onClick={() => setShowModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-indigo-400 text-white text-sm font-medium hover:bg-indigo-500 transition-colors">
            + 거래 추가
          </button>
        </div>
      </div>

      {/* 필터 + 뷰 전환 */}
      <div className="flex items-center justify-between flex-wrap gap-2.5 mb-4">
        <div className="flex gap-2.5 items-center flex-wrap">
          <select className="fin-select" style={{ width: 160 }} value={filterAccount}
            onChange={e => { setFilterAccount(e.target.value); setSelectedIds(new Set()) }}>
            <option value="">전체 계좌</option>
            {accounts.map(a => <option key={a.id} value={a.id}>{a.accountName}</option>)}
          </select>
          <select className="fin-select" style={{ width: 120 }} value={filterType}
            onChange={e => { setFilterType(e.target.value); setSelectedIds(new Set()) }}>
            <option value="">전체 유형</option>
            <option value="INCOME">수입</option>
            <option value="EXPENSE">지출</option>
            <option value="TRANSFER">이체</option>
          </select>
        </div>

        {/* 뷰 전환 토글 */}
        <div className="flex items-center gap-1 bg-slate-100 rounded-xl p-1">
          <button
            onClick={() => setViewMode('list')}
            className={`px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors
              ${viewMode === 'list' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
          >
            목록
          </button>
          <button
            onClick={() => setViewMode('calendar')}
            className={`px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors
              ${viewMode === 'calendar' ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
          >
            캘린더
          </button>
        </div>
      </div>

      {/* 목록 뷰 */}
      {viewMode === 'list' && (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
          {filtered.length > 0 && (
            <div className="flex items-center px-6 py-3 border-b border-slate-100 gap-3">
              <input type="checkbox" className="w-4 h-4 rounded accent-indigo-500 cursor-pointer"
                checked={allSelected}
                ref={el => { if (el) el.indeterminate = someSelected }}
                onChange={toggleAll} />
              <span className="text-xs text-slate-400">
                {selectedIds.size > 0 ? `${selectedIds.size}건 선택됨` : '전체 선택'}
              </span>
            </div>
          )}
          {filtered.length === 0 ? (
            <div className="text-center py-12 px-6 text-slate-400">
              <div className="text-4xl mb-3">💳</div>
              <div className="text-sm">거래 내역이 없습니다</div>
            </div>
          ) : filtered.map(t => <TxRow key={t.id} t={t} showCheckbox />)}
        </div>
      )}

      {/* 캘린더 뷰 */}
      {viewMode === 'calendar' && <CalendarView />}

      {/* ── 거래 상세 모달 ──────────────────────────────────────────── */}
      {selectedTx && (
        <div className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setSelectedTx(null)}>
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[420px]" onClick={e => e.stopPropagation()}>
            {/* 헤더 */}
            <div className="flex items-center gap-3 mb-6">
              <div className={`w-12 h-12 rounded-xl shrink-0 ${TX_BG[selectedTx.type]} flex items-center justify-center text-2xl`}>
                {TX_ICON[selectedTx.type]}
              </div>
              <div>
                <div className="text-[17px] font-bold text-slate-800">
                  {selectedTx.description || '거래 상세'}
                </div>
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mt-1 ${TX_BADGE[selectedTx.type]}`}>
                  {TYPE_LABELS[selectedTx.type]}
                </span>
              </div>
            </div>

            {/* 금액 */}
            <div className="bg-slate-50 rounded-2xl p-5 mb-5 text-center">
              <div className="text-xs text-slate-400 mb-1">금액</div>
              <div className={`text-3xl font-bold ${TX_AMOUNT[selectedTx.type]}`}>
                {selectedTx.type === 'EXPENSE' ? '-' : '+'}{fmtAmount(selectedTx.amount)}
              </div>
            </div>

            {/* 상세 정보 */}
            <div className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">계좌</span>
                <span className="font-medium text-slate-800">{accMap[selectedTx.accountId] ?? `계좌 ${selectedTx.accountId}`}</span>
              </div>
              {selectedTx.toAccountId && (
                <div className="flex justify-between text-sm">
                  <span className="text-slate-400">이체 대상</span>
                  <span className="font-medium text-slate-800">{accMap[selectedTx.toAccountId] ?? `계좌 ${selectedTx.toAccountId}`}</span>
                </div>
              )}
              {selectedTx.categoryId && (
                <div className="flex justify-between text-sm">
                  <span className="text-slate-400">카테고리</span>
                  <span className="font-medium text-slate-800">{catMap[selectedTx.categoryId] ?? '카테고리'}</span>
                </div>
              )}
              <div className="flex justify-between text-sm">
                <span className="text-slate-400">거래일</span>
                <span className="font-medium text-slate-800">{selectedTx.transactionDate}</span>
              </div>
              {selectedTx.description && (
                <div className="flex justify-between text-sm">
                  <span className="text-slate-400">메모</span>
                  <span className="font-medium text-slate-800">{selectedTx.description}</span>
                </div>
              )}
            </div>

            <div className="flex gap-2 justify-end mt-6">
              <button
                onClick={() => { handleDelete(selectedTx.id); setSelectedTx(null) }}
                className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors">
                삭제
              </button>
              <button onClick={() => setSelectedTx(null)}
                className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-slate-100 text-slate-700 hover:bg-slate-200 transition-colors">
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── 거래 추가 모달 ─────────────────────────────────────────── */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowModal(false)}>
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
                  onClick={() => setShowModal(false)}>취소</button>
                <button type="submit" disabled={loading}
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors disabled:opacity-60">
                  {loading ? '추가 중...' : '추가하기'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── 카테고리 관리 모달 ─────────────────────────────────────── */}
      {showCatModal && (
        <div className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowCatModal(false)}>
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[480px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-5">카테고리 관리</div>
            <div className="mb-5 max-h-48 overflow-y-auto">
              {categories.length === 0 ? (
                <div className="text-sm text-slate-400 text-center py-4">등록된 카테고리가 없습니다</div>
              ) : categories.map(c => (
                <div key={c.id} className="flex items-center justify-between py-2.5 border-b border-slate-100 last:border-b-0">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-slate-800">{c.name}</span>
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                      c.type === 'INCOME' ? 'bg-emerald-50 text-emerald-600' : 'bg-red-50 text-red-600'
                    }`}>{c.type === 'INCOME' ? '수입' : '지출'}</span>
                  </div>
                  <button onClick={() => handleDeleteCategory(c.id)}
                    className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium text-red-400 border border-red-200 hover:bg-red-50 transition-colors">
                    삭제
                  </button>
                </div>
              ))}
            </div>
            <div className="border-t border-slate-100 pt-5">
              <div className="text-[13px] font-semibold text-slate-600 mb-3">새 카테고리 추가</div>
              <form onSubmit={handleCreateCategory}>
                <div className="flex gap-2 mb-3">
                  <input className="fin-input flex-1" placeholder="카테고리 이름" value={catForm.name}
                    onChange={e => setCatForm(f => ({ ...f, name: e.target.value }))} required />
                  <select className="fin-select" style={{ width: 100 }} value={catForm.type}
                    onChange={e => setCatForm(f => ({ ...f, type: e.target.value }))}>
                    <option value="EXPENSE">지출</option>
                    <option value="INCOME">수입</option>
                  </select>
                </div>
                <div className="flex gap-2 justify-end">
                  <button type="button"
                    className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                    onClick={() => setShowCatModal(false)}>닫기</button>
                  <button type="submit" disabled={catLoading}
                    className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors disabled:opacity-60">
                    {catLoading ? '추가 중...' : '추가하기'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
