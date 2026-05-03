import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/layout/Layout.tsx'
import LoginPage from './pages/LoginPage.tsx'
import SignupPage from './pages/SignupPage.tsx'
import DashboardPage from './pages/DashboardPage.tsx'
import AccountsPage from './pages/AccountsPage.tsx'
import TransactionsPage from './pages/TransactionsPage.tsx'
import BudgetPage from './pages/BudgetPage.tsx'
import NotificationsPage from './pages/NotificationsPage.tsx'

function isAuthenticated() {
  return !!localStorage.getItem('accessToken')
}

// DUMMY: 개발 단계에서 인증 우회 — 백엔드 연동 시 아래 주석 해제하고 그 다음 줄 삭제
// function ProtectedRoute({ children }: { children: React.ReactNode }) {
//   return isAuthenticated() ? <>{children}</> : <Navigate to="/login" replace />
// }
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"  element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <Layout>
                <Routes>
                  <Route index                 element={<DashboardPage />} />
                  <Route path="accounts"       element={<AccountsPage />} />
                  <Route path="transactions"   element={<TransactionsPage />} />
                  <Route path="budget"         element={<BudgetPage />} />
                  <Route path="notifications"  element={<NotificationsPage />} />
                  <Route path="*"              element={<Navigate to="/" replace />} />
                </Routes>
              </Layout>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
