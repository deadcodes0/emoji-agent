<script setup lang="ts">
</script>

<template>
  <header class="toolbar">
    <div class="toolbar__title">
      <h1>情绪日志</h1>
      <p>当天全部情绪 · 一览无余</p>
    </div>
    <div class="toolbar__spacer" />
  </header>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.toolbar__title h1 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1.2px;
  color: var(--text);
  transition: color 0.6s ease;
}

.toolbar__title p {
  margin: 2px 0 0;
  font-size: 11px;
  letter-spacing: 0.5px;
  color: var(--text-sub);
  transition: color 0.6s ease;
}

.toolbar__spacer {
  flex: 1;
}
</style>
