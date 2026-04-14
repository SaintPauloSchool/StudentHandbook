<template>
  <div class="notice-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon class="back-icon"><HomeFilled /></el-icon>
        返回首頁
      </button>
      <button class="refresh-button" @click="refreshList" :disabled="loading">
        <el-icon class="refresh-icon" :class="{ 'rotating': loading }"><Refresh /></el-icon>
        刷新
      </button>
    </div>

    <!-- 通知列表 -->
    <div class="notice-list" v-if="noticeList.length > 0" @scroll="handleScroll" ref="scrollContainer">
      <div 
        class="notice-item" 
        v-for="notice in noticeList" 
        :key="notice.notificationId"
        @click="viewDetail(notice.notificationId)"
      >
        <div class="notice-header">
          <h3 class="notice-title">{{ notice.title }}</h3>
        </div>
        <div class="notice-meta">
          <div class="meta-item">
            <el-icon class="meta-icon"><User /></el-icon>
            <span>發送人:{{ notice.senderName }}</span>
          </div>
          <div class="meta-item">
            <el-icon class="meta-icon"><Clock /></el-icon>
            <span>發佈時間:{{ formatDate(notice.createTime) }}</span>
          </div>
        </div>
        <p class="notice-content">{{ truncateContent(notice.content, 100) }}</p>
        <div class="notice-footer">
          <span class="view-detail">查看詳情 →</span>
        </div>
      </div>
    
      <!-- 加载更多状态 -->
      <div class="load-more-status" v-if="noticeList.length < total">
        <div v-if="loadingMore" class="loading-more">
          <div class="loading-spinner-small"></div>
          <span>加載中...</span>
        </div>
        <div v-else class="load-more-hint" @click="loadMore">
          <span>點擊加載更多</span>
        </div>
      </div>
    
      <!-- 已加载全部 -->
      <div class="all-loaded" v-else>
        <span>已加載全部通知</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else-if="!loading && noticeList.length === 0">
      <div class="empty-icon">📭</div>
      <p class="empty-text">暫無通知</p>
    </div>

    <!-- 加载状态 : 当没有数据且正在加载时才显示 -->
    <div class="loading-state" v-if="loading && noticeList.length === 0">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { User, Clock, HomeFilled, Refresh } from '@element-plus/icons-vue'

export default {
  name: 'ParentNotice',
  components: {
    User,
    Clock,
    HomeFilled,
    Refresh
  },
  data() {
    return {
      noticeList: [],
      loading: false,
      loadingMore: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      hasMore: true
    }
  },
  mounted() {
    this.loadNoticeList()
  },
  activated() {
    // 當從其他組件(如詳情頁)返回時，保持當前狀態，不做任何操作
    // 避免重置列表導致滾動位置丟失
  },
  beforeUnmount() {
    // 清理事件監聽
    if (this.scrollContainer) {
      this.scrollContainer.removeEventListener('scroll', this.handleScroll)
    }
  },
  methods: {
    // 返回上一页
    goBack() {
      this.$router.push('/')
    },

    // 加载通知列表
    async loadNoticeList(reset = false) {
      // 重置分页
      if (reset) {
        this.currentPage = 1
        this.noticeList = []
        this.hasMore = true
      }

      // 只有當沒有數據時，才顯示全屏加載狀態以避免重新加載時閃爍和滾動位置丟失
      if (this.noticeList.length === 0) {
        this.loading = true
      }

      // 如果没有更多数据，直接返回
      if (!this.hasMore && !reset) {
        this.loading = false
        this.loadingMore = false
        return
      }

      try {
        this.loadingMore = true
        const response = await service.get(API_ENDPOINTS.NOTICE_LIST, {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        })
        if (response.data.code === 200) {
          const data = response.data.data
          const newList = data.list || []
          this.total = data.total || 0
          this.pageSize = data.pageSize || 10

          // 追加数据
          if (reset) {
            this.noticeList = newList
          } else {
            this.noticeList = [...this.noticeList, ...newList]
          }

          // 判断是否还有更多数据
          this.hasMore = this.noticeList.length < this.total
          this.currentPage++
        } else {
          ElMessage.error(response.data.msg || '獲取通知列表失敗')
        }
      } catch (error) {
        console.error('獲取通知列表失敗:', error)
        if (this.noticeList.length === 0) {
          ElMessage.error('網絡錯誤，請稍後重試')
        }
      } finally {
        this.loading = false
        this.loadingMore = false
      }
    },

    // 滚动事件处理
    handleScroll(event) {
      const container = event.target
      const scrollTop = container.scrollTop
      const scrollHeight = container.scrollHeight
      const clientHeight = container.clientHeight

      // 当滚动到距离底部 50px 时自动加载
      if (scrollTop + clientHeight >= scrollHeight - 50) {
        if (this.hasMore && !this.loadingMore) {
          this.loadMore()
        }
      }
    },

    // 加载更多
    loadMore() {
      if (this.hasMore && !this.loadingMore) {
        this.loadNoticeList()
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
    },

    // 刷新列表
    async refreshList() {
      if (this.loading) return
      
      try {
        await this.loadNoticeList(true)
        ElMessage.success('刷新成功')
      } catch (error) {
        console.error('刷新失败:', error)
        ElMessage.error('刷新失敗，請稍後重試')
      }
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
  justify-content: space-between;
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

.back-icon {
  width: 16px;
  height: 16px;
}

.refresh-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3073f1 0%, #60a5fa 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 6px rgba(48, 115, 241, 0.2);
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.refresh-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #1e5fd9 0%, #3b82f6 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(48, 115, 241, 0.3);
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  width: 16px;
  height: 16px;
}

.refresh-icon.rotating {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 通知列表 */
.notice-list {
  padding: 20px 25px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
  overflow-y: auto;
}

.notice-item {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  justify-content: flex-start;
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
  text-align: left;
  letter-spacing: 0.3px;
}

.notice-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #94a3b8;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.notice-content {
  font-size: 15px;
  color: #475569;
  line-height: 1.8;
  margin: 0 0 16px 0;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #bae6fd;
  text-align: left;
}

.notice-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #e0f2fe;
}

.view-detail {
  font-size: 14px;
  color: #0284c7;
  font-weight: 600;
  transition: all 0.3s ease;
}

.notice-item:hover .view-detail {
  color: #0369a1;
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

/* 加载更多状态 */
.load-more-status {
  padding: 20px;
  text-align: center;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #909399;
  font-size: 14px;
}

.loading-spinner-small {
  width: 20px;
  height: 20px;
  border: 2px solid #e4e7ed;
  border-top-color: #0284c7;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.load-more-hint {
  cursor: pointer;
  color: #0284c7;
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: #f0f9ff;
}

.load-more-hint:hover {
  background: #e0f2fe;
  transform: translateY(-2px);
}

/* 已加载全部 */
.all-loaded {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
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

  .refresh-button {
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
