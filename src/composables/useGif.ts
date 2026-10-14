// src/composables/useGif.ts
// 统一解析 src/assets/gif 下的所有 gif，提供 name → URL 查询。
// 任何地方（首页、切换器、未来的组件）都从这里取，不再各自写一遍 glob。

const modules = import.meta.glob('../assets/gif/*.gif', {
  eager: true,
  import: 'default',
}) as Record<string, string>

// { happy: '/assets/happy-a1b2.gif', blink: '...', ... }
export const GIFS: Record<string, string> = Object.fromEntries(
  Object.entries(modules).map(([path, url]) => [
    path.split('/').pop()!.replace('.gif', ''),
    url,
  ])
)

export function getGif(name: string): string {
  const url = GIFS[name]
  if (!url && import.meta.env.DEV) {
    console.warn(
      `[useGif] 找不到 gif "${name}"，请确认 src/assets/gif/${name}.gif 存在`
    )
  }
  return url ?? ''
}