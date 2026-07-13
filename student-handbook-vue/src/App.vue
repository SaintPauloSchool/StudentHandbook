<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <keep-alive include="ParentNotice">
        <component :is="Component" />
      </keep-alive>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style>
html, body {
  background-color: #f5f9ff !important; /* 使用項目記憶中的柔和淺藍灰色調 */
  margin: 0;
  padding: 0;
  font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; /* 現代化字體 */
}

#app {
  font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 0;
  background-color: #f5f9ff !important; /* 使用項目記憶中的柔和淺藍灰色調 */
  min-height: 100vh;
  padding-top: 0; /* 移除頂部內邊距 */
}

/* 學生資訊標籤（全局統一樣式） */
.student-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 14px;
  background: #f8fafc;
  box-shadow: inset 0 0 0 1px #e2e8f0;
  max-width: 100%;
  min-width: 0;
  box-sizing: border-box;
  user-select: none;
}

.student-chip__text {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  min-width: 0;
}

.student-chip__name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.3;
  max-width: 100%;
  text-align: center;
  word-break: break-all;
}

.student-chip__class {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 700;
  color: #1d4ed8;
  background: #dbeafe;
  padding: 1px 6px;
  border-radius: 4px;
  letter-spacing: 0.03em;
  line-height: 1.4;
}
</style>