import { Outlet } from 'react-router-dom'
import { SidebarInset, SidebarProvider } from '@/shared/ui/sidebar'
import { AppSidebar } from './AppSidebar'

export function AppLayout() {
  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <Outlet />
      </SidebarInset>
    </SidebarProvider>
  )
}
