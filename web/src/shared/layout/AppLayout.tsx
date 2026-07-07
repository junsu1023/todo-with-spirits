import type { ReactNode } from "react";
import { SidebarInset, SidebarProvider } from "@/shared/ui/sidebar";
import { AppSidebar } from "./AppSidebar";

interface AppLayoutProps {
	children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
	return (
		<SidebarProvider>
			<AppSidebar />
			<SidebarInset>{children}</SidebarInset>
		</SidebarProvider>
	);
}
