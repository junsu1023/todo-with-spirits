import { ChevronLeft } from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { SETTING_ITEMS } from '@/feature/setting'
import { ROUTES } from '@/shared/routes'

const NAV_ITEMS = SETTING_ITEMS.filter(
	(item): item is typeof item & { href: string } => !!item.href,
)

export function SettingPage() {
	return (
		<main className="flex h-screen flex-col overflow-hidden p-6">
			<div className="mb-6 flex items-center gap-3">
				<Link
					to={ROUTES.MYPAGE}
					className="flex h-8 w-8 items-center justify-center rounded-full text-gray-400 hover:bg-gray-100"
				>
					<ChevronLeft size={20} />
				</Link>
				<h1 className="text-xl font-bold">설정</h1>
			</div>

			<div className="flex min-h-0 flex-1 gap-6">
				{/* 좌측 메뉴 */}
				<nav className="flex w-44 shrink-0 flex-col gap-1">
					{NAV_ITEMS.map((item) => (
						<NavLink
							key={item.href}
							to={item.href}
							className={({ isActive }) =>
								`rounded-lg px-4 py-2.5 text-left text-sm font-medium transition-colors ${
									isActive
										? 'bg-[#F3E8FF] text-[#B286FD]'
										: 'text-gray-500 hover:bg-gray-100'
								}`
							}
						>
							{'navLabel' in item ? item.navLabel : item.label}
						</NavLink>
					))}
				</nav>

				{/* 우측 콘텐츠 */}
				<div className="flex min-h-0 flex-1 flex-col">
					<Outlet />
				</div>
			</div>
		</main>
	)
}
