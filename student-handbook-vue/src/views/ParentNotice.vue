<template>
  <div class="notice-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon class="back-icon"><HomeFilled /></el-icon>
        返回首頁
      </button>
      <div class="header-buttons">
        <button class="refresh-button" @click="refreshList" :disabled="loading">
          <el-icon class="refresh-icon" :class="{ 'rotating': loading }"><Refresh /></el-icon>
          刷新
        </button>
        <button class="user-switch-btn" @click="toggleUserMenu">
          切換學生
        </button>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notice-list" v-if="noticeList.length > 0" @scroll="handleScroll" ref="scrollContainer">
      <div 
        class="notice-item" 
        v-for="notice in noticeList" 
        :key="notice.notificationId"
        @click="viewDetail(notice)"
      >
        <!-- 未读徽章 -->
        <span class="unread-badge" v-if="isUnread(notice)">未讀</span>

        <div class="notice-header">
          <h3 class="notice-title" :class="{ 'unread-title': isUnread(notice) }">{{ notice.title }}</h3>
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

    <!-- 共用切換學生彈窗元件 -->
    <StudentSwitchDialog v-model="studentDialogVisible" @switched="onStudentSwitched" />
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { User, Clock, HomeFilled, Refresh } from '@element-plus/icons-vue'
import settings from '@/config/settings' // 导入全局配置设置
import StudentSwitchDialog from '@/components/StudentSwitchDialog.vue'

export default {
  name: 'ParentNotice',
  components: {
    User,
    Clock,
    HomeFilled,
    Refresh,
    StudentSwitchDialog
  },
  data() {
    return {
      noticeList: [],
      loading: false,
      loadingMore: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      hasMore: true,
      savedScrollTop: 0, // 保存滚动位置
      isInitialMount: false, // 是否初次挂载
      
      // 學生選擇相關
      studentDialogVisible: false,
      selectedStudentUserId: localStorage.getItem('currentStudentUserId') || ''
    }
  },
  mounted() {
    this.isInitialMount = true
    this.selectedStudentUserId = localStorage.getItem('currentStudentUserId') || ''
    this.loadNoticeList()
  },
  activated() {
    if (this.isInitialMount) {
      this.isInitialMount = false
      return
    }
    
    const fromPath = this.$route.meta.fromPath || ''
    
    if (!fromPath.startsWith('/notice/')) {
      // 從首頁或其他地方進入：刷新列表並回到頂部
      this.selectedStudentUserId = localStorage.getItem('currentStudentUserId') || ''
      this.savedScrollTop = 0
      this.$nextTick(() => {
        if (this.$refs.scrollContainer) {
          this.$refs.scrollContainer.scrollTop = 0
        }
      })
      this.loadNoticeList(true)
    } else {
      // 當從詳情頁返回時，恢復滾動位置
      this.$nextTick(() => {
        if (this.$refs.scrollContainer) {
          // 使用 setTimeout 確保 DOM 完全渲染後再設置滾動位置
          setTimeout(() => {
            this.$refs.scrollContainer.scrollTop = this.savedScrollTop
          }, 50)
        }
      })
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
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        // 如果有选中的学生ID，添加到请求参数中
        if (this.selectedStudentUserId) {
          params.studentUserId = this.selectedStudentUserId
        }
        
        const response = await service.get(API_ENDPOINTS.NOTICE_LIST, {
          params: params
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

          // 检查是否需要自动加载更多（无滚动条或接近底部）
          this.$nextTick(() => {
            this.checkAndLoadMore()
          })
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

    // 检查并自动加载更多（处理无滚动条情况）
    checkAndLoadMore() {
      if (!this.hasMore || this.loadingMore) {
        return
      }

      const container = this.$refs.scrollContainer
      if (!container) {
        return
      }

      const scrollHeight = container.scrollHeight
      const clientHeight = container.clientHeight

      // 如果内容高度小于等于容器高度（无滚动条），自动加载更多
      if (scrollHeight <= clientHeight) {
        this.loadMore()
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

    // 判断通知是否未读（无发送记录 或 is_read='0'）
    isUnread(notice) {
      // 若 sendRecordId 为 null，说明该通知没有对应的发送记录（未发给当前用户），视为无状态，不显示未读
      if (!notice.sendRecordId) return false
      // isRead 为 '0' 或 null（有发送记录但无阅读记录）时视为未读
      return notice.isRead !== '1'
    },

    // 查看详情
    viewDetail(notice) {
      // 保存当前滚动位置
      if (this.$refs.scrollContainer) {
        this.savedScrollTop = this.$refs.scrollContainer.scrollTop
      }
      // 乐观更新本地状态：立即将该条记录标记为已读，避免返回列表时仍显示未读
      if (this.isUnread(notice)) {
        notice.isRead = '1'
      }
      this.$router.push(`/notice/${notice.notificationId}`)
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
    },

    // 切換用戶菜單顯示狀態
    toggleUserMenu() {
      this.studentDialogVisible = true;
    },

    // 學生切換成功的回調
    onStudentSwitched({ studentUserId }) {
      this.selectedStudentUserId = studentUserId;
      // 刷新通知列表
      this.loadNoticeList(true);
      
      // 通知父组件或全局更新未读数量
      window.dispatchEvent(new CustomEvent('studentChanged', { 
        detail: { studentUserId: this.selectedStudentUserId } 
      }));
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

.header-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #dbeafe 100%);
  color: #1e3a8a;
  border: none;
  box-shadow: 0 4px 6px rgba(147, 197, 253, 0.2);
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.back-button:hover {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
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

.user-switch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #dbeafe 100%);
  color: #1e3a8a;
  border: none;
  box-shadow: 0 4px 6px rgba(147, 197, 253, 0.2);
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.user-switch-btn:hover {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
}

.user-icon {
  width: 16px;
  height: 16px;
}

/* 通知列表 */
.notice-list {
  padding: 20px 25px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 80px);
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

/* 未读徽章 */
.unread-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: linear-gradient(135deg, #ef4444, #f87171);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.45);
  letter-spacing: 0.5px;
  z-index: 10;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { box-shadow: 0 2px 6px rgba(239, 68, 68, 0.45); }
  50%       { box-shadow: 0 2px 12px rgba(239, 68, 68, 0.75); }
}

/* 未读通知标题加粗高亮 */
.notice-title.unread-title {
  color: #0c4a6e;
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

  .user-switch-btn {
    padding: 10px 14px;
    font-size: 14px;
  }

  .header-buttons {
    gap: 8px;
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

/* 學生選擇項目的樣式 */
.student-options-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 5px 0;
  align-items: stretch;
}

.student-list {
  padding: 10px;
  border-radius: 8px;
  background: transparent;
  border: none; /* 移除邊框 */
  max-height: unset; /* 移除最大高度限制 */
  overflow-y: visible; /* 移除滾動條 */
}

.empty-student-list {
  text-align: center;
  padding: 20px;
}

.no-student-text {
  font-weight: bold;
  text-align: center;
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.student-item-radio {
  display: block;
  width: calc(100% - 20px);
  max-width: 100%;
  padding: 12px 16px;
  margin: 8px 0;
  border: 1px solid #d1e5f5; /* 添加淡藍色框線 */
  border-radius: 8px;
  background: transparent; /* 透明背景 */
  transition: all 0.3s ease;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-item-radio:hover {
  background: #dbeafe; /* 懸停時的淺藍色背景 */
  border: 1px solid #3b82f6 !important; /* 懸停時的藍色邊框 */
  color: #1e40af !important; /* 懸停時的深藍色文字 */
  transform: translateY(-1px);
}

/* 選中狀態的樣式 */
.student-item-radio.selected {
  background: #2563eb !important; /* 選中時的深藍色背景 */
  border: 2px solid #1d4ed8 !important; /* 選中時的深藍色邊框 */
  color: white !important; /* 選中時的白色文字 */
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4) !important;
  transform: translateY(-2px);
}

.student-item-radio.selected .student-name {
  color: white !important;
  font-weight: 700 !important;
  text-shadow: 0 0 2px rgba(255, 255, 255, 0.5) !important;
}

.student-name {
  font-size: 16px;
  color: #374151; /* 默認深灰色文字 */
  transition: all 0.3s ease;
  font-weight: 500;
}

.dialog-cancel-btn {
  background: linear-gradient(135deg, #60a5fa 0%, #93c5fd 100%) !important;
  border: none !important;
  color: #1e40af !important;
  font-weight: 600;
  padding: 12px 24px !important;
  font-size: 14px !important;
  min-width: 100px;
  margin: 0 8px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2) !important;
  border-radius: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.dialog-cancel-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  color: white !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.4) !important;
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
  padding: 12px 24px !important;
  font-size: 14px !important;
  min-width: 100px;
  margin: 0 8px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2) !important;
  border-radius: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.dialog-confirm-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.4) !important;
}

/* 自定义模态对话框样式 */
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.4) 0%, rgba(12, 74, 160, 0.6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(8px);
}

.custom-student-dialog {
  background: transparent;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  width: 30%;
  max-width: 90%;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  padding: 20px;
  border-radius: 16px 16px 0 0;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  color: white;
  font-weight: 700;
  font-size: 18px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.student-selection-content {
  padding: 25px;
  background: transparent;
  color: white;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
  padding: 0 25px 25px;
}

/* 手机端适配 */
@media (max-width: 768px) {
  .custom-student-dialog {
    width: 90%;
  }
}
</style>
