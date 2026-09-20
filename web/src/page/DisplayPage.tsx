import { Switch } from '@base-ui/react/switch'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { updateDisplaySetting } from '@/feature/setting/api/mutate'
import { getDisplaySetting } from '@/feature/setting/api/query'
import {
	LANGUAGE_LABELS,
	LANGUAGE_OPTIONS,
	labelToLanguage,
} from '@/feature/setting/model/constants'
import type { DisplaySetting } from '@/feature/setting/model/type'
import { DropdownSelect } from '@/shared/ui/dropdown-select'

const TOGGLE_ROWS: {
	key: keyof Pick<DisplaySetting, 'darkMode' | 'ddayDisplayEnabled'>
	label: string
}[] = [
	{ key: 'darkMode', label: '다크모드' },
	{ key: 'ddayDisplayEnabled', label: '플랜 디데이 표시' },
]

export function DisplayPage() {
	const queryClient = useQueryClient()

	const { data: settingData } = useQuery({
		queryKey: ['setting', 'display'],
		queryFn: getDisplaySetting,
	})
	const setting = settingData?.result === 'success' ? settingData.detail : null

	const { mutate } = useMutation({
		mutationFn: updateDisplaySetting,
		onSuccess: (res) => {
			if (res.result === 'success') {
				queryClient.invalidateQueries({ queryKey: ['setting', 'display'] })
			}
		},
	})

	return (
		<div className="flex max-w-lg flex-1 flex-col gap-1 overflow-y-auto">
			{TOGGLE_ROWS.map(({ key, label }) => (
				<div
					key={key}
					className="flex items-center justify-between rounded-xl bg-white px-5 py-2"
				>
					<span className="text-sm text-gray-700">{label}</span>
					<Switch.Root
						checked={setting?.[key] ?? false}
						onCheckedChange={(checked) => mutate({ [key]: checked })}
						className="relative flex h-6 w-11 items-center rounded-full border-none bg-gray-200 transition-colors outline-none data-[checked]:bg-[#B286FD]"
					>
						<Switch.Thumb className="block h-5 w-5 translate-x-0.5 rounded-full bg-white shadow-sm transition-transform data-[checked]:translate-x-[22px]" />
					</Switch.Root>
				</div>
			))}

			{/* 아직 서비스 적용은 안됌 */}
			<div className="flex items-center justify-between rounded-xl bg-white px-5 py-2">
				<span className="text-sm text-gray-700">언어</span>
				<DropdownSelect
					options={LANGUAGE_OPTIONS}
					value={LANGUAGE_LABELS[setting?.language ?? 'system']}
					onChange={(label) => mutate({ language: labelToLanguage(label) })}
				/>
			</div>
		</div>
	)
}
