// 서버 dto/spirit 기준 (2026-11-25). 정령(게임) 기획이 확정 전이라 화면에서 쓰지 않는 값은 받기만 한다

// 서버 AssetResponse. 웹은 url로 렌더링한다 (웹용 에셋이 아직 없으면 url·fileName이 null)
export interface SpiritAsset {
	key: string
	url: string | null
	fileName: string | null
}

// 서버 SpiritTrait
export type SpiritTrait =
	| 'CURIOUS'
	| 'EASYGOING'
	| 'ACTIVE'
	| 'SOCIABLE'
	| 'CAUTIOUS'
	| 'METICULOUS'
	| 'PLAYFUL'
	| 'INDEPENDENT'

// GET api/spirits — 정령도감 (전체 정령 종류 + 보유 여부). 서버 SpiritCollectionResponse
// 보유하지 않은 종류는 isOwned = false이고 spiritId 이하 정령 개체 값이 모두 null
export interface Spirit {
	speciesId: number
	speciesName: string
	speciesDescription: string | null
	image: SpiritAsset | null
	isOwned: boolean
	spiritId: number | null
	spiritName: string | null
	// 게임 값 (호감도 0~100%, 단계 1~5). 받기만 하고 화면에서는 쓰지 않는다
	affection: number | null
	affectionLevel: number | null
	isImportant: boolean | null
	isRepresentative: boolean | null
	// 서버 응답에서 제거됨 (정령 단계·특성별 경험치·imageUrl·representative)
	// id: number
	// stage: number
	// exp: number
	// focusExp: number
	// energyExp: number
	// consistencyExp: number
	// creativityExp: number
	// imageUrl: string
	// representative: boolean
}

export type SpiritList = Spirit[]

// GET api/spirits/current — 대표 정령. 서버 RepresentativeSpiritResponse
export interface RepresentativeSpirit {
	// 정령 개체 id (도감의 spiritId와 같음)
	id: number
	spiritName: string
	// 게임 값 (누적 경험치, 상한 없음 — 서버 Spirit.addExp). 화면에는 누적 숫자만 보여준다
	// todo: 정령 성장 기획이 나오면 진행률 등 표시 방식 다시 정하기
	exp: number
	image: SpiritAsset | null
	// 게임 값 (정령 특성). 받기만 하고 화면에서는 쓰지 않는다
	trait: SpiritTrait[]
	// 서버 응답에서 제거됨 (정령 단계·특성별 경험치·imageUrl)
	// stage: number
	// focusExp: number
	// vitalityExp: number
	// consistencyExp: number
	// creativityExp: number
	// imageUrl: string
}
