 <template>
  <div class="notice-detail-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon class="back-icon"><ArrowLeft /></el-icon>
        返回
      </button>
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
          <div class="meta-item">
            <el-icon class="meta-icon"><User /></el-icon>
            <span>發送人：{{ notice.senderName }}</span>
          </div>
          <div class="meta-item">
            <el-icon class="meta-icon"><Clock /></el-icon>
            <span>發佈時間：{{ formatDate(notice.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 通知正文 -->
      <div class="notice-body">
        <div class="content-text" v-html="formattedContent"></div>
        
        <!-- 附件/图片 -->
        <div class="attachments" v-if="hasAttachments">
          <h4 class="attachments-title">附件</h4>
          <div class="attachment-list">
            <div 
              class="attachment-item" 
              v-for="(attachment, index) in attachmentList" 
              :key="index"
            >
              <img v-if="isImage(attachment)" :src="getFullAttachmentUrl(attachment)" :alt="getAttachmentName(attachment)" class="attachment-image" @error="handleImageError" />
              <a v-else :href="getFullAttachmentUrl(attachment)" target="_blank" class="attachment-link" @click="handleAttachmentClick(attachment)">
                <span class="link-icon">📎</span>
                <span class="link-text">{{ getAttachmentName(attachment) }}</span>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 问题列表（如果有） -->
      <div class="questions-section" v-if="questions && questions.length > 0">
        <h3 class="section-title">問卷</h3>
        <div 
          class="question-item" 
          v-for="question in questions" 
          :key="question.questionId"
        >
          <!-- 若為邏輯表單 (題型 5) -->
          <div class="logic-form-wrapper" v-if="question.questionType === '5' && getLogicFormData(question)">
             <div class="logic-form-header">
               <h4 class="form-title">{{ getLogicFormData(question).title }}</h4>
               <p class="form-desc" v-if="getLogicFormData(question).description">{{ getLogicFormData(question).description }}</p>
             </div>
             
             <div class="logic-nodes-list">
               <FormQuestionNode 
                 v-for="rootNode in getLogicFormData(question).roots"
                 :key="rootNode.node.id"
                 :question="rootNode.node"
                 :all-nodes="getLogicFormData(question).allNodes"
                 :level="0"
                 :display-num="rootNode.displayNum"
               />
             </div>
          </div>

          <!-- 一般題型 -->
          <div class="normal-question-wrapper" v-else>
            <div class="question-header">
            <span class="question-number">{{ question.sortOrder }}.</span>
            <span class="question-title">{{ question.questionTitle }}</span>
            <span class="required-mark" v-if="question.isRequired === '1'">*</span>
          </div>
          
          <!-- 渲染 content 裡的 JSON 或富文本 -->
          <div class="question-content" v-if="question.content">
            <div v-if="parseContentJson(question.content)" class="content-json-wrapper">
              <template v-for="(item, idx) in parseContentJson(question.content)" :key="idx">
                <span v-if="item.type === 'text' || item.type === 'string'" class="json-text">{{ item.value || item.text || item.content || item }}</span>
                <input v-else-if="item.type === 'input' || item.type === 'blank'" type="text" class="json-input" :placeholder="item.placeholder || '請輸入'" />
                <textarea v-else-if="item.type === 'textarea'" class="json-textarea" :placeholder="item.placeholder || '請輸入'"></textarea>
                <img v-else-if="item.type === 'image'" :src="item.url || item.value" class="json-image" />
                <span v-else class="json-text">{{ item.value || item.text || item }}</span>
              </template>
            </div>
            <div v-else class="content-html" v-html="question.content"></div>
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
            <div v-if="parseContentJson(question.fillBlanks)" class="fill-blanks-container">
              <div v-for="(blank, idx) in parseContentJson(question.fillBlanks)" :key="idx" class="blank-box">
                <span class="blank-label">{{ blank.label || ('填寫項 ' + (idx + 1)) }}:</span>
                <input type="text" class="text-input" :placeholder="blank.placeholder || '請輸入您的答案'" />
              </div>
            </div>
            <!-- 如果 content 已經渲染了空格（部分需求可能把空格放在 content 裡），這裡可依據情況保留預設輸入，但一般建議 fallback -->
            <input v-else-if="!parseContentJson(question.content)" type="text" class="text-input" placeholder="請輸入您的答案" />
          </div>

          <!-- 附件上传 -->
          <div class="question-upload" v-else-if="question.questionType === '4'">
            <button class="upload-button">📤 上傳附件</button>
          </div>
          </div> <!-- 結束 normal-question-wrapper -->
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
import { API_ENDPOINTS, baseURL } from '@/config/api.js'
import { User, Clock, ArrowLeft, Document } from '@element-plus/icons-vue'
import FormQuestionNode from '@/components/FormQuestionNode.vue'

export default {
  name: 'NoticeDetail',
  components: {
    User,
    Clock,
    ArrowLeft,
    Document,
    FormQuestionNode
  },
  data() {
    return {
      notice: null,
      questions: [],
      loading: false,
      logicFormDataCache: {} // 緩存解析結果
    }
  },
  computed: {
    formattedContent() {
      if (!this.notice || !this.notice.content) return ''
      // 若內容帶有 \n 或 \\n，將其轉為 HTML 的 <br> 以確保前端正常換行顯示
      // 防止後端傳來的是字面量 "\\n" 或是未被解析的換行符
      return String(this.notice.content)
        .replace(/\\n/g, '\n')
        .replace(/\n/g, '<br/>')
    },
    hasAttachments() {
      return this.notice && this.notice.attachmentUrls && this.attachmentList.length > 0
    },
    attachmentList() {
      if (!this.notice || !this.notice.attachmentUrls) {
        return []
      }
      try {
        const parsed = JSON.parse(this.notice.attachmentUrls)
        return parsed
      } catch (e) {
        // 如果不是JSON，可能是单个URL字符串
        if (typeof this.notice.attachmentUrls === 'string' && this.notice.attachmentUrls.trim()) {
          return [this.notice.attachmentUrls]
        }
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
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push('/notice')
      }
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

    // 解析 content 或 fillBlanks 等 JSON 字符串
    parseContentJson(str) {
      if (!str) return null
      try {
        const data = JSON.parse(str)
        return Array.isArray(data) ? data : [data]
      } catch (e) {
        return null
      }
    },

    // 取得並解析邏輯表單資料
    getLogicFormData(question) {
      if (question.questionType !== '5') return null;
      if (!question.content) return null;
      if (this.logicFormDataCache[question.questionId]) {
        return this.logicFormDataCache[question.questionId];
      }
      try {
        const parsed = JSON.parse(question.content);
        if (parsed && parsed.questions) {
          let allNodes = parsed.questions.map(q => ({ node: q, parentId: null, parentOptIdx: null, displayNum: '', isRoot: true }));
          let nodeById = {};
          allNodes.forEach(n => nodeById[n.node.id] = n);
          
          // 找出 parent 分支 (只要有被明確 jumpTarget 指向的就不是 root)
          parsed.questions.forEach(q => {
            (q.logicRuleList || []).forEach(rule => {
              const target = rule.jumpTarget;
              if (typeof target === 'number' || (typeof target === 'string' && target !== 'next' && target !== 'end')) {
                let targetId = Number(target);
                if (nodeById[targetId]) {
                  nodeById[targetId].isRoot = false;
                  nodeById[targetId].parentId = q.id;
                  nodeById[targetId].parentOptIdx = rule.optionIndex;
                }
              }
            });
          });

          // 分配階層編號
          let rootCounter = 1;
          const assignDisplayNum = (nodeInfo, currentNum) => {
            nodeInfo.displayNum = currentNum;
            let childCounter = 1;
            (nodeInfo.node.logicRuleList || []).forEach(rule => {
              const target = rule.jumpTarget;
              if (typeof target === 'number' || (typeof target === 'string' && target !== 'next' && target !== 'end')) {
                let targetId = Number(target);
                if (nodeById[targetId]) {
                  assignDisplayNum(nodeById[targetId], `${currentNum}.${childCounter++}`);
                }
              }
            });
          };

          allNodes.filter(n => n.isRoot).forEach(root => {
            assignDisplayNum(root, `${rootCounter++}`);
          });

          const result = {
            title: parsed.questionnaire ? parsed.questionnaire.title : '表單',
            description: parsed.questionnaire ? parsed.questionnaire.description : '',
            allNodes: allNodes,
            roots: allNodes.filter(n => n.isRoot)
          };
          this.logicFormDataCache[question.questionId] = result;
          return result;
        }
      } catch(e) { 
        console.error('Logic Form Parse Error', e);
      }
      return null;
    },

    // 获取完整的附件URL
    getFullAttachmentUrl(attachment) {
      if (!attachment) return ''
      
      // 如果是对象，提取url属性
      let url = typeof attachment === 'object' ? attachment.url : attachment
      
      if (!url || typeof url !== 'string') return ''
      
      // 如果已经是完整URL（以http或https开头），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      
      // 清理URL中的双斜杠
      url = url.replace(/\/+/g, '/')
      
      // 开发环境：需要拼接后端服务器地址和context-path
      // 生产环境：使用当前域名
      if (import.meta.env.MODE === 'development' || import.meta.env.MODE === 'test') {
        // 开发环境使用后端服务器地址 + context-path
        return 'http://localhost:8003/sp-api' + url
      } else {
        // 生产环境使用当前域名
        const origin = window.location.origin
        if (url.startsWith('/')) {
          return origin + url
        }
        return origin + '/' + url
      }
    },

    // 获取附件名称
    getAttachmentName(attachment) {
      if (!attachment) return '未知文件'
      
      // 如果是对象，提取name属性
      if (typeof attachment === 'object') {
        return attachment.name || this.getFileNameFromUrl(attachment.url)
      }
      
      // 如果是字符串，从URL提取文件名
      return this.getFileNameFromUrl(attachment)
    },

    // 从URL中提取文件名
    getFileNameFromUrl(url) {
      if (!url || typeof url !== 'string') return '未知文件'
      try {
        // 移除查询参数
        const urlWithoutParams = url.split('?')[0]
        // 获取最后一个/后面的部分
        const fileName = urlWithoutParams.split('/').pop()
        // 解码URL编码的字符
        return decodeURIComponent(fileName) || '未知文件'
      } catch (e) {
        return '未知文件'
      }
    },

    // 判断是否为图片
    isImage(attachment) {
      if (!attachment) return false
      
      // 如果是对象，提取url属性
      let url = typeof attachment === 'object' ? attachment.url : attachment
      
      if (!url || typeof url !== 'string') return false
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']
      return imageExtensions.some(ext => url.toLowerCase().endsWith(ext))
    },

    // 提交回答
    submitAnswers() {
      ElMessage.info('回答提交功能開發中')
      // TODO: 实现回答提交逻辑
    },

    // 图片加载错误处理
    handleImageError(event) {
      event.target.style.display = 'none'
    },

    // 附件点击处理
    handleAttachmentClick(attachment) {
      // 用于附件点击事件
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
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
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
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.back-button:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #fcd34d 100%);
  transform: translateY(-2px);
}

.back-icon {
  width: 16px;
  height: 16px;
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
  padding: 20px 25px;
  max-width: 1200px;
  margin: 0 auto;
}

.notice-header-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  position: relative;
  overflow: visible;
}

.notice-header-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #7dd3fc, #bae6fd, #bae6fd);
  z-index: 1;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: #0284c7;
  margin: 0 0 16px 0;
  line-height: 1.5;
  text-align: left;
  letter-spacing: 0.3px;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

/* 通知正文 */
.notice-body {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  position: relative;
  overflow: visible;
}

.notice-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #7dd3fc, #bae6fd, #bae6fd);
  z-index: 1;
}

.content-text {
  font-size: 15px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
  text-align: left;
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

/* 填空题列表 */
.fill-blanks-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.blank-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

.blank-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 60px;
}

/* JSON 內容渲染樣式 */
.question-content {
  margin-top: 10px;
  margin-bottom: 15px;
  padding: 10px;
  background: #fdfdfd;
  border-radius: 6px;
  border-left: 3px solid #7dd3fc;
}

.content-json-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  line-height: 2;
}

.json-text {
  font-size: 15px;
  color: #333;
}

.json-input {
  border: 1px solid #dcdfe6;
  border-bottom: 1px solid #909399; /* 讓填空看起來更像輸入線 */
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 14px;
  outline: none;
  min-width: 120px;
  transition: all 0.2s;
  background: transparent;
}

.json-input:focus {
  border-color: #67c23a;
  background: white;
}

.json-textarea {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 10px;
  font-size: 14px;
  outline: none;
  resize: vertical;
  min-height: 80px;
  margin-top: 8px;
  margin-bottom: 8px;
}

.json-textarea:focus {
  border-color: #67c23a;
}

.json-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 6px;
  margin: 5px 0;
  display: block;
}

.content-html {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
}

/* 邏輯表單特有樣式 - 優化協調性 */
.logic-form-header {
  margin-bottom: 25px;
  padding: 16px 20px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.logic-form-header .form-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 10px 0;
}

.logic-form-header .form-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

.logic-nodes-list {
  padding: 0;
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
}

.submit-button:hover {
  transform: translateY(-2px);
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
