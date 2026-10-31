export function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

export function formatDay(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}