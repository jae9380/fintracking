import { useEffect, useState } from 'react'
import { apiFetch } from '../lib/api.ts'

interface Notification {
  id: number; type: string; title: string; message: string
  read: boolean; sentAt: string
}
interface PageResponse { content: Notification[]; totalElements: number; last: boolean }

const TYPE_ICONS: Record<string, string> = {
  BUDGET_EXCEEDED: '🚨',
  BUDGET_WARNING:  '⚠️',
  MONTHLY_REPORT:  '📊',
}
function iconBg(type: string) {
  if (type === 'BUDGET_EXCEEDED') return 'bg-red-50'
  if (type === 'BUDGET_WARNING')  return 'bg-amber-50'
  return 'bg-indigo-50'
}

export default function NotificationsPage() {
  const [data, setData]             = useState<PageResponse>({ content: [], totalElements: 0, last: true })
  const [filterRead, setFilterRead] = useState<string>('')
  const [page, setPage]             = useState(0)
  const [loading, setLoading]       = useState(false)

  async function load(p = 0) {
    setLoading(true)
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (filterRead !== '') params.set('read', filterRead)
    const d = await apiFetch(`/notification-service/api/v1/notifications?${params}`)
    setData(d.data ?? { content: [], totalElements: 0, last: true })
    setPage(p)
    setLoading(false)
  }

  useEffect(() => { load(0) }, [filterRead])

  async function handleMarkRead(id: number) {
    await apiFetch(`/notification-service/api/v1/notifications/${id}/read`, { method: 'PATCH' })
    load(page)
  }

  async function handleMarkAllRead() {
    await apiFetch('/notification-service/api/v1/notifications/read-all', { method: 'PATCH' })
    load(page)
  }

  const unreadCount = data.content.filter(n => !n.read).length

  return (
    <>
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="text-xl font-bold text-slate-800">알림</div>
          <div className="text-xs text-slate-400 mt-0.5">총 {data.totalElements}개 · 미읽음 {unreadCount}개</div>
        </div>
        <div className="flex gap-2.5">
          <select
            className="fin-select"
            style={{ width: 120 }}
            value={filterRead}
            onChange={e => setFilterRead(e.target.value)}
          >
            <option value="">전체</option>
            <option value="false">미읽음</option>
            <option value="true">읽음</option>
          </select>
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              전체 읽음
            </button>
          )}
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
        {loading ? (
          <div className="text-center py-12 px-6 text-slate-400">
            <div className="text-sm">로딩 중...</div>
          </div>
        ) : data.content.length === 0 ? (
          <div className="text-center py-12 px-6 text-slate-400">
            <div className="text-4xl mb-3">🔔</div>
            <div className="text-sm">알림이 없습니다</div>
          </div>
        ) : data.content.map(n => (
          <div
            key={n.id}
            className={`flex items-center px-6 py-3.5 border-b border-slate-100 gap-3 last:border-b-0 ${
              !n.read ? 'bg-indigo-50/40 cursor-pointer' : ''
            }`}
            onClick={() => !n.read && handleMarkRead(n.id)}
          >
            <div className={`w-10 h-10 rounded-xl shrink-0 ${iconBg(n.type)} flex items-center justify-center text-[18px]`}>
              {TYPE_ICONS[n.type] ?? '🔔'}
            </div>

            <div className="flex-1">
              <div className="flex items-center gap-2">
                <span className={`text-sm ${n.read ? 'font-normal text-slate-700' : 'font-semibold text-slate-800'}`}>
                  {n.title}
                </span>
                {!n.read && (
                  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-600">
                    새 알림
                  </span>
                )}
              </div>
              <div className="text-xs text-slate-400 mt-0.5 leading-relaxed">{n.message}</div>
              <div className="text-[11px] text-slate-300 mt-1">{new Date(n.sentAt).toLocaleString('ko-KR')}</div>
            </div>

            {!n.read && (
              <div className="w-2 h-2 rounded-full bg-indigo-400 shrink-0" />
            )}
          </div>
        ))}
      </div>

      {/* Pagination */}
      {(!data.last || page > 0) && (
        <div className="flex justify-center gap-2.5 mt-4">
          {page > 0 && (
            <button
              onClick={() => load(page - 1)}
              className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              ← 이전
            </button>
          )}
          {!data.last && (
            <button
              onClick={() => load(page + 1)}
              className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
            >
              다음 →
            </button>
          )}
        </div>
      )}
    </>
  )
}
