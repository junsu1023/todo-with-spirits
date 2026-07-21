import ky from 'ky'

export const apiClient = ky.create({
  baseUrl:
    import.meta.env.VITE_API_BASE_URL ??
    'https://todo-with-spirits.onrender.com',
  headers: {
    'Content-Type': 'application/json',
  },
})
