<!-- src/components/backgrounds/DayBg.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Dust {
  size: number; left: number; dx: number; delay: number; duration: number
}

const dusts = ref<Dust[]>([])
onMounted(() => {
  dusts.value = Array.from({ length: 18 }, () => ({
    size: Math.random() * 10 + 4,
    left: Math.random() * 100,
    dx: Math.random() * 80 - 40,
    delay: -Math.random() * 16,
    duration: 12 + Math.random() * 10
  }))
})
</script>

<template>
  <div class="bg-day">
    <div class="sun"></div>
    <div class="glow g1"></div>
    <div class="glow g2"></div>
    <div class="glow g3"></div>
    <div class="wave"></div>
    <div class="wave w2"></div>

    <div
      v-for="(d, i) in dusts"
      :key="i"
      class="dust"
      :style="{
        width: d.size + 'px',
        height: d.size + 'px',
        left: d.left + 'vw',
        '--dx': d.dx + 'px',
        animationDelay: d.delay + 's',
        animationDuration: d.duration + 's'
      }"
    />
  </div>
</template>

<style scoped>
.bg-day {
  position: absolute; inset: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF5E6 0%, #FFE8D6 50%, #FFD9C0 100%);
}

.glow { position: absolute; border-radius: 50%; filter: blur(70px); opacity: .55; will-change: transform; }
.glow.g1 { width: 420px; height: 420px; background: #FFB88C; top: -100px; left: -100px;
           animation: floatA 18s ease-in-out infinite; }
.glow.g2 { width: 320px; height: 320px; background: #FFD3A5; bottom: -80px; right: -80px;
           animation: floatB 22s ease-in-out infinite; }
.glow.g3 { width: 280px; height: 280px; background: #FFC9B0; top: 45%; left: 62%;
           animation: floatC 26s ease-in-out infinite; }
@keyframes floatA {
  0%,100% { transform: translate(0,0) scale(1); }
  33%     { transform: translate(60px,-40px) scale(1.12); }
  66%     { transform: translate(-40px,40px) scale(.94); }
}
@keyframes floatB {
  0%,100% { transform: translate(0,0) scale(1); }
  50%     { transform: translate(-70px,-50px) scale(1.15); }
}
@keyframes floatC {
  0%,100% { transform: translate(0,0) scale(1); }
  40%     { transform: translate(-50px,30px) scale(1.08); }
  75%     { transform: translate(30px,-40px) scale(.96); }
}

.sun {
  position: absolute; top: -60px; left: 50%; transform: translateX(-50%);
  width: 260px; height: 260px; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,220,160,.85), rgba(255,200,140,0) 70%);
  animation: sunBreathe 6s ease-in-out infinite;
}
@keyframes sunBreathe {
  0%,100% { transform: translateX(-50%) scale(1);    opacity: .8; }
  50%     { transform: translateX(-50%) scale(1.12); opacity: 1; }
}

.dust {
  position: absolute; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,235,200,.95), rgba(255,235,200,0));
  bottom: -30px;
  animation: driftUp linear infinite;
}
@keyframes driftUp {
  0%   { transform: translate(0,0) scale(.6); opacity: 0; }
  15%  { opacity: .9; }
  85%  { opacity: .7; }
  100% { transform: translate(var(--dx,20px), -110vh) scale(1.1); opacity: 0; }
}

.wave {
  position: absolute; left: 0; bottom: 0; width: 200%; height: 180px;
  background: radial-gradient(ellipse at 50% 100%, rgba(255,180,140,.28), transparent 70%);
  animation: waveMove 14s linear infinite;
}
.wave.w2 {
  height: 240px;
  background: radial-gradient(ellipse at 50% 100%, rgba(255,160,120,.18), transparent 70%);
  animation-duration: 20s; animation-direction: reverse;
}
@keyframes waveMove { 0%{transform:translateX(0)} 100%{transform:translateX(-50%)} }

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; }
}
</style>