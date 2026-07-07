import ky from 'ky'

export const api = ky.create({
  prefix: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})
