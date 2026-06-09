import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/layout/Layout.tsx'
import LoginPage from './pages/LoginPage.tsx'
import SignupPage from './pages/SignupPage.tsx'
import KakaoCallbackPage from './pages/KakaoCallbackPage.tsx'
import DashboardPage from './pages/DashboardPage.tsx'
import AccountsPage from './pages/AccountsPage.tsx'
import TransactionsPage from './pages/TransactionsPage.tsx'
import BudgetPage from './pages/BudgetPage.tsx'
import NotificationsPage from './pages/NotificationsPage.tsx'
import ErrorPage from './pages/ErrorPage.tsx'

function isAuthenticated() {
  return !!localStorage.getItem('accessToken')
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  return isAuthenticated() ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"          element={<LoginPage />} />
        <Route path="/signup"         element={<SignupPage />} />
        <Route path="/outh/callback"  element={<KakaoCallbackPage />} />
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
                  <Route path="*"              element={<ErrorPage />} />
                </Routes>
              </Layout>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
