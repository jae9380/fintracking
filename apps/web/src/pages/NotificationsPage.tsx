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
  const [selectedNotif, setSelectedNotif] = useState<Notification | null>(null)
  const [showSettings, setShowSettings] = useState(false)
  const [settingsForm, setSettingsForm] = useState({ emailEnabled: false, email: '', fcmEnabled: false })
  const [settingsFcmToken, setSettingsFcmToken] = useState<string | null>(null)
  const [settingsLoading, setSettingsLoading] = useState(false)
  const [settingsSaved, setSettingsSaved] = useState(false)

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

  async function openDetail(n: Notification) {
    setSelectedNotif(n)
    if (!n.read) {
      await apiFetch(`/notification-service/api/v1/notifications/${n.id}/read`, { method: 'PATCH' })
      setData(prev => ({
        ...prev,
        content: prev.content.map(item => item.id === n.id ? { ...item, read: true } : item),
      }))
      setSelectedNotif({ ...n, read: true })
    }
  }

  async function handleMarkAllRead() {
    await apiFetch('/notification-service/api/v1/notifications/read-all', { method: 'PATCH' })
    load(page)
  }

  async function openSettings() {
    setSettingsLoading(true)
    try {
      const res = await apiFetch('/notification-service/api/v1/notifications/settings')
      const s = res.data
      setSettingsForm({
        fcmEnabled: s?.fcmEnabled ?? false,
        emailEnabled: s?.emailEnabled ?? false,
        email: s?.email ?? '',
      })
      setSettingsFcmToken(s?.fcmToken ?? null)
    } catch {
      setSettingsForm({ fcmEnabled: false, emailEnabled: false, email: '' })
      setSettingsFcmToken(null)
    } finally {
      setSettingsLoading(false)
    }
    setShowSettings(true)
  }

  async function handleSaveSettings(e: React.FormEvent) {
    e.preventDefault()
    setSettingsLoading(true)
    try {
      await apiFetch('/notification-service/api/v1/notifications/settings', {
        method: 'POST',
        body: {
          fcmEnabled: settingsForm.fcmEnabled,
          emailEnabled: settingsForm.emailEnabled,
          email: settingsForm.email,
          fcmToken: settingsFcmToken,
        },
      })
      setSettingsSaved(true)
      setTimeout(() => setSettingsSaved(false), 2000)
    } finally { setSettingsLoading(false) }
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
          <button
            onClick={openSettings}
            className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
          >
            알림 설정
          </button>
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
            className={`flex items-center px-6 py-3.5 border-b border-slate-100 gap-3 last:border-b-0 cursor-pointer hover:bg-slate-50/60 transition-colors ${
              !n.read ? 'bg-indigo-50/40' : ''
            }`}
            onClick={() => openDetail(n)}
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

      {/* Notification detail modal */}
      {selectedNotif && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setSelectedNotif(null)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[480px]" onClick={e => e.stopPropagation()}>
            <div className="flex items-start gap-4 mb-5">
              <div className={`w-14 h-14 rounded-2xl shrink-0 ${iconBg(selectedNotif.type)} flex items-center justify-center text-[28px]`}>
                {TYPE_ICONS[selectedNotif.type] ?? '🔔'}
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-[17px] font-bold text-slate-800">{selectedNotif.title}</span>
                  {!selectedNotif.read ? (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-600">새 알림</span>
                  ) : (
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-400">읽음</span>
                  )}
                </div>
                <div className="text-[11px] text-slate-300 mt-1">{new Date(selectedNotif.sentAt).toLocaleString('ko-KR')}</div>
              </div>
            </div>
            <div className="bg-slate-50 rounded-xl p-4 text-sm text-slate-700 leading-relaxed mb-6">
              {selectedNotif.message}
            </div>
            <div className="flex justify-end">
              <button
                onClick={() => setSelectedNotif(null)}
                className="inline-flex items-center px-5 py-2.5 rounded-xl text-sm font-medium bg-indigo-400 text-white hover:bg-indigo-500 transition-colors"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Notification settings modal */}
      {showSettings && (
        <div
          className="fixed inset-0 bg-slate-900/35 flex items-center justify-center z-[999] backdrop-blur-sm"
          onClick={() => setShowSettings(false)}
        >
          <div className="bg-white rounded-3xl shadow-xl p-8 w-full max-w-[440px]" onClick={e => e.stopPropagation()}>
            <div className="text-[17px] font-bold text-slate-800 mb-1">알림 설정</div>
            <div className="text-xs text-slate-400 mb-5">알림을 받을 방법을 설정하세요</div>
            <form onSubmit={handleSaveSettings}>
              {/* 웹 푸시 알림 */}
              <div className="flex items-center justify-between py-3.5 border-b border-slate-100">
                <div>
                  <div className="text-sm font-medium text-slate-800">웹 푸시 알림</div>
                  <div className="text-xs text-slate-400 mt-0.5">브라우저 푸시 알림으로 실시간 수신</div>
                </div>
                <button
                  type="button"
                  onClick={() => setSettingsForm(f => ({ ...f, fcmEnabled: !f.fcmEnabled }))}
                  className={`relative w-11 h-6 rounded-full transition-colors ${settingsForm.fcmEnabled ? 'bg-indigo-400' : 'bg-slate-200'}`}
                >
                  <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${settingsForm.fcmEnabled ? 'translate-x-5.5' : 'translate-x-0.5'}`} />
                </button>
              </div>

              {/* 이메일 알림 */}
              <div className="flex items-center justify-between py-3.5 border-b border-slate-100">
                <div>
                  <div className="text-sm font-medium text-slate-800">이메일 알림</div>
                  <div className="text-xs text-slate-400 mt-0.5">예산 초과, 월간 리포트를 이메일로 수신</div>
                </div>
                <button
                  type="button"
                  onClick={() => setSettingsForm(f => ({ ...f, emailEnabled: !f.emailEnabled }))}
                  className={`relative w-11 h-6 rounded-full transition-colors ${settingsForm.emailEnabled ? 'bg-indigo-400' : 'bg-slate-200'}`}
                >
                  <span className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform ${settingsForm.emailEnabled ? 'translate-x-5.5' : 'translate-x-0.5'}`} />
                </button>
              </div>

              {/* 이메일 주소 입력 */}
              {settingsForm.emailEnabled && (
                <div className="py-3.5 border-b border-slate-100">
                  <label className="fin-label">수신 이메일 주소</label>
                  <input
                    className="fin-input"
                    type="email"
                    placeholder="example@email.com"
                    value={settingsForm.email}
                    onChange={e => setSettingsForm(f => ({ ...f, email: e.target.value }))}
                    required={settingsForm.emailEnabled}
                  />
                </div>
              )}

              <div className="flex gap-2 justify-end mt-5">
                <button type="button"
                  className="inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium text-slate-500 border border-slate-200 hover:bg-slate-50 transition-colors"
                  onClick={() => setShowSettings(false)}>
                  취소
                </button>
                <button type="submit" disabled={settingsLoading}
                  className={`inline-flex items-center px-4 py-2.5 rounded-xl text-sm font-medium transition-colors disabled:opacity-60 ${
                    settingsSaved ? 'bg-emerald-400 text-white' : 'bg-indigo-400 text-white hover:bg-indigo-500'
                  }`}>
                  {settingsSaved ? '저장됨 ✓' : settingsLoading ? '저장 중...' : '저장하기'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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
