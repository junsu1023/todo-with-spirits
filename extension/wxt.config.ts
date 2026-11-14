import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'wxt'

// See https://wxt.dev/api/reference/wxt/interfaces/inlineconfig.html
export default defineConfig({
  srcDir: 'src',
  publicDir: 'public',
  modules: ['@wxt-dev/module-react'],
  vite: () => ({
    plugins: [tailwindcss()],
  }),
  manifest: {
    name: 'Todo with Spirits',
    description: '브라우저 사이드패널에서 바로 쓰는 todo-with-spirits',
    permissions: [
      'sidePanel',
      'storage',
      'declarativeNetRequestWithHostAccess',
    ],
    host_permissions: [
      'https://custom-lucky-draw.store/*',
      'https://todo-with-spirits.vercel.app/*',
    ],
    // 툴바 아이콘. 클릭 시 사이드패널을 연다 (background.ts)
    action: {
      default_title: 'Todo with Spirits 열기',
    },
  },
})
