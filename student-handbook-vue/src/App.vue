<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script setup>
// 移除直接導入的StudentHandbook組件
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

/* 全新设计 - 现代化学生选择对话框样式 */
.custom-student-dialog .el-dialog {
  border-radius: 24px !important;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(21, 128, 221, 0.4) !important;
  border: 2px solid rgba(96, 165, 250, 0.6) !important;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(240, 249, 255, 0.98)) !important;
  backdrop-filter: blur(20px) !important;
  position: relative !important;
  border-image: linear-gradient(135deg, rgba(96, 165, 250, 0.8), rgba(59, 130, 246, 0.8)) 1 !important;
}

/* 专门针对关闭按钮的样式 */
.custom-student-dialog .el-dialog__headerbtn {
  top: 18px !important;
  right: 20px !important;
  width: 32px !important;
  height: 32px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 50% !important;
  background: rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(5px) !important;
  transition: all 0.3s ease !important;
  z-index: 10 !important;
}

.custom-student-dialog .el-dialog__headerbtn:hover {
  background: rgba(255, 255, 255, 0.6) !important;
  transform: scale(1.1) !important;
}

.custom-student-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 18px !important;
  font-weight: bold !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.custom-student-dialog .el-dialog__header {
  background: linear-gradient(135deg, #2563eb, #1d4ed8, #1e40af) !important;
  padding: 28px 32px !important;
  border-radius: 22px 22px 0 0 !important;
  position: relative !important;
  text-align: center !important;
}

.custom-student-dialog .el-dialog__title {
  color: white !important;
  font-weight: 700 !important;
  font-size: 20px !important;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3) !important;
}

.custom-student-dialog .el-dialog__body {
  padding: 35px !important;
  background: transparent !important;
}

/* 移动设备对话框样式 */
@media (max-width: 768px) {
  .custom-student-dialog .el-dialog {
    width: 92% !important;
    margin-top: 18vh !important;
    max-height: 75vh;
    border-radius: 20px !important;
  }
  
  .custom-student-dialog .el-dialog__header {
    padding: 22px 26px !important;
  }
  
  .custom-student-dialog .el-dialog__body {
    padding: 30px !important;
  }
  
  .custom-student-dialog .el-dialog__title {
    font-size: 18px !important;
  }
  
  .custom-student-dialog .el-dialog__headerbtn {
    width: 30px !important;
    height: 30px !important;
    top: 15px !important;
  }
}

/* 覆盖Element Plus默认背景 */
.custom-student-dialog .el-overlay {
  background: linear-gradient(135deg, rgba(21, 94, 236, 0.7), rgba(12, 74, 160, 0.8)) !important;
}

/* 对话框动画效果 */
.custom-student-dialog .el-dialog {
  animation: dialogScaleIn 0.5s cubic-bezier(0.23, 1, 0.32, 1) !important;
}

@keyframes dialogScaleIn {
  from {
    transform: scale(0.7) translateY(30px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}
</style>