import Sidebar from './Sidebar.tsx'
import Header from './Header.tsx'

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <main className="ml-60 flex-1 flex flex-col min-h-screen">
        <Header />
        <div className="p-7 flex-1">{children}</div>
      </main>
    </div>
  )
}
