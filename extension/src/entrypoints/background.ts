import { API_HOST, ORIGIN_OVERRIDE } from '@/lib/config'

// 익스텐션의 API 요청 Origin을 웹 도메인으로 바꾸는 규칙 ID
const CORS_RULE_ID = 1

export default defineBackground(() => {
  // 툴바 아이콘을 누르면 팝업 대신 사이드패널이 열리도록 설정
  browser.sidePanel
    .setPanelBehavior({ openPanelOnActionClick: true })
    .catch((error) => console.error(error))

  registerCorsRule().catch((error) => console.error(error))
})

/**
 * 백엔드 CORS 허용 목록에 chrome-extension:// Origin이 없어서 403이 난다.
 * 임시로 익스텐션이 보내는 API 요청의 Origin 헤더를 허용된 웹 도메인으로 바꾼다.
 * 🔧 개선 필요: 백엔드에 익스텐션 Origin이 추가되면 이 규칙을 제거한다. (docs/spec.md 9번)
 */
async function registerCorsRule() {
  await browser.declarativeNetRequest.updateSessionRules({
    removeRuleIds: [CORS_RULE_ID],
    addRules: [
      {
        id: CORS_RULE_ID,
        priority: 1,
        action: {
          type: 'modifyHeaders',
          requestHeaders: [
            { header: 'origin', operation: 'set', value: ORIGIN_OVERRIDE },
          ],
        },
        condition: {
          requestDomains: [API_HOST],
          // 탭이 아닌 곳(사이드패널, background)에서 보낸 요청만. 웹 탭의 요청은 건드리지 않는다.
          tabIds: [-1],
        },
      },
    ],
  })
}
