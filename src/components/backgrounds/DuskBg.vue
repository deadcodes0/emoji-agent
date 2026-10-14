<!-- src/components/backgrounds/DuskBg.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Spark {
  size: number; left: number; dx: number; delay: number; duration: number
}

const sparks = ref<Spark[]>([])
onMounted(() => {
  sparks.value = Array.from({ length: 16 }, () => ({
    size: Math.random() * 8 + 3,
    left: Math.random() * 100,
    dx: Math.random() * 100 - 50,
    delay: -Math.random() * 14,
    duration: 11 + Math.random() * 9
  }))
})
</script>

<template>
  <div class="bg-dusk">
    <div class="cloud c1"></div>
    <div class="cloud c2"></div>
    <div class="cloud c3"></div>
    <div class="sunset"></div>

    <div
      v-for="(s, i) in sparks"
      :key="i"
      class="spark"
      :style="{
        width: s.size + 'px',
        height: s.size + 'px',
        left: s.left + 'vw',
        '--dx': s.dx + 'px',
        animationDelay: s.delay + 's',
        animationDuration: s.duration + 's'
      }"
    />
  </div>
</template>

<style scoped>
.bg-dusk {
  position: absolute; inset: 0;
  overflow: hidden;
  background: linear-gradient(180deg,
    #FFC48C 0%, #FF9A8B 22%, #E88BA0 45%,
    #B87AB0 68%, #7A6BA8 88%, #5A5A8C 100%);
}

.cloud {
  position: absolute; border-radius: 50%;
  filter: blur(40px); opacity: .55;
  mix-blend-mode: screen; will-change: transform;
}
.cloud.c1 { width: 520px; height: 140px; background: #FFD6A8; top: 18%; left: -20%;
            animation: cloudMove 38s linear infinite; }
.cloud.c2 { width: 680px; height: 180px; background: #FFA8B8; top: 42%; left: -30%;
            animation: cloudMove 52s linear infinite; animation-delay: -12s; }
.cloud.c3 { width: 460px; height: 120px; background: #D8A0E8; top: 62%; left: -25%;
            animation: cloudMove 44s linear infinite; animation-delay: -24s; }
@keyframes cloudMove {
  0%   { transform: translateX(0); }
  100% { transform: translateX(160vw); }
}

.sunset {
  position: absolute; right: -80px; bottom: -80px;
  width: 420px; height: 420px; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,190,120,.75), rgba(255,140,100,0) 70%);
  animation: sunsetPulse 9s ease-in-out infinite;
}
@keyframes sunsetPulse {
  0%,100% { transform: scale(1);   opacity: .75; }
  50%     { transform: scale(1.1); opacity: 1; }
}

.spark {
  position: absolute; border-radius: 50%;
  background: radial-gradient(circle, rgba(255,220,180,.9), rgba(255,220,180,0));
  bottom: -30px;
  animation: sparkRise linear infinite;
}
@keyframes sparkRise {
  0%   { transform: translate(0,0) scale(.5); opacity: 0; }
  20%  { opacity: .85; }
  80%  { opacity: .6; }
  100% { transform: translate(var(--dx,30px), -105vh) scale(1.1); opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  * { animation: none !important; }
}
</style>