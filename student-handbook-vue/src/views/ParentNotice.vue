<template>
  <div class="notice-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
          <path d="M8.354 1.146a.5.5 0 0 0-.708 0l-6 6A.5.5 0 0 0 1.5 7.5v7a.5.5 0 0 0 .5.5h4.5a.5.5 0 0 0 .5-.5V14h3v1.5a.5.5 0 0 0 .5.5H14a.5.5 0 0 0 .5-.5v-7a.5.5 0 0 0-.146-.354L13 5.793V2.5a.5.5 0 0 0-.5-.5h-1a.5.5 0 0 0-.5.5v1.293L8.354 1.146ZM11.5 14v-6h-3v6h3Z"/>
        </svg>
        返回首頁
      </button>
    </div>

    <!-- 通知列表 -->
    <div class="notice-list" v-if="!loading && noticeList.length > 0">
      <div 
        class="notice-item" 
        v-for="notice in noticeList" 
        :key="notice.notificationId"
        @click="viewDetail(notice.notificationId)"
      >
        <div class="notice-header">
          <h3 class="notice-title">{{ notice.title }}</h3>
          <span class="notice-date">{{ formatDate(notice.createTime) }}</span>
        </div>
        <p class="notice-content">{{ truncateContent(notice.content, 100) }}</p>
        <div class="notice-footer">
          <span class="sender-name">發送人：{{ notice.senderName }}</span>
          <span class="view-detail">查看詳情 →</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!loading && noticeList.length === 0">
      <div class="empty-icon">📭</div>
      <p class="empty-text">暫無通知</p>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'

export default {
  name: 'ParentNotice',
  data() {
    return {
      noticeList: [],
      loading: false
    }
  },
  mounted() {
    this.loadNoticeList()
  },
  methods: {
    // 返回上一页
    goBack() {
      this.$router.push('/')
    },

    // 加载通知列表
    async loadNoticeList() {
      this.loading = true
      try {
        const response = await service.get(API_ENDPOINTS.NOTICE_LIST)
        if (response.data.code === 200) {
          this.noticeList = response.data.data || []
        } else {
          ElMessage.error(response.data.msg || '獲取通知列表失敗')
        }
      } catch (error) {
        console.error('獲取通知列表失敗:', error)
        ElMessage.error('網絡錯誤，請稍後重試')
      } finally {
        this.loading = false
      }
    },

    // 查看详情
    viewDetail(notificationId) {
      this.$router.push(`/notice/${notificationId}`)
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 截断内容
    truncateContent(content, maxLength) {
      if (!content) return ''
      if (content.length <= maxLength) return content
      return content.substring(0, maxLength) + '...'
    }
  }
}
</script>

<style scoped>
.notice-container {
  min-height: 100vh;
  background-color: #f5f9ff;
  padding: 0;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  color: #92400e;
  border: none;
  box-shadow: 0 4px 6px rgba(245, 158, 11, 0.2);
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.back-button:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #fcd34d 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(245, 158, 11, 0.3);
}

.back-button svg {
  width: 16px;
  height: 16px;
}

/* 通知列表 */
.notice-list {
  padding: 20px 25px;
  max-width: 1200px;
  margin: 0 auto;
}

.notice-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  position: relative;
  overflow: visible;
}

.notice-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #7dd3fc, #bae6fd, #bae6fd);
  z-index: 1;
}

.notice-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 20px rgba(125, 211, 252, 0.2);
}

.notice-item:active {
  transform: translateY(-4px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 15px;
}

.notice-title {
  flex: 1;
  font-size: 18px;
  font-weight: 700;
  color: #0284c7;
  margin: 0;
  line-height: 1.5;
}

.notice-date {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
  font-weight: 500;
}

.notice-content {
  font-size: 15px;
  color: #475569;
  line-height: 1.8;
  margin: 0 0 15px 0;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #e0f2fe;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e0f2fe;
}

.sender-name {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.view-detail {
  font-size: 14px;
  color: #67c23a;
  font-weight: 600;
  transition: all 0.3s ease;
}

.notice-item:hover .view-detail {
  color: #4caf50;
  transform: translateX(3px);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 15px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e4e7ed;
  border-top-color: #67c23a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 14px;
  color: #909399;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 12px 15px;
  }

  .back-button {
    padding: 10px 14px;
    font-size: 14px;
  }

  .notice-list {
    padding: 15px;
  }

  .notice-item {
    padding: 16px;
  }

  .notice-title {
    font-size: 16px;
  }

  .notice-content {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
