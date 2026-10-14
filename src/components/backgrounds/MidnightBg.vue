<!-- src/components/backgrounds/MidnightBg.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useStarfield } from '@/composables/useStarfield'

const canvasRef = ref<HTMLCanvasElement | null>(null)

useStarfield(canvasRef, {
  density: 16000,
  bigChance: 0.04,
  meteorChance: 0.0008,
  maxMeteors: 1,
  starColor: '#D8D8FF',
  bigColor: '#EEEEFF'
})
</script>

<template>
  <div class="bg-midnight">
    <canvas ref="canvasRef" class="starfield" />
    <div class="breath"></div>
    <div class="mist"></div>
    <div class="mist m2"></div>
  </div>
</template>

<style scoped>
.bg-midnight {
  position: absolute; inset: 0;
  overflow: hidden;
  background: radial-gradient(ellipse at 50% 100%, #1a1a3e 0%, #0d0d24 45%, #050510 100%);
}
.starfield { position: absolute; inset: 0; display: block; }

.breath {
  position: absolute; left: 50%; bottom: 10%;
  width: 700px; height: 400px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(120,110,220,.22), rgba(120,110,220,0) 70%);
  animation: deepBreath 12s ease-in-out infinite;
}
@keyframes deepBreath {
  0%,100% { transform: translateX(-50%) scale(1);    opacity: .55; }
  50%     { transform: translateX(-50%) scale(1.18); opacity: .9; }
}
.mist {
  position: absolute; left: 0; bottom: 0;
  width: 200%; height: 220px;
  background: radial-gradient(ellipse at 50% 100%, rgba(90,100,180,.18), transparent 70%);
  animation: mistMove 40s linear infinite;
}
.mist.m2 {
  height: 300px;
  background: radial-gradient(ellipse at 50% 100%, rgba(70,80,160,.12), transparent 70%);
  animation-duration: 56s; animation-direction: reverse;
}
@keyframes mistMove { 0%{transform:translateX(0)} 100%{transform:translateX(-50%)} }

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; }
}
</style>