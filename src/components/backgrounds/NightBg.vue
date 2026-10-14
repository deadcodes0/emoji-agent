<!-- src/components/backgrounds/NightBg.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useStarfield } from '@/composables/useStarfield'

const canvasRef = ref<HTMLCanvasElement | null>(null)

useStarfield(canvasRef, {
  density: 6500,
  bigChance: 0.08,
  meteorChance: 0.004,
  maxMeteors: 2,
  starColor: '#FFFFFF',
  bigColor: '#FFFBE8'
})
</script>

<template>
  <div class="bg-night">
    <canvas ref="canvasRef" class="starfield" />
    <div class="moon"></div>
    <div class="mist"></div>
    <div class="mist m2"></div>
  </div>
</template>

<style scoped>
.bg-night {
  position: absolute; inset: 0;
  overflow: hidden;
  background: radial-gradient(ellipse at 50% 0%, #2b2d5e 0%, #1a1a3e 40%, #0d0d24 100%);
}
.starfield { position: absolute; inset: 0; display: block; }

.moon {
  position: absolute; top: 8%; right: 12%;
  width: 90px; height: 90px; border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #FFF8E7, #FFE9B0 70%);
  animation: moonFloat 8s ease-in-out infinite;
}
.moon::after {
  content: ''; position: absolute; inset: -30px; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,233,176,.35), transparent 70%);
  animation: moonHalo 6s ease-in-out infinite;
  z-index: -1;
}
@keyframes moonFloat {
  0%,100% { transform: translateY(0); }
  50%     { transform: translateY(-12px); }
}
@keyframes moonHalo {
  0%,100% { transform: scale(1);    opacity: .6; }
  50%     { transform: scale(1.25); opacity: 1; }
}

.mist {
  position: absolute; left: 0; bottom: 0;
  width: 200%; height: 220px;
  background: radial-gradient(ellipse at 50% 100%, rgba(120,140,220,.25), transparent 70%);
  animation: mistMove 22s linear infinite;
}
.mist.m2 {
  height: 300px;
  background: radial-gradient(ellipse at 50% 100%, rgba(90,110,200,.18), transparent 70%);
  animation-duration: 32s; animation-direction: reverse;
}
@keyframes mistMove { 0%{transform:translateX(0)} 100%{transform:translateX(-50%)} }

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; }
}
</style>