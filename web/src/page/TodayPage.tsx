import { Card } from '@/shared/ui/card'
import { TodayAchievementCard } from './TodayAchievementCard'
import { TodayDateHeader } from './TodayDateHeader'
import { TodayTodoCard } from './TodayTodoCard'

export function TodayPage() {
  return (
    <main className="flex h-screen flex-col overflow-hidden p-6">
      <div className="flex min-h-0 flex-1 gap-6">
        {/* 좌: 정령 + 달성률 */}
        <div className="flex w-[260px] shrink-0 flex-col gap-4">
          <Card className="flex flex-col items-center gap-4 p-6">
            <div className="h-[120px] w-[120px] rounded-xl bg-gray-500" />
            <div className="flex w-full flex-col gap-3">
              <span className="text-lg font-bold leading-snug text-[#B286FD]">
                벌써 절반 왔어요!
                <br />
                루미랑 조금 더 힘내봐요
              </span>
              <div className="flex flex-col gap-2">
                <span className="text-sm text-[#8F8170]">루미</span>
                <div className="h-1 w-full rounded-full bg-gray-200" />
                <div className="rounded-md bg-[#F3E8FF] px-3 py-2">
                  <span className="text-sm font-medium text-[#6B42E2]">
                    오늘의 성장 포인트
                  </span>
                </div>
              </div>
            </div>
          </Card>
          <TodayAchievementCard percentage={100} />
        </div>

        {/* 우: 날짜 헤더 + 할 일 */}
        <div className="flex min-h-0 flex-1 flex-col gap-4">
          <TodayDateHeader />
          <TodayTodoCard />
        </div>
      </div>
    </main>
  )
}
