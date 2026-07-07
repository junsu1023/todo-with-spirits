import { BarChart2, CalendarDays, CalendarRange, TreePine, User } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/shared/ui/sidebar'

const navItems = [
  { label: 'Today', icon: CalendarDays, href: '/today' },
  { label: 'Plan', icon: CalendarRange, href: '/plan' },
  { label: 'Forest', icon: TreePine, href: '/forest' },
  { label: 'Record', icon: BarChart2, href: '/record' },
  { label: 'My Page', icon: User, href: '/my-page' },
]

export function AppSidebar() {
  return (
    <Sidebar>
      <SidebarHeader>
        <div className="px-2 py-3">
          <p className="text-xl font-bold text-brand">Todo with Spirits</p>
</div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.href}>
                  <SidebarMenuButton render={<a href={item.href} />}>
                    <item.icon />
                    <span>{item.label}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        <div className="flex items-center gap-3 px-2 py-3">
          <Avatar className="size-9">
            <AvatarImage src="" alt="User avatar" />
            <AvatarFallback>U</AvatarFallback>
          </Avatar>
          <div className="flex flex-col">
            <span className="text-sm font-medium">Username</span>
            <span className="text-xs text-muted-foreground">Member</span>
          </div>
        </div>
      </SidebarFooter>
    </Sidebar>
  )
}
