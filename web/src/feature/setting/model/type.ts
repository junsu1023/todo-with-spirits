export type Language = 'system' | 'ko' | 'en' | 'ja' | 'zh'

export interface DisplaySetting {
	darkMode: boolean
	ddayDisplayEnabled: boolean
	language: Language
}

export interface UpdateDisplaySettingRequest {
	darkMode?: boolean
	ddayDisplayEnabled?: boolean
	language?: Language
}
