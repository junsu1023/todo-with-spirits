import {
	CalendarClock,
	ChevronLeft,
	ChevronRight,
	Pencil,
	Plus,
	Search,
	Star,
	Trash2,
} from "lucide-react";
import { useState } from "react";
import { Card } from "@/shared/ui/card";
import { DropdownSelect } from "@/shared/ui/dropdown-select";
import { PlanItemForm } from "./PlanItemForm";

// ─── Types ───────────────────────────────────────────────────────────────────

export type ItemType = "todo" | "routine";
type PlanTab = "전체" | "To do" | "루틴";

export interface PlanItem {
	id: number;
	type: ItemType;
	title: string;
	starred: boolean;
	completed: boolean;
	dday: number;
	dateLabel: string;
	date?: string;
	time?: string;
	category?: string;
	isPublic?: boolean;
	repeat?: "매일" | "매주" | "매월";
	excludeHolidays?: boolean;
	memo?: string;
	tags: string[];
}

// ─── Mock Data ────────────────────────────────────────────────────────────────

const MOCK_ITEMS: PlanItem[] = [
	{
		id: 1,
		type: "todo",
		title: "성과 보고서 제출 마감",
		starred: true,
		completed: true,
		dday: 0,
		dateLabel: "26. 06. 01 (월) PM 06:00",
		date: "2026-06-01",
		time: "18:00",
		category: "업무/커리어",
		isPublic: false,
		memo: "퇴근 전까지 이메일로 발송할 것",
		tags: ["업무/커리어"],
	},
	{
		id: 2,
		type: "todo",
		title: "민지랑 저녁",
		starred: false,
		completed: false,
		dday: 0,
		dateLabel: "26. 06. 01 (월) PM 07:00",
		date: "2026-06-01",
		time: "19:00",
		category: "인간관계/약속",
		isPublic: false,
		memo: undefined,
		tags: ["인간관계/약속"],
	},
	{
		id: 3,
		type: "routine",
		title: "책 20페이지 읽기",
		starred: false,
		completed: false,
		dday: 0,
		dateLabel: "26. 06. 01 (월) PM 07:00",
		date: "2026-06-01",
		time: "19:00",
		category: "자기계발",
		isPublic: false,
		memo: undefined,
		tags: ["자기계발", "매주 월, 화"],
	},
	{
		id: 4,
		type: "todo",
		title: "비행기 티켓 끊기",
		starred: false,
		completed: false,
		dday: 10,
		dateLabel: "26. 06. 12 (금) PM 23:59",
		date: "2026-06-12",
		time: "23:59",
		category: "자기계발",
		isPublic: false,
		memo: undefined,
		tags: ["자기계발", "매주 월, 화"],
	},
];

// dots per date (month-day → colors)
const DOT_MAP: Record<string, string[]> = {
	"6-1": ["#B286FD", "#48CAD9"],
	"6-4": ["#B2F042"],
	"6-8": ["#48CAD9"],
	"6-10": ["#B286FD", "#48CAD9"],
	"6-12": ["#48CAD9"],
	"6-15": ["#B2F042"],
	"6-17": ["#B286FD"],
	"6-18": ["#B286FD", "#48CAD9"],
	"6-19": ["#48CAD9"],
	"6-21": ["#B2F042"],
	"6-22": ["#B2F042"],
	"6-24": ["#B2F042"],
	"6-28": ["#48CAD9"],
	"6-29": ["#B286FD", "#48CAD9"],
	"6-31": ["#B2F042"],
};

// event labels per date
const EVENT_MAP: Record<string, string> = {
	"6-1": "중요 1",
	"6-18": "중요 2",
};

const WEEKDAYS = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

// ─── Calendar ─────────────────────────────────────────────────────────────────

function PlanCalendar() {
	const [current, setCurrent] = useState(new Date(2026, 5, 1)); // June 2026

	const year = current.getFullYear();
	const month = current.getMonth();

	const firstWeekday = new Date(year, month, 1).getDay();
	const daysInMonth = new Date(year, month + 1, 0).getDate();
	const prevMonthDays = new Date(year, month, 0).getDate();

	const today = new Date();

	// Build grid cells
	type Cell =
		| { kind: "prev"; day: number }
		| { kind: "cur"; day: number }
		| { kind: "next"; day: number };

	const cells: Cell[] = [];
	for (let i = firstWeekday - 1; i >= 0; i--)
		cells.push({ kind: "prev", day: prevMonthDays - i });
	for (let d = 1; d <= daysInMonth; d++) cells.push({ kind: "cur", day: d });
	const remaining = 7 - (cells.length % 7);
	if (remaining < 7)
		for (let d = 1; d <= remaining; d++) cells.push({ kind: "next", day: d });

	const isToday = (day: number) =>
		today.getFullYear() === year &&
		today.getMonth() === month &&
		today.getDate() === day;

	const dotKey = (day: number) => `${month + 1}-${day}`;

	const headerLabel = `${year}. ${String(month + 1).padStart(2, "0")}. 01 (${["일", "월", "화", "수", "목", "금", "토"][new Date(year, month, 1).getDay()]}요일)`;

	return (
		<div className="flex flex-col gap-5">
			{/* Month navigation */}
			<div className="flex items-center justify-between">
				<span className="text-xl font-bold">{headerLabel}</span>
				<div className="flex gap-1">
					<button
						type="button"
						onClick={() => setCurrent(new Date(year, month - 1, 1))}
						className="flex h-8 w-8 items-center justify-center rounded-full hover:bg-gray-100"
					>
						<ChevronLeft size={18} />
					</button>
					<button
						type="button"
						onClick={() => setCurrent(new Date(year, month + 1, 1))}
						className="flex h-8 w-8 items-center justify-center rounded-full hover:bg-gray-100"
					>
						<ChevronRight size={18} />
					</button>
				</div>
			</div>

			{/* Grid */}
			<div className="grid grid-cols-7 gap-y-1">
				{/* Weekday headers */}
				{WEEKDAYS.map((d) => (
					<div
						key={d}
						className="py-1 text-center text-xs font-medium text-gray-400"
					>
						{d}
					</div>
				))}

				{/* Day cells */}
				{cells.map((cell, idx) => {
					const isCur = cell.kind === "cur";
					const dots = isCur ? (DOT_MAP[dotKey(cell.day)] ?? []) : [];
					const event = isCur ? EVENT_MAP[dotKey(cell.day)] : undefined;
					const todayMark = isCur && isToday(cell.day);

					return (
						<div
							// biome-ignore lint/suspicious/noArrayIndexKey: static grid
							key={idx}
							className="flex flex-col items-center gap-0.5 py-1"
						>
							<span
								className={`flex h-7 w-7 items-center justify-center rounded-full text-sm ${
									!isCur
										? "text-gray-300"
										: todayMark
											? "bg-[#B286FD] font-semibold text-white"
											: "font-medium text-gray-700 hover:bg-gray-100"
								}`}
							>
								{cell.day}
							</span>
							{/* colored dots */}
							{dots.length > 0 && (
								<div className="flex gap-0.5">
									{dots.map((color, i) => (
										<div
											// biome-ignore lint/suspicious/noArrayIndexKey: static list
											key={i}
											style={{ backgroundColor: color }}
											className="h-1.5 w-1.5 rounded-full"
										/>
									))}
								</div>
							)}
							{/* event label */}
							{event && (
								<span className="rounded-sm bg-[#EDE9FE] px-1.5 py-0.5 text-[10px] font-medium text-[#7C3AED]">
									{event}
								</span>
							)}
						</div>
					);
				})}
			</div>
		</div>
	);
}

// ─── Task List ────────────────────────────────────────────────────────────────

const ACCENT: Record<ItemType, string> = {
	todo: "#48CAD9",
	routine: "#B2F042",
};

function DdayBadge({ dday }: { dday: number }) {
	const label = dday === 0 ? "D-DAY" : `D-${dday}`;
	const isUrgent = dday === 0;
	return (
		<span
			className={`shrink-0 text-sm font-bold ${isUrgent ? "text-[#B286FD]" : "text-gray-400"}`}
		>
			{label}
		</span>
	);
}

const SORT_OPTIONS = ["마감 임박 순", "생성 순", "이름 순"];

interface PlanTaskListProps {
	items: PlanItem[];
	onToggle: (id: number) => void;
	onEdit: (id: number) => void;
	onDelete: (id: number) => void;
	onPostpone: (id: number) => void;
	onAdd: () => void;
}

function PlanTaskList({
	items,
	onToggle,
	onEdit,
	onDelete,
	onPostpone,
	onAdd,
}: PlanTaskListProps) {
	const [tab, setTab] = useState<PlanTab>("전체");
	const [hideCompleted, setHideCompleted] = useState(false);
	const [sortBy, setSortBy] = useState(SORT_OPTIONS[0]);

	const TABS: PlanTab[] = ["전체", "To do", "루틴"];

	const filtered = items.filter((item) => {
		if (hideCompleted && item.completed) return false;
		if (tab === "To do") return item.type === "todo";
		if (tab === "루틴") return item.type === "routine";
		return true;
	});

	return (
		<div className="flex min-h-0 flex-1 flex-col gap-4">
			{/* Tabs */}
			<div className="flex border-b border-gray-100">
				{TABS.map((t) => (
					<button
						key={t}
						type="button"
						onClick={() => setTab(t)}
						className={`px-4 pb-2.5 text-sm font-medium transition-colors ${
							tab === t
								? "border-b-2 border-[#B286FD] text-[#B286FD]"
								: "text-gray-400 hover:text-gray-600"
						}`}
					>
						{t}
					</button>
				))}
			</div>

			{/* Controls */}
			<div className="flex items-center justify-between">
				<button
					type="button"
					onClick={() => setHideCompleted((h) => !h)}
					className={`text-sm transition-colors ${hideCompleted ? "text-[#B286FD]" : "text-gray-400"}`}
				>
					완료 숨기기
				</button>
				<DropdownSelect
					options={SORT_OPTIONS}
					value={sortBy}
					onChange={setSortBy}
				/>
			</div>

			{/* Item list */}
			<div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto pb-1">
				{filtered.map((item) => (
					<div
						key={item.id}
						className="group flex gap-0 overflow-hidden rounded-xl ring-1 ring-gray-100 transition-shadow hover:shadow-sm"
					>
						{/* Left accent bar */}
						<div
							className="w-1 shrink-0"
							style={{ backgroundColor: ACCENT[item.type] }}
						/>

						{/* Content */}
						<div className="flex flex-1 flex-col gap-1.5 px-4 py-3">
							{/* Title row */}
							<div className="flex items-center gap-2">
								<button
									type="button"
									onClick={() => onToggle(item.id)}
									className={`flex h-5 w-5 shrink-0 items-center justify-center rounded border-2 transition-colors ${
										item.completed
											? "border-[#B286FD] bg-[#B286FD]"
											: "border-gray-300"
									}`}
								>
									{item.completed && (
										<svg
											viewBox="0 0 10 8"
											fill="none"
											className="h-3 w-3"
											aria-hidden="true"
										>
											<title>checked</title>
											<path
												d="M1 4L3.5 6.5L9 1"
												stroke="white"
												strokeWidth="1.5"
												strokeLinecap="round"
												strokeLinejoin="round"
											/>
										</svg>
									)}
								</button>
								<span
									className={`flex-1 font-medium ${
										item.completed
											? "text-gray-400 line-through"
											: "text-gray-800"
									}`}
								>
									{item.title}
								</span>
								{item.starred && (
									<Star size={14} className="fill-[#B286FD] text-[#B286FD]" />
								)}
								<DdayBadge dday={item.dday} />
							</div>

							{/* Date */}
							<span className="text-xs text-gray-400">{item.dateLabel}</span>

							{/* Memo */}
							<span className="text-xs text-gray-400">
								{item.memo ?? "(메모 없음)"}
							</span>
							<div className="flex justify-between">
								{/* Tags */}
								{item.tags.length > 0 && (
									<div className="flex flex-wrap gap-1.5">
										{item.tags.map((tag) => (
											<span
												key={tag}
												className="rounded-full flex items-center bg-gray-100 px-2.5 py-0.5 text-xs text-gray-500"
											>
												{tag}
											</span>
										))}
									</div>
								)}
								<div className="flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
									<button
										type="button"
										onClick={() => onPostpone(item.id)}
										className="flex h-7 w-7 items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
										aria-label="미루기"
									>
										<CalendarClock size={14} />
									</button>
									<button
										type="button"
										onClick={() => onEdit(item.id)}
										className="flex h-7 w-7 items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
										aria-label="수정"
									>
										<Pencil size={14} />
									</button>
									<button
										type="button"
										onClick={() => onDelete(item.id)}
										className="flex h-7 w-7 items-center justify-center rounded-lg text-gray-400 transition-colors hover:bg-red-50 hover:text-red-400"
										aria-label="삭제"
									>
										<Trash2 size={14} />
									</button>
								</div>
							</div>
						</div>
					</div>
				))}
			</div>

			{/* 추가 버튼 */}
			<button
				type="button"
				onClick={onAdd}
				className="flex w-full items-center justify-center  rounded-xl bg-[#B286FD] py-3.5 text-sm font-semibold text-white transition-opacity hover:opacity-90"
			>
				<Plus size={15} />
				추가하기
			</button>
		</div>
	);
}

// ─── Page ─────────────────────────────────────────────────────────────────────

const BLANK_ITEM: PlanItem = {
	id: 0,
	type: "todo",
	title: "",
	starred: false,
	completed: false,
	dday: 0,
	dateLabel: "",
	tags: [],
};

export function PlanPage() {
	const [items, setItems] = useState<PlanItem[]>(MOCK_ITEMS);
	const [editingId, setEditingId] = useState<number | null>(null);
	const [isAdding, setIsAdding] = useState(false);

	const editingItem =
		editingId !== null ? items.find((i) => i.id === editingId) : undefined;

	const handleSave = (updated: PlanItem) => {
		if (updated.id === 0) {
			const newId = Math.max(0, ...items.map((i) => i.id)) + 1;
			setItems((prev) => [...prev, { ...updated, id: newId }]);
			setIsAdding(false);
		} else {
			setItems((prev) => prev.map((i) => (i.id === updated.id ? updated : i)));
			setEditingId(null);
		}
	};

	const handleCancel = () => {
		setEditingId(null);
		setIsAdding(false);
	};

	const toggleCompleted = (id: number) =>
		setItems((prev) =>
			prev.map((i) => (i.id === id ? { ...i, completed: !i.completed } : i)),
		);

	const deleteItem = (id: number) =>
		setItems((prev) => prev.filter((i) => i.id !== id));

	const postponeItem = (id: number) => console.log("postpone", id);

	return (
		<main className="flex h-screen flex-col overflow-hidden p-6">
			<div className="flex min-h-0 flex-1 gap-6">
				{/* 좌: 검색 + 캘린더 */}
				<div className="flex w-[520px] shrink-0 flex-col gap-4">
					<div className="flex items-center gap-3 rounded-xl bg-white px-4 py-3 ring-1 ring-gray-200">
						<input
							type="text"
							placeholder="플랜 검색하기"
							className="flex-1 bg-transparent text-sm text-gray-700 outline-none placeholder:text-gray-300"
						/>
						<Search size={18} className="shrink-0 text-gray-300" />
					</div>
					<Card className="flex-1 overflow-y-auto p-6">
						<PlanCalendar />
					</Card>
				</div>

				{/* 우: 목록 or 수정/추가 폼 */}
				<Card className="flex min-h-0 flex-1 flex-col p-6">
					{editingItem || isAdding ? (
						<PlanItemForm
							item={editingItem ?? BLANK_ITEM}
							onSave={handleSave}
							onCancel={handleCancel}
							mode={isAdding ? "create" : "edit"}
						/>
					) : (
						<PlanTaskList
							items={items}
							onToggle={toggleCompleted}
							onEdit={setEditingId}
							onDelete={deleteItem}
							onPostpone={postponeItem}
							onAdd={() => setIsAdding(true)}
						/>
					)}
				</Card>
			</div>
		</main>
	);
}
