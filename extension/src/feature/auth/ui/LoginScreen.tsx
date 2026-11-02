import { WEB_URL } from '@/lib/config'

export function LoginScreen() {
  const openWebLogin = () => browser.tabs.create({ url: `${WEB_URL}/login` })

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
      <p className="text-sm text-neutral-600">
        웹에서 로그인하면
        <br />
        여기에서도 자동으로 로그인돼요.
      </p>
      <button
        type="button"
        onClick={openWebLogin}
        className="rounded-md bg-neutral-900 px-4 py-2 text-sm text-white"
      >
        웹에서 로그인
      </button>
      <p className="text-xs text-neutral-400">
        이미 로그인했다면 웹 페이지를 한 번 열어 주세요.
      </p>
    </div>
  )
}
