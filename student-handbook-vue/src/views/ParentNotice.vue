<template>
  <div class="notice-container">
    <!-- 頂部導航欄 -->
    <div class="header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <el-icon class="back-icon"><HomeFilled /></el-icon>
          返回首頁
        </button>
      </div>
      <div class="header-center">
        <StudentChip
          chip-class="student-name-display"
          :name="currentStudentName || '未選擇學生'"
          :class-section="currentStudentClassSection"
          :profile-number="currentStudentProfileNumber"
        />
      </div>
      <div class="header-right">
        <button class="user-switch-btn" @click="toggleUserMenu">
          切換學生
        </button>
        <button class="refresh-button" @click="refreshList" :disabled="loading">
          <el-icon class="refresh-icon" :class="{ 'rotating': loading }"><Refresh /></el-icon>
          刷新
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
        <!-- 未讀徽章 -->
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
    
      <!-- 加載更多狀態 -->
      <div class="load-more-status" v-if="noticeList.length < total">
        <div v-if="loadingMore" class="loading-more">
          <div class="loading-spinner-small"></div>
          <span>加載中...</span>
        </div>
        <div v-else class="load-more-hint" @click="loadMore">
          <span>點擊加載更多</span>
        </div>
      </div>
    
      <!-- 已加載全部 -->
      <div class="all-loaded" v-else>
        <span>已加載全部通知</span>
      </div>
    </div>

    <!-- 空狀態 -->
    <div class="empty-state" v-else-if="!loading && noticeList.length === 0">
      <div class="empty-icon">📭</div>
      <p class="empty-text">暫無通知</p>
    </div>

    <!-- 加載狀態 : 當沒有數據且正在加載時才顯示 -->
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
import StudentSwitchDialog from '@/components/StudentSwitchDialog.vue'
import StudentChip from '@/components/StudentChip.vue'

export default {
  name: 'ParentNotice',
  components: {
    User,
    Clock,
    HomeFilled,
    Refresh,
    StudentSwitchDialog,
    StudentChip
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
      savedScrollTop: 0, // 保存滾動位置
      isInitialMount: false, // 是否初次掛載
      
      // 學生選擇相關
      studentDialogVisible: false,
      currentStudentName: localStorage.getItem('currentStudentName') || '',
      currentStudentClassSection: localStorage.getItem('currentStudentClassSection') || '',
      currentStudentProfileNumber: localStorage.getItem('currentStudentProfileNumber') || '',
      selectedStudentId: localStorage.getItem('currentStudentId') || ''
    }
  },
  mounted() {
    this.isInitialMount = true
    this.selectedStudentId = localStorage.getItem('currentStudentId') || ''
    this.currentStudentName = localStorage.getItem('currentStudentName') || ''
    this.currentStudentClassSection = localStorage.getItem('currentStudentClassSection') || ''
    this.currentStudentProfileNumber = localStorage.getItem('currentStudentProfileNumber') || ''
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
      this.selectedStudentId = localStorage.getItem('currentStudentId') || ''
      this.currentStudentName = localStorage.getItem('currentStudentName') || ''
      this.currentStudentClassSection = localStorage.getItem('currentStudentClassSection') || ''
      this.currentStudentProfileNumber = localStorage.getItem('currentStudentProfileNumber') || ''
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
    // 返回上一頁
    goBack() {
      this.$router.push('/')
    },

    // 加載通知列表
    async loadNoticeList(reset = false) {
      // 重置分頁
      if (reset) {
        this.currentPage = 1
        this.noticeList = []
        this.hasMore = true
      }

      // 只有當沒有數據時，才顯示全屏加載狀態以避免重新加載時閃爍和滾動位置丟失
      if (this.noticeList.length === 0) {
        this.loading = true
      }

      // 如果沒有更多數據，直接返回
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
        // 如果有選中的學生ID，添加到請求參數中
        if (this.selectedStudentId) {
          params.studentId = this.selectedStudentId
        }
        
        const response = await service.get(API_ENDPOINTS.NOTICE_LIST, {
          params: params
        })
        if (response.data.code === 200 || response.data.code === 0) {
          const data = response.data
          const newList = data.rows || []
          this.total = data.total || 0

          // 追加數據
          if (reset) {
            this.noticeList = newList
          } else {
            this.noticeList = [...this.noticeList, ...newList]
          }

          // 判斷是否還有更多數據
          this.hasMore = this.noticeList.length < this.total
          this.currentPage++

          // 檢查是否需要自動加載更多（無滾動條或接近底部）
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

    // 檢查並自動加載更多（處理無滾動條情況）
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

      // 如果內容高度小於等於容器高度（無滾動條），自動加載更多
      if (scrollHeight <= clientHeight) {
        this.loadMore()
      }
    },

    // 滾動事件處理
    handleScroll(event) {
      const container = event.target
      const scrollTop = container.scrollTop
      const scrollHeight = container.scrollHeight
      const clientHeight = container.clientHeight

      // 當滾動到距離底部 50px 時自動加載
      if (scrollTop + clientHeight >= scrollHeight - 50) {
        if (this.hasMore && !this.loadingMore) {
          this.loadMore()
        }
      }
    },

    // 加載更多
    loadMore() {
      if (this.hasMore && !this.loadingMore) {
        this.loadNoticeList()
      }
    },

    // 判斷通知是否未讀（無發送記錄 或 is_read='0'）
    isUnread(notice) {
      // 若 sendRecordId 爲 null，說明該通知沒有對應的發送記錄（未發給當前用戶），視爲無狀態，不顯示未讀
      if (!notice.sendRecordId) return false
      // isRead 爲 '0' 或 null（有發送記錄但無閱讀記錄）時視爲未讀
      return notice.isRead !== '1'
    },

    // 查看詳情
    viewDetail(notice) {
      // 保存當前滾動位置
      if (this.$refs.scrollContainer) {
        this.savedScrollTop = this.$refs.scrollContainer.scrollTop
      }
      // 樂觀更新本地狀態：立即將該條記錄標記爲已讀，避免返回列表時仍顯示未讀
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

    // 截斷內容
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
        console.error('刷新失敗:', error)
        ElMessage.error('刷新失敗，請稍後重試')
      }
    },

    // 切換用戶菜單顯示狀態
    toggleUserMenu() {
      this.studentDialogVisible = true;
    },

    // 學生切換成功的回調
    onStudentSwitched({ studentId, studentName, classSection, studentProfileNumber }) {
      this.selectedStudentId = studentId;
      this.currentStudentName = studentName || localStorage.getItem('currentStudentName') || '';
      this.currentStudentClassSection = classSection || localStorage.getItem('currentStudentClassSection') || '';
      this.currentStudentProfileNumber = studentProfileNumber || localStorage.getItem('currentStudentProfileNumber') || '';
      // 刷新通知列表
      this.loadNoticeList(true);
      
      // 通知父組件或全局更新未讀數量
      window.dispatchEvent(new CustomEvent('studentChanged', { 
        detail: { studentId: this.selectedStudentId } 
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

/* 頂部導航欄 */
.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: start;
  padding: 15px 20px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  justify-self: start;
  display: flex;
  align-items: center;
}

.header-center {
  justify-self: center;
  display: flex;
  justify-content: center;
}

.header-right {
  justify-self: end;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.student-name-display {
  max-width: 200px;
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

@media (hover: hover) {
  .back-button:hover {
    background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
  }
}

.back-button:active {
  transform: scale(0.96);
  opacity: 0.9;
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

@media (hover: hover) {
  .refresh-button:hover:not(:disabled) {
    background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
  }
}

.refresh-button:active:not(:disabled) {
  transform: scale(0.96);
  opacity: 0.9;
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
  white-space: nowrap;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
}

@media (hover: hover) {
  .user-switch-btn:hover {
    background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
  }
}

.user-switch-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
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

/* 未讀徽章 */
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

/* 未讀通知標題加粗高亮 */
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

/* 空狀態 */
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

/* 加載狀態 */
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

/* 加載更多狀態 */
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

/* 已加載全部 */
.all-loaded {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

/* 移動端適配 */
@media (max-width: 768px) {
  .header {
    padding: 12px 15px;
  }

  .back-button {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
  }

  .refresh-button {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
  }

  .user-switch-btn {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
  }

  .student-name-display {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
  }

  .header-left {
    align-items: flex-start;
  }

  .header-center {
    padding: 0 8px;
  }
  
  .header-right {
    gap: 6px;
    align-items: flex-end;
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
