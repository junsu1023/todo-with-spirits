import { useMutation } from '@tanstack/react-query'
import {
	BarChart2,
	CalendarDays,
	CalendarRange,
	LogOut,
	TreePine,
	User,
} from 'lucide-react'
import { NavLink, useLocation } from 'react-router-dom'
import { logoutApi } from '@/feature/auth/api/mutate'
import { useAuthStore } from '@/feature/auth/model/authStore'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from '@/shared/ui/popover'
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
	const { pathname } = useLocation()
	const clearAuth = useAuthStore((state) => state.clearAuth)

	const { mutate: logout } = useMutation({
		mutationFn: logoutApi,
		onSettled: () => {
			clearAuth()
		},
	})

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
						<SidebarMenu className="gap-2">
							{navItems.map((item) => (
								<SidebarMenuItem key={item.href}>
									<SidebarMenuButton
										isActive={pathname === item.href}
										render={<NavLink to={item.href} />}
									>
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
				<Popover>
					<PopoverTrigger className="w-full rounded-lg hover:bg-sidebar-accent transition-colors">
						<div className="flex items-center gap-3 px-2 py-3">
							<Avatar className="size-9">
								<AvatarImage src="" alt="User avatar" />
								<AvatarFallback>U</AvatarFallback>
							</Avatar>
							<div className="flex flex-col text-left">
								<span className="text-sm font-medium">Username</span>
								<span className="text-xs text-muted-foreground">Member</span>
							</div>
						</div>
					</PopoverTrigger>
					<PopoverContent side="top" align="start" className="w-48 p-1">
						<button
							type="button"
							onClick={() => logout()}
							className="flex w-full items-center gap-2.5 rounded-md px-3 py-2 text-sm text-red-500 transition-colors hover:bg-red-50"
						>
							<LogOut size={15} />
							로그아웃
						</button>
					</PopoverContent>
				</Popover>
			</SidebarFooter>
		</Sidebar>
	)
}
