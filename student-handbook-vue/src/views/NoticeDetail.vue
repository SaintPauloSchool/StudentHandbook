<template>
  <div class="notice-detail-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span>
      </button>
      <h1 class="header-title">通知詳情</h1>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>

    <!-- 通知详情内容 -->
    <div class="detail-content" v-else-if="notice">
      <!-- 通知头部信息 -->
      <div class="notice-header-section">
        <h2 class="detail-title">{{ notice.title }}</h2>
        <div class="meta-info">
          <span class="sender">發送人：{{ notice.senderName }}</span>
          <span class="date">{{ formatDate(notice.createTime) }}</span>
        </div>
      </div>

      <!-- 通知正文 -->
      <div class="notice-body">
        <div class="content-text" v-html="notice.content"></div>
        
        <!-- 附件/图片 -->
        <div class="attachments" v-if="hasAttachments">
          <h4 class="attachments-title">附件</h4>
          <div class="attachment-list">
            <div 
              class="attachment-item" 
              v-for="(url, index) in attachmentList" 
              :key="index"
            >
              <img v-if="isImage(url)" :src="url" :alt="'附件' + (index + 1)" class="attachment-image" />
              <a v-else :href="url" target="_blank" class="attachment-link">
                <span class="link-icon">📎</span>
                <span class="link-text">查看附件 {{ index + 1 }}</span>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 问题列表（如果有） -->
      <div class="questions-section" v-if="questions && questions.length > 0">
        <h3 class="section-title">問卷調查</h3>
        <div 
          class="question-item" 
          v-for="question in questions" 
          :key="question.questionId"
        >
          <div class="question-header">
            <span class="question-number">{{ question.sortOrder }}.</span>
            <span class="question-title">{{ question.questionTitle }}</span>
            <span class="required-mark" v-if="question.isRequired === '1'">*</span>
          </div>
          
          <!-- 单选题 -->
          <div class="question-options" v-if="question.questionType === '1'">
            <div 
              class="option-item" 
              v-for="(option, idx) in parseOptions(question.options)" 
              :key="idx"
            >
              <input type="radio" :name="'q_' + question.questionId" :value="option" class="option-radio" />
              <label class="option-label">{{ option }}</label>
            </div>
          </div>

          <!-- 多选题 -->
          <div class="question-options" v-else-if="question.questionType === '2'">
            <div 
              class="option-item" 
              v-for="(option, idx) in parseOptions(question.options)" 
              :key="idx"
            >
              <input type="checkbox" :name="'q_' + question.questionId" :value="option" class="option-checkbox" />
              <label class="option-label">{{ option }}</label>
            </div>
          </div>

          <!-- 填空题 -->
          <div class="question-input" v-else-if="question.questionType === '3'">
            <input type="text" class="text-input" placeholder="請輸入您的答案" />
          </div>

          <!-- 附件上传 -->
          <div class="question-upload" v-else-if="question.questionType === '4'">
            <button class="upload-button">📤 上傳附件</button>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <button class="submit-button" @click="submitAnswers">提交回答</button>
        </div>
      </div>

      <!-- 回复截止时间提示 -->
      <div class="deadline-tip" v-if="notice.replyDeadline">
        <span class="tip-icon">⏰</span>
        <span class="tip-text">回覆截止時間：{{ formatDateTime(notice.replyDeadline) }}</span>
      </div>
    </div>

    <!-- 错误状态 -->
    <div class="error-state" v-else>
      <div class="error-icon">❌</div>
      <p class="error-text">加載失敗，請重試</p>
      <button class="retry-button" @click="loadNoticeDetail">重試</button>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'

export default {
  name: 'NoticeDetail',
  data() {
    return {
      notice: null,
      questions: [],
      loading: false
    }
  },
  computed: {
    hasAttachments() {
      return this.notice && this.notice.attachmentUrls && this.attachmentList.length > 0
    },
    attachmentList() {
      if (!this.notice || !this.notice.attachmentUrls) return []
      try {
        return JSON.parse(this.notice.attachmentUrls)
      } catch (e) {
        return []
      }
    }
  },
  mounted() {
    this.loadNoticeDetail()
  },
  methods: {
    // 返回上一页
    goBack() {
      this.$router.push('/notice')
    },

    // 加载通知详情
    async loadNoticeDetail() {
      const notificationId = this.$route.params.id
      if (!notificationId) {
        ElMessage.error('通知ID不存在')
        this.goBack()
        return
      }

      this.loading = true
      try {
        const response = await service.get(`${API_ENDPOINTS.NOTICE_DETAIL}/${notificationId}`)
        if (response.data.code === 200) {
          this.notice = response.data.data.notification
          this.questions = response.data.data.questions || []
        } else {
          ElMessage.error(response.data.msg || '獲取通知詳情失敗')
        }
      } catch (error) {
        console.error('獲取通知詳情失敗:', error)
        ElMessage.error('網絡錯誤，請稍後重試')
      } finally {
        this.loading = false
      }
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

    // 格式化日期时间
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },

    // 解析选项
    parseOptions(optionsStr) {
      if (!optionsStr) return []
      try {
        return JSON.parse(optionsStr)
      } catch (e) {
        return []
      }
    },

    // 判断是否为图片
    isImage(url) {
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']
      return imageExtensions.some(ext => url.toLowerCase().endsWith(ext))
    },

    // 提交回答
    submitAnswers() {
      ElMessage.info('回答提交功能開發中')
      // TODO: 实现回答提交逻辑
    }
  }
}
</script>

<style scoped>
.notice-detail-container {
  min-height: 100vh;
  background-color: #f5f9ff;
  padding: 0;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-button {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 10px;
  transition: transform 0.2s;
}

.back-button:hover {
  transform: translateX(-3px);
}

.header-title {
  flex: 1;
  font-size: 20px;
  font-weight: bold;
  margin: 0;
  text-align: center;
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

/* 详情内容 */
.detail-content {
  padding: 20px;
}

.notice-header-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.detail-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.meta-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

/* 通知正文 */
.notice-body {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.content-text {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 附件 */
.attachments {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.attachments-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 12px 0;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.attachment-item {
  border-radius: 6px;
  overflow: hidden;
}

.attachment-image {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 6px;
}

.attachment-link {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  text-decoration: none;
  color: #67c23a;
  transition: background 0.2s;
}

.attachment-link:hover {
  background: #e8f5e9;
}

.link-icon {
  font-size: 18px;
  margin-right: 8px;
}

.link-text {
  font-size: 14px;
}

/* 问题部分 */
.questions-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 17px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #67c23a;
}

.question-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.question-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.question-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.question-number {
  font-size: 15px;
  font-weight: bold;
  color: #67c23a;
  margin-right: 5px;
}

.question-title {
  flex: 1;
  font-size: 15px;
  color: #303133;
  line-height: 1.5;
}

.required-mark {
  color: #f56c6c;
  font-size: 16px;
  margin-left: 5px;
}

/* 选项样式 */
.question-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.option-item:hover {
  background: #e8f5e9;
}

.option-radio,
.option-checkbox {
  margin-right: 10px;
  cursor: pointer;
}

.option-label {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  flex: 1;
}

/* 输入框样式 */
.question-input {
  margin-top: 10px;
}

.text-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.text-input:focus {
  border-color: #67c23a;
}

/* 上传按钮 */
.question-upload {
  margin-top: 10px;
}

.upload-button {
  padding: 10px 20px;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.upload-button:hover {
  background: #5daf34;
}

/* 提交按钮 */
.submit-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 2px solid #ebeef5;
}

.submit-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.submit-button:active {
  transform: translateY(0);
}

/* 截止时间提示 */
.deadline-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #fff3e0;
  border-radius: 6px;
  margin-top: 15px;
}

.tip-icon {
  font-size: 18px;
  margin-right: 8px;
}

.tip-text {
  font-size: 13px;
  color: #e6a23c;
  font-weight: 500;
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 15px;
}

.error-text {
  font-size: 16px;
  color: #909399;
  margin-bottom: 20px;
}

.retry-button {
  padding: 10px 30px;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.retry-button:hover {
  background: #5daf34;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 12px 15px;
  }

  .header-title {
    font-size: 18px;
  }

  .detail-content {
    padding: 15px;
  }

  .detail-title {
    font-size: 18px;
  }

  .content-text {
    font-size: 14px;
  }
}
</style>
