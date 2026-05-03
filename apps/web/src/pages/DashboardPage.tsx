import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiFetch } from '../lib/api.ts'

interface Account   { id: number; accountName: string; balance: number; accountType: string }
interface Transaction {
  id: number; type: 'INCOME' | 'EXPENSE' | 'TRANSFER'
  amount: number; description: string; categoryId: number; transactionDate: string
}
interface Budget { id: number; categoryId: number; amount: number; spentAmount: number; yearMonth: string }

function fmtAmount(n: number) {
  return n.toLocaleString('ko-KR') + '원'
}

const TX_BG: Record<string, string> = {
  INCOME:   'bg-emerald-50',
  EXPENSE:  'bg-red-50',
  TRANSFER: 'bg-indigo-50',
}
const TX_ICON: Record<string, string> = { INCOME: '📈', EXPENSE: '📉', TRANSFER: '🔄' }
const TX_COLOR: Record<string, string> = {
  INCOME:   'text-emerald-600',
  EXPENSE:  'text-red-600',
  TRANSFER: 'text-indigo-500',
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const [accounts, setAccounts]         = useState<Account[]>([])
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [budgets, setBudgets]           = useState<Budget[]>([])

  useEffect(() => {
    apiFetch('/account-service/api/v1/accounts').then(d => setAccounts(d.data ?? []))
    apiFetch('/transaction-service/api/v1/transactions').then(d => setTransactions((d.data ?? []).slice(0, 5)))
    apiFetch('/budget-service/api/v1/budgets').then(d => setBudgets(d.data ?? []))
  }, [])

  const totalBalance = accounts.reduce((s, a) => s + a.balance, 0)
  const monthIncome  = transactions.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0)
  const monthExpense = transactions.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0)

  return (
    <>
      {/* Summary cards */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6 border-t-[3px] border-t-indigo-400">
          <div className="text-xs text-slate-400 mb-1.5">총 자산</div>
          <div className="text-2xl font-bold text-slate-800">{fmtAmount(totalBalance)}</div>
          <div className="text-xs text-slate-400 mt-1">계좌 {accounts.length}개 합산</div>
        </div>
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6 border-t-[3px] border-t-emerald-400">
          <div className="text-xs text-slate-400 mb-1.5">이번 달 수입</div>
          <div className="text-2xl font-bold text-emerald-600">{fmtAmount(monthIncome)}</div>
          <div className="text-xs text-slate-400 mt-1">이번 달 합계</div>
        </div>
        <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6 border-t-[3px] border-t-red-400">
          <div className="text-xs text-slate-400 mb-1.5">이번 달 지출</div>
          <div className="text-2xl font-bold text-red-600">{fmtAmount(monthExpense)}</div>
          <div className="text-xs text-slate-400 mt-1">이번 달 합계</div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-5">
        {/* Recent transactions */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <span className="font-semibold text-[15px] text-slate-800">최근 거래</span>
            <button
              onClick={() => navigate('/transactions')}
              className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              전체 보기
            </button>
          </div>
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
            {transactions.length === 0 ? (
              <div className="text-center py-12 px-6 text-slate-400">
                <div className="text-4xl mb-3">💳</div>
                <div className="text-sm">거래 내역이 없습니다</div>
              </div>
            ) : transactions.map(t => (
              <div key={t.id} className="flex items-center px-6 py-3.5 border-b border-slate-100 gap-3 last:border-b-0">
                <div className={`w-9 h-9 rounded-[10px] shrink-0 ${TX_BG[t.type]} flex items-center justify-center text-base`}>
                  {TX_ICON[t.type]}
                </div>
                <div className="flex-1">
                  <div className="text-sm font-medium text-slate-800">{t.description || '거래'}</div>
                  <div className="text-xs text-slate-400 mt-0.5">{t.transactionDate}</div>
                </div>
                <div className={`font-semibold text-sm ${TX_COLOR[t.type]}`}>
                  {t.type === 'EXPENSE' ? '-' : '+'}{fmtAmount(t.amount)}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Budget overview */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <span className="font-semibold text-[15px] text-slate-800">예산 현황</span>
            <button
              onClick={() => navigate('/budget')}
              className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              전체 보기
            </button>
          </div>
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6">
            {budgets.length === 0 ? (
              <div className="text-center py-12 px-6 text-slate-400">
                <div className="text-4xl mb-3">📋</div>
                <div className="text-sm">등록된 예산이 없습니다</div>
              </div>
            ) : budgets.slice(0, 4).map(b => {
              const pct = Math.min(Math.round((b.spentAmount / b.amount) * 100), 100)
              const barColor = pct >= 100 ? 'bg-red-400' : pct >= 80 ? 'bg-amber-400' : 'bg-indigo-400'
              return (
                <div key={b.id} className="mb-4 last:mb-0">
                  <div className="flex justify-between text-[13px] mb-1.5">
                    <span className="font-medium text-slate-700">카테고리 {b.categoryId}</span>
                    <span className="text-slate-400">{pct}% · {fmtAmount(b.spentAmount)} / {fmtAmount(b.amount)}</span>
                  </div>
                  <div className="h-2 bg-slate-200 rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-[width] duration-500 ${barColor}`} style={{ width: `${pct}%` }} />
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* Account summary */}
      <div className="mt-6">
        <div className="flex items-center justify-between mb-3">
          <span className="font-semibold text-[15px] text-slate-800">내 계좌</span>
          <button
            onClick={() => navigate('/accounts')}
            className="inline-flex items-center px-3 py-1.5 rounded-xl text-xs font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
          >
            전체 보기
          </button>
        </div>
        <div className="grid grid-cols-3 gap-4">
          {accounts.map(a => (
            <div
              key={a.id}
              className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6 cursor-pointer hover:shadow-md transition-shadow"
              onClick={() => navigate('/accounts')}
            >
              <div className="flex items-center gap-2.5 mb-3">
                <div className="w-9 h-9 rounded-[10px] bg-indigo-50 flex items-center justify-center text-lg shrink-0">🏦</div>
                <div>
                  <div className="text-[13px] font-semibold text-slate-800">{a.accountName}</div>
                  <div className="text-[11px] text-slate-400">{a.accountType}</div>
                </div>
              </div>
              <div className="text-xl font-bold text-slate-800">{fmtAmount(a.balance)}</div>
            </div>
          ))}
          {accounts.length === 0 && (
            <div className="col-span-3 bg-white rounded-2xl border border-slate-100 shadow-sm p-5 px-6">
              <div className="text-center py-8 text-slate-400">
                <div className="text-4xl mb-3">🏦</div>
                <div className="text-sm">등록된 계좌가 없습니다</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  )
}
