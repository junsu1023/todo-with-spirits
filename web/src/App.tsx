import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AppLayout } from "@/shared/layout/AppLayout";
import { PlanPage } from "./page/PlanPage";
import { TodayPage } from "./page/TodayPage";

function App() {
	return (
		<BrowserRouter>
			<AppLayout>
				<Routes>
					<Route path="/" element={<TodayPage />} />
					<Route path="/today" element={<TodayPage />} />
					<Route path="/plan" element={<PlanPage />} />
				</Routes>
			</AppLayout>
		</BrowserRouter>
	);
}

export default App;
