// src/composables/useStarfield.ts
import { onMounted, onBeforeUnmount, type Ref } from 'vue'

export interface StarfieldOptions {
  density?: number        // 每多少像素一颗星，越小越密
  bigChance?: number      // 大星比例
  meteorChance?: number   // 每帧生成流星概率
  maxMeteors?: number     // 同屏流星上限
  starColor?: string
  bigColor?: string
}

interface Star {
  x: number; y: number; r: number
  phase: number; speed: number
  vx: number; vy: number
  big: boolean
}

interface Meteor {
  x: number; y: number
  vx: number; vy: number
  len: number; life: number
}

class Starfield {
  private canvas: HTMLCanvasElement
  private ctx: CanvasRenderingContext2D
  private opts: Required<StarfieldOptions>
  private DPR: number
  private W = 0
  private H = 0
  private stars: Star[] = []
  private meteors: Meteor[] = []
  private t = 0
  private rafId: number | null = null
  private running = false
  private onResize = () => this.resize()

  constructor(canvas: HTMLCanvasElement, options: StarfieldOptions) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')!
    this.opts = {
      density: 6500,
      bigChance: 0.08,
      meteorChance: 0.004,
      maxMeteors: 2,
      starColor: '#FFFFFF',
      bigColor: '#FFFBE8',
      ...options
    }
    this.DPR = Math.min(window.devicePixelRatio || 1, 2)
    window.addEventListener('resize', this.onResize)
    this.resize()
  }

  private resize() {
    const W = window.innerWidth
    const H = window.innerHeight
    this.W = W; this.H = H
    this.canvas.width = W * this.DPR
    this.canvas.height = H * this.DPR
    this.canvas.style.width = W + 'px'
    this.canvas.style.height = H + 'px'
    this.ctx.setTransform(this.DPR, 0, 0, this.DPR, 0, 0)

    const count = Math.max(40, Math.floor((W * H) / this.opts.density))
    this.stars = Array.from({ length: count }, () => this.spawnStar())
  }

  private spawnStar(): Star {
    const big = Math.random() < this.opts.bigChance
    return {
      x: Math.random() * this.W,
      y: Math.random() * this.H,
      r: big ? Math.random() * 1.6 + 1.2 : Math.random() * 1.1 + 0.3,
      phase: Math.random() * Math.PI * 2,
      speed: Math.random() * 0.02 + 0.008,
      vx: (Math.random() - 0.5) * 0.06,
      vy: (Math.random() - 0.5) * 0.04,
      big
    }
  }

  private spawnMeteor() {
    const fromLeft = Math.random() < 0.5
    const startX = fromLeft ? -50 : Math.random() * this.W
    const startY = fromLeft ? Math.random() * this.H * 0.5 : -50
    const angle = Math.PI / 4 + (Math.random() - 0.5) * 0.3
    const speed = 7 + Math.random() * 4
    this.meteors.push({
      x: startX, y: startY,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      len: 80 + Math.random() * 60,
      life: 1
    })
  }

  private drawStar(s: Star) {
    const { ctx } = this
    const twinkle = 0.35 + Math.abs(Math.sin(s.phase + this.t * s.speed)) * 0.65
    ctx.globalAlpha = twinkle
    ctx.fillStyle = s.big ? this.opts.bigColor : this.opts.starColor
    ctx.beginPath()
    ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
    ctx.fill()

    if (s.big) {
      ctx.globalAlpha = twinkle * 0.55
      ctx.strokeStyle = this.opts.bigColor
      ctx.lineWidth = 0.8
      const L = s.r * 4
      ctx.beginPath()
      ctx.moveTo(s.x - L, s.y); ctx.lineTo(s.x + L, s.y)
      ctx.moveTo(s.x, s.y - L); ctx.lineTo(s.x, s.y + L)
      ctx.stroke()
    }
    ctx.globalAlpha = 1
  }

  private drawMeteor(m: Meteor) {
    const { ctx } = this
    const grad = ctx.createLinearGradient(
      m.x, m.y,
      m.x - m.vx * (m.len / 10),
      m.y - m.vy * (m.len / 10)
    )
    grad.addColorStop(0, `rgba(255,255,255,${m.life})`)
    grad.addColorStop(1, 'rgba(255,255,255,0)')
    ctx.strokeStyle = grad
    ctx.lineWidth = 2
    ctx.lineCap = 'round'
    ctx.beginPath()
    ctx.moveTo(m.x, m.y)
    ctx.lineTo(m.x - m.vx * (m.len / 10), m.y - m.vy * (m.len / 10))
    ctx.stroke()
  }

  private loop = () => {
    if (!this.running) return
    this.t += 0.016
    const { ctx, W, H } = this
    ctx.clearRect(0, 0, W, H)

    for (const s of this.stars) {
      this.drawStar(s)
      s.x += s.vx; s.y += s.vy
      if (s.x < -5) s.x = W + 5
      if (s.x > W + 5) s.x = -5
      if (s.y < -5) s.y = H + 5
      if (s.y > H + 5) s.y = -5
    }

    if (Math.random() < this.opts.meteorChance && this.meteors.length < this.opts.maxMeteors) {
      this.spawnMeteor()
    }
    for (let i = this.meteors.length - 1; i >= 0; i--) {
      const m = this.meteors[i]
      if (!m) continue
      m.x += m.vx; m.y += m.vy; m.life -= 0.008
      this.drawMeteor(m)
      if (m.life <= 0 || m.x > W + 100 || m.y > H + 100) this.meteors.splice(i, 1)
    }

    this.rafId = requestAnimationFrame(this.loop)
  }

  start() {
    if (this.running) return
    this.running = true
    this.rafId = requestAnimationFrame(this.loop)
  }

  stop() {
    this.running = false
    if (this.rafId !== null) cancelAnimationFrame(this.rafId)
    this.rafId = null
    this.ctx.clearRect(0, 0, this.W, this.H)
  }

  destroy() {
    this.stop()
    window.removeEventListener('resize', this.onResize)
  }
}

export function useStarfield(
  canvasRef: Ref<HTMLCanvasElement | null>,
  options: StarfieldOptions = {}
) {
  let field: Starfield | null = null

  const onVisibility = () => {
    if (!field) return
    document.hidden ? field.stop() : field.start()
  }

  onMounted(() => {
    if (!canvasRef.value) return
    field = new Starfield(canvasRef.value, options)
    field.start()
    document.addEventListener('visibilitychange', onVisibility)
  })

  onBeforeUnmount(() => {
    document.removeEventListener('visibilitychange', onVisibility)
    field?.destroy()
    field = null
  })
}