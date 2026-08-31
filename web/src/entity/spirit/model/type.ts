// GET /api/spirit/list — 보유 정령 목록
export interface Spirit {
	id: number
	spiritName: string
	stage: number
	exp: number
	focusExp: number
	energyExp: number
	consistencyExp: number
	creativityExp: number
	imageUrl: string
	representative: boolean
}

export type SpiritList = Spirit[]

// GET /api/spirit/representative — 대표 정령 단일 조회
export interface RepresentativeSpirit {
	id: number
	spiritName: string
	stage: number
	exp: number
	focusExp: number
	vitalityExp: number
	consistencyExp: number
	creativityExp: number
	imageUrl: string
}
