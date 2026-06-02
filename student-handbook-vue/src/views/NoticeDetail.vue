<template>
  <div class="notice-detail-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <button class="back-button" @click="goBack">
        <el-icon class="back-icon"><ArrowLeft /></el-icon>
        返回
      </button>
      <div class="student-name-display" v-if="currentStudentName">
        <el-icon class="user-icon" style="flex-shrink: 0;"><User /></el-icon>
        <span class="student-name-text">{{ currentStudentName }}</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>

    <!-- 错误状态 -->
    <div class="error-state" v-else-if="errorMessage">
      <div class="error-icon">❌</div>
      <p class="error-text">{{ errorMessage }}</p>
      <button class="retry-button" @click="goBack">返回</button>
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
          <div class="meta-item deadline-item" v-if="notice.replyDeadline">
            <el-icon class="meta-icon"><Clock /></el-icon>
            <span>回覆截止時間：{{ formatDateTime(notice.replyDeadline) }}</span>
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
              <a v-else href="javascript:void(0)" class="attachment-link" @click.prevent="handleSecureDownload(attachment)">
                <span class="link-icon">📎</span>
                <span class="link-text">{{ getAttachmentName(attachment) }}</span>
              </a>
            </div>
          </div>
        </div>

        <!-- 外部跳轉連結 -->
        <div class="jump-link-wrapper" v-if="notice.jumpUrl">
          <a :href="notice.jumpUrl" target="_blank" class="jump-link-btn">
            <el-icon class="jump-link-icon"><Link /></el-icon>
            點擊前往外部連結
          </a>
          <div class="jump-link-url">{{ notice.jumpUrl }}</div>
        </div>
      </div>

      <!-- 问题列表（如果有） -->
      <div class="questions-section" v-if="questions && questions.length > 0">
        <div class="questions-header">
          <h3 class="section-title">
            <el-icon class="icon"><Document /></el-icon>
            問題表單
          </h3>
        </div>

        <!-- 直接顯示內容 -->
        <div class="questions-content-wrapper">
          <!-- 问题表单 -->
          <div
              class="question-item"
              v-for="question in questions"
              :key="question.questionId"
          >
            <!-- 若為邏輯表單 (題型 5) -->
            <div class="logic-form-wrapper logic-stepper-view" v-if="question.questionType === '5' && getLogicFormData(question)">
              <div class="logic-form-header">
                <h4 class="form-title">{{ getLogicFormData(question).title }}</h4>
                <p class="form-desc" v-if="getLogicFormData(question).description">{{ getLogicFormData(question).description }}</p>
              </div>

              <!-- 動態渲染當前啟動的題目 -->
              <div class="stepper-body" v-if="getLogicFormState(question) && getLogicFormState(question).activeNodeId">

                <!-- 过期提示 - 仅在逻辑表单内显示 -->
                <div class="expired-notice" v-if="isExpired && !hasSubmitted" style="margin: 20px 0;">
                  <div class="expired-icon">⏰</div>
                  <p class="expired-title">回覆時間已過</p>
                  <p class="expired-text">當前回覆時間已過，無法作答</p>
                </div>

                <!-- 答题区域 - 未过期时显示 -->
                <template v-if="!isExpired">
                  <!-- 进度条（仅逻辑表单显示） -->
                  <div class="progress-wrapper stepper-progress" v-if="question.questionType === '5'">
                    <div class="progress-info">
                      <span class="progress-text">作答進度</span>
                      <span class="progress-percent">{{ getQuestionProgress(question.questionId) }}%</span>
                    </div>
                    <div class="progress-bar-container">
                      <div class="progress-bar" :style="{ width: getQuestionProgress(question.questionId) + '%' }"></div>
                    </div>
                  </div>

                  <!-- 问题类型标签 - 只在未提交时显示 -->
                  <div class="question-type-label" v-if="!hasSubmitted">
                    <span v-if="String(getActiveNode(question).node.type) === '1'">單選題</span>
                    <span v-else-if="String(getActiveNode(question).node.type) === '2'">多選題</span>
                    <span v-else-if="String(getActiveNode(question).node.type) === '3'">填空題</span>
                    <span v-else-if="String(getActiveNode(question).node.type) === '4'">附件上傳</span>
                  </div>

                  <div class="active-node-container transition-wrapper fade-in" v-if="!hasSubmitted">
                    <div class="question-header logic-question-header">
                     <span class="question-number-wrapper">
                       <span class="question-number">{{ getActiveNode(question).displayNum }}</span>
                       <span class="required-mark" v-if="getActiveNode(question).node.required">*</span>
                     </span>
                      <span class="question-number-suffix">.</span>
                      <span class="question-title">{{ getActiveNode(question).node.title }}</span>
                    </div>

                    <!-- 單選/多選 -->
                    <div class="logic-options" v-if="['1', '2'].includes(String(getActiveNode(question).node.type))">
                      <div
                          class="logic-option-item"
                          :class="{ 'is-selected': isLogicOptionSelected(question.questionId, getActiveNode(question).node.id, optIdx) }"
                          v-for="(opt, optIdx) in getActiveNode(question).node.options"
                          :key="optIdx"
                          @click="handleLogicOptionClick(question, getActiveNode(question).node, optIdx)"
                      >
                       <span class="option-content">
                         <span class="opt-label">{{ String.fromCharCode(65 + optIdx) }}</span>
                         <span class="opt-text">{{ opt }}</span>
                       </span>
                        <el-icon class="check-icon" v-if="isLogicOptionSelected(question.questionId, getActiveNode(question).node.id, optIdx)"><Select /></el-icon>
                      </div>
                    </div>

                    <!-- 填空題 -->
                    <div class="logic-inputs" v-if="String(getActiveNode(question).node.type) === '3'">
                      <div class="logic-content-html" v-if="getActiveNode(question).node.content" v-html="formatFillBlankContent(getActiveNode(question).node.content)"></div>

                      <!-- 根據需填空的數量渲染對應的輸入框 -->
                      <div class="logic-fill-blanks" v-if="getFillBlanksCount(getActiveNode(question).node.content) > 0">
                        <div
                            class="fill-blank-item"
                            v-for="n in getFillBlanksCount(getActiveNode(question).node.content)"
                            :key="n"
                        >
                          <label class="blank-label">填寫空格 {{ n }}</label>
                          <input
                              type="text"
                              class="logic-input-text"
                              :value="getLogicFillBlankAnswer(question.questionId, getActiveNode(question).node.id, n - 1)"
                              @input="updateLogicFillBlankAnswer(question.questionId, getActiveNode(question).node.id, n - 1, $event.target.value)"
                              placeholder="請輸入對應的內容..."
                          />
                        </div>

                      </div>

                      <!-- 如果找不到占位符格式，降级為單一輸入框 -->
                      <textarea
                          v-else
                          class="logic-textarea"
                          v-model="getLogicFormState(question).answers[getActiveNode(question).node.id]"
                          placeholder="請輸入您的答案..."
                          rows="3"
                      ></textarea>
                    </div>

                    <!-- 附件上傳 -->
                    <div class="logic-upload" v-if="String(getActiveNode(question).node.type) === '4'">
                      <div class="logic-content-html" v-if="getActiveNode(question).node.content" v-html="getActiveNode(question).node.content"></div>

                      <div class="custom-upload-area" @click="triggerUpload(question.questionId, getActiveNode(question).node.id)">
                        <input
                            type="file"
                            :ref="'fileInput_' + getActiveNode(question).node.id"
                            style="display: none"
                            accept="image/jpeg,image/jpg,image/png,image/gif,image/bmp"
                            @change="handleFileUpload($event, question.questionId, getActiveNode(question).node.id)"
                        />

                        <!-- 未上傳時的佔位符 -->
                        <div class="upload-placeholder" v-if="!getLogicFormState(question).answers[getActiveNode(question).node.id]">
                          <div class="upload-icon-wrapper">
                            <el-icon class="upload-icon"><UploadFilled /></el-icon>
                          </div>
                          <span class="upload-hint">點擊上傳圖片</span>
                          <span class="upload-sub-hint">支援 JPG、PNG、GIF、BMP 格式，最大 5MB</span>
                        </div>

                        <!-- 已上傳預覽 -->
                        <div class="upload-file-preview" v-else>
                          <div class="file-info">
                            <el-icon class="file-icon"><Document /></el-icon>
                            <span class="file-name">{{ getLogicFormState(question).answers[getActiveNode(question).node.id].name }}</span>
                          </div>
                          <span class="file-success"><el-icon><Check /></el-icon> 已上傳</span>
                        </div>
                      </div>
                    </div>

                    <!-- 顯示下一題按鈕 -->
                    <div class="logic-action-bar">
                      <button class="back-step-btn" v-if="getLogicFormState(question).historyStack.length > 0 || getLogicFormState(question).isComplete" @click="handleLogicBack(question.questionId)">
                        <el-icon><ArrowLeft /></el-icon> 返回上一題
                      </button>
                      <button class="next-step-btn" v-if="!getLogicFormState(question).isComplete" @click="handleLogicNext(question)">
                        下一題 <el-icon><ArrowRight /></el-icon>
                      </button>
                    </div>
                  </div>

                </template>

                <!-- 已提交后显示所有节点和答案 -->
                <div class="submitted-answers" v-if="hasSubmitted">
                  <div class="answer-review-title">
                    我的作答
                    <span class="answerer-info" v-if="answererInfo">（{{ answererInfo }}作答）</span>
                  </div>
                  <div class="all-nodes-review">
                    <div
                        v-for="(nodeAnswer, index) in getReviewAnswers(question)"
                        :key="nodeAnswer.nodeId"
                        class="node-answer-item"
                    >
                      <div class="node-answer-header">
                        <span class="node-num">{{ index + 1 }}</span>
                        <span class="node-title">{{ nodeAnswer.nodeTitle }}</span>
                      </div>
                      <div class="node-answer-content">
                        <!-- 填空题：显示题目内容 -->
                        <div v-if="String(nodeAnswer.nodeType) === '3' && nodeAnswer.nodeContent" class="node-question-content" v-html="formatFillBlankContentForDisplay(nodeAnswer.nodeContent)">
                        </div>
                        <span class="answer-text">{{ formatAnswerContent(nodeAnswer.answerContent) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="normal-question-wrapper" v-else>
              <div class="question-header">
              <span class="question-number-wrapper">
                <span class="question-number">{{ question.sortOrder }}</span>
                <span class="required-mark" v-if="question.isRequired === '1'">*</span>
              </span>
                <span class="question-number-suffix">.</span>
                <span class="question-title">{{ question.questionTitle }}</span>
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

          <!-- 提交按钮 - 未过期且未提交时显示 -->
          <div class="submit-section" v-if="!hasSubmitted && !isExpired">
            <button class="submit-button" @click="submitAnswers" :disabled="submitting">
              <span v-if="!submitting">提交回答</span>
              <span v-else class="submitting-text">
              <span class="loading-spinner"></span>
              正在提交...
            </span>
            </button>
          </div>

          <!-- 已提交提示 - 已提交时显示（无论是否过期） -->
          <div class="submitted-hint" v-if="hasSubmitted">
            <div class="hint-icon">✓</div>
            <p class="hint-text">您已完成作答</p>
          </div>
        </div> <!-- 結束 questions-content-wrapper -->
      </div>

    </div>

    <!-- 居中提示弹窗 -->
    <div class="center-toast" v-if="showCenterToast">
      <div class="toast-content" :class="`toast-${toastType}`">
        <div class="toast-icon-wrapper">
          <el-icon v-if="toastType === 'success'" :size="36" color="#10b981"><Check /></el-icon>
          <el-icon v-else-if="toastType === 'error'" :size="36" color="#ef4444"><Close /></el-icon>
          <el-icon v-else-if="toastType === 'warning'" :size="36" color="#f59e0b"><Warning /></el-icon>
          <el-icon v-else :size="36" color="#3b82f6"><InfoFilled /></el-icon>
        </div>
        <p class="toast-message">{{ toastMessage }}</p>
      </div>
    </div>

    <!-- 自訂答題完成提示彈窗 -->
    <div class="custom-complete-dialog-mask fade-in" v-if="showCompleteDialog">
      <div class="custom-complete-dialog-content">
        <div class="dialog-success-icon-wrapper">
          <span class="dialog-success-icon">🎉</span>
        </div>
        <h3 class="dialog-title">問題表單已填寫完畢</h3>
        <p class="dialog-message">您已完成所有題目的填寫，請點擊下方的「提交回答」按鈕完成作答。</p>
        <button class="dialog-confirm-btn" @click="closeCompleteDialog">確定</button>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { User, Clock, ArrowLeft, ArrowRight, Document, Select, UploadFilled, Check, Download, Close, Warning, InfoFilled, Link } from '@element-plus/icons-vue'
import settings from '@/config/settings' // 导入全局配置设置
import CryptoJS from 'crypto-js' // 导入crypto-js库用于MD5加密

export default {
  name: 'NoticeDetail',
  components: {
    User,
    Clock,
    ArrowLeft,
    ArrowRight,
    Document,
    Select,
    UploadFilled,
    Check,
    Download,
    Close,
    Warning,
    InfoFilled,
    Link
  },
  data() {
    return {
      notice: null,
      questions: [],
      loading: false,
      submitting: false, // 提交中状态
      hasSubmitted: false, // 是否已提交
      userAnswer: null, // 用户已提交的答案（单个对象）
      answererInfo: '', // 作答人信息，例如：“吴煜键 - 妈妈”
      isExpired: false, // 是否已过期
      logicFormDataCache: {}, // 緩存解析結果
      logicFormStates: {}, // 邏輯表單狀態緩存
      showCenterToast: false,
      showCompleteDialog: false, // 是否顯示自訂完成彈窗
      toastMessage: '',
      errorMessage: '', // 错误信息
      isFromWechatLink: false, // 是否从微信链接进入（带有sid参数）
      currentStudentName: localStorage.getItem('currentStudentName') || '' // 學生姓名
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
    },

  },
  async mounted() {
    // 组件挂载时立即滚动到顶部
    window.scrollTo(0, 0)
    // 加载时获取学生列表，设置默认学生
    await this.loadStudentList()
    await this.loadNoticeDetail()
  },
  methods: {
    // MD5加密函数（使用crypto-js库）
    md5Encrypt(text) {
      return CryptoJS.MD5(text).toString();
    },

    // 加载学生列表，设置默认学生
    async loadStudentList() {
      try {
        // 检查是否启用Token验证
        if (settings.enableTokenAuth) {
          // 从前端存储获取token
          const token = localStorage.getItem('token') || sessionStorage.getItem('token');

          if (!token) {
            console.warn('未找到token，无法获取学生列表');
            return;
          }
        }

        // 调用后端API获取当前token关联的学生列表
        const response = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS);

        if (response.data.code === 200) {
          const relations = response.data.data;

          if (relations && relations.length > 0) {
            // 检查URL中是否有加密的学生ID（sid参数）
            const urlSid = this.$route.query.sid;

            if (urlSid) {
              // 标记为从微信链接进入
              this.isFromWechatLink = true;

              // 遍历学生列表，对每个学生ID进行MD5加密并匹配
              let matchedStudent = null;
              const encryptionSalt = settings.studentIdEncryptionSalt; // 从配置中获取加密盐值

              for (const relation of relations) {
                const studentUserId = relation.studentUserId;
                // 对学生ID进行MD5加密：studentUserId + salt
                const encryptedId = this.md5Encrypt(studentUserId + encryptionSalt);

                if (encryptedId === urlSid) {
                  matchedStudent = relation;
                  break;
                }
              }

              if (matchedStudent) {
                // 匹配成功，使用匹配到的学生ID和姓名
                localStorage.setItem('currentStudentUserId', matchedStudent.studentUserId);
                localStorage.setItem('currentStudentName', matchedStudent.studentName);
                this.currentStudentName = matchedStudent.studentName;
              } else {
                // 匹配失败，设置错误信息并停止加载
                this.errorMessage = '无效的访问链接，无法识别学生信息';
                this.loading = false;
              }
            } else {
              // 没有sid参数，检查localStorage中是否已有选中的学生
              const savedStudentUserId = localStorage.getItem('currentStudentUserId');
              if (savedStudentUserId) {
                // 验证保存的学生ID是否在当前关系中
                const isValid = relations.some(r => r.studentUserId === savedStudentUserId);
                if (!isValid) {
                  // 如果缓存的学生ID无效，使用第一个学生
                  localStorage.setItem('currentStudentUserId', relations[0].studentUserId);
                  localStorage.setItem('currentStudentName', relations[0].studentName);
                  this.currentStudentName = relations[0].studentName;
                } else {
                  this.currentStudentName = localStorage.getItem('currentStudentName');
                }
              } else {
                // 没有缓存，使用第一个学生作为默认
                localStorage.setItem('currentStudentUserId', relations[0].studentUserId);
                localStorage.setItem('currentStudentName', relations[0].studentName);
                this.currentStudentName = relations[0].studentName;
              }
            }
          } else {
            console.warn('當前帳號未關聯任何學生');
          }
        } else {
          console.error('获取学生列表失败:', response.data.msg);
        }
      } catch (error) {
        console.error('獲取學生列表失敗:', error);
      }
    },

    // 返回上一页或通知列表
    goBack() {
      // 如果从微信链接进入，返回通知列表
      if (this.isFromWechatLink) {
        this.$router.push('/notice')
      } else {
        // 否则返回浏览器历史上一页
        if (window.history.length > 1) {
          this.$router.back()
        } else {
          this.$router.push('/notice')
        }
      }
    },

    // 加载通知详情
    async loadNoticeDetail() {
      const notificationId = this.$route.params.id
      if (!notificationId) {
        this.errorMessage = '通知ID不存在'
        this.loading = false
        return
      }

      this.loading = true
      try {
        // 从localStorage获取当前选中的学生ID
        const studentUserId = localStorage.getItem('currentStudentUserId')

        // 如果没有学生ID，显示错误
        if (!studentUserId) {
          this.errorMessage = '无法获取学生信息，请重新登录'
          this.loading = false
          return
        }

        const params = {}
        if (studentUserId) {
          params.studentUserId = studentUserId
        }

        const response = await service.get(`${API_ENDPOINTS.NOTICE_DETAIL}/${notificationId}`, {
          params: params
        })
        if (response.data.code === 200) {
          this.notice = response.data.data.notification
          this.questions = response.data.data.questions || []
          this.userAnswer = response.data.data.userAnswer || null
          this.answererInfo = response.data.data.answererInfo || ''
          this.hasSubmitted = response.data.data.hasSubmitted || false

          // 检查是否已过期
          this.checkIfExpired()

          // 如果已提交，初始化答案显示
          if (this.hasSubmitted && this.userAnswer) {
            this.initUserAnswer()
          }

          // 标记为已读（静默调用，内部已捕获异常，不影响主流程）
          await this.markAsRead(notificationId)

          // 数据加载完成后，确保滚动到页面顶部
          setTimeout(() => {
            window.scrollTo({ top: 0, behavior: 'auto' })
          }, 100)
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

    // 标记通知为已读（静默，不影响主流程）
    async markAsRead(notificationId) {
      try {
        // 从localStorage获取当前选中的学生ID
        const studentUserId = localStorage.getItem('currentStudentUserId')

        const params = {}
        if (studentUserId) {
          params.studentUserId = studentUserId
        }

        await service.post(`${API_ENDPOINTS.NOTICE_MARK_READ}/${notificationId}/read`, null, {
          params: params
        })
      } catch (e) {
        // 静默忽略，不影响用户体验
        console.warn('标记已读失败（已忽略）:', e)
      }
    },

    // 检查是否已过期
    checkIfExpired() {
      if (!this.notice) {
        this.isExpired = false
        return
      }

      // 获取截止时间（后端返回的是驼峰命名 replyDeadline）
      const deadlineStr = this.notice.replyDeadline

      if (!deadlineStr) {
        // 如果没有截止时间，默认不过期
        this.isExpired = false
        return
      }

      try {
        const deadline = new Date(deadlineStr)
        const now = new Date()
        this.isExpired = now > deadline
      } catch (e) {
        console.error('解析截止时间失败:', e)
        this.isExpired = false
      }
    },

    // 初始化用户已提交的答案（单个对象）
    initUserAnswer() {
      if (!this.userAnswer) return

      // 遍历所有问题，初始化逻辑表单状态
      this.questions.forEach(question => {
        if (question.questionType === '5' && this.userAnswer.questionId === question.questionId) {
          this.initLogicFormAnswerFromObject(question, this.userAnswer)
        }
      })
    },

    // 从单个答案对象初始化逻辑表单
    initLogicFormAnswerFromObject(question, answer) {
      const logicData = this.getLogicFormData(question)
      if (!logicData) return

      const state = this.getLogicFormState(question)

      try {
        const answerData = JSON.parse(answer.answerData)
        if (Array.isArray(answerData)) {
          // answerData是数组，包含多个节点答案
          answerData.forEach(nodeAnswer => {
            const nodeId = String(nodeAnswer.nodeId)
            let answerValue = null

            // 解析answerContent
            try {
              answerValue = JSON.parse(nodeAnswer.answerContent)
              // 如果是填空题的新格式（包含blankId和value的对象数组），转换为简单数组
              if (Array.isArray(answerValue) && answerValue.length > 0 &&
                  typeof answerValue[0] === 'object' && answerValue[0].hasOwnProperty('blankId')) {
                answerValue = answerValue.map(item => item.value)
              }
            } catch (e) {
              answerValue = nodeAnswer.answerContent
            }

            // 如果是附件类型，从attachmentUrls恢复文件信息
            if (String(nodeAnswer.nodeType) === '4' && nodeAnswer.attachmentUrls) {
              try {
                const attachments = JSON.parse(nodeAnswer.attachmentUrls)
                if (Array.isArray(attachments) && attachments.length > 0) {
                  answerValue = {
                    name: attachments[0].name,
                    url: attachments[0].url,
                    size: attachments[0].size,
                    type: attachments[0].type
                  }
                }
              } catch (e) {
                console.error('解析附件URL失败:', e)
              }
            }

            state.answers[nodeId] = answerValue
          })
        }
      } catch (e) {
        console.error('解析答案数据失败:', e)
      }

      // 标记为已完成
      state.isComplete = true

      // 设置activeNodeId为第一个root节点
      if (logicData.roots && logicData.roots.length > 0) {
        state.activeNodeId = logicData.roots[0].node.id
      }
    },

    // 初始化逻辑表单的答案
    initLogicFormAnswers(question, answers) {
      const logicData = this.getLogicFormData(question)
      if (!logicData) return

      const state = this.getLogicFormState(question)

      // 将答案填充到state.answers中
      answers.forEach(answer => {
        try {
          const answerData = JSON.parse(answer.answerData)
          if (Array.isArray(answerData)) {
            // answerData是数组，包含多个节点答案
            answerData.forEach(nodeAnswer => {
              const nodeId = String(nodeAnswer.nodeId)
              let answerValue = null

              // 解析answerContent
              try {
                answerValue = JSON.parse(nodeAnswer.answerContent)
                // 如果是填空题的新格式（包含blankId和value的对象数组），转换为简单数组
                if (Array.isArray(answerValue) && answerValue.length > 0 &&
                    typeof answerValue[0] === 'object' && answerValue[0].hasOwnProperty('blankId')) {
                  answerValue = answerValue.map(item => item.value)
                }
              } catch (e) {
                answerValue = nodeAnswer.answerContent
              }

              // 如果是附件类型，从attachmentUrls恢复文件信息
              if (String(nodeAnswer.nodeType) === '4' && nodeAnswer.attachmentUrls) {
                try {
                  const attachments = JSON.parse(nodeAnswer.attachmentUrls)
                  if (Array.isArray(attachments) && attachments.length > 0) {
                    answerValue = {
                      name: attachments[0].name,
                      url: attachments[0].url,
                      size: attachments[0].size,
                      type: attachments[0].type
                    }
                  }
                } catch (e) {
                  console.error('解析附件URL失败:', e)
                }
              }

              state.answers[nodeId] = answerValue
            })
          }
        } catch (e) {
          console.error('解析答案数据失败:', e)
        }
      })

      // 标记为已完成
      state.isComplete = true

      // 设置activeNodeId为第一个root节点
      if (logicData.roots && logicData.roots.length > 0) {
        state.activeNodeId = logicData.roots[0].node.id
      }
    },

    // 获取审查模式下的答案列表（从单个对象解析）
    getReviewAnswers(question) {
      if (!this.userAnswer) return []

      try {
        const data = JSON.parse(this.userAnswer.answerData)
        return Array.isArray(data) ? data : []
      } catch (e) {
        console.error('解析审查答案失败:', e)
        return []
      }
    },

    // 格式化答案内容显示
    formatAnswerContent(content) {
      if (!content) return '未作答'
      try {
        // 如果content已经是对象或数组，直接使用
        let parsed = content
        // 如果是字符串，尝试解析
        if (typeof content === 'string') {
          parsed = JSON.parse(content)
        }

        if (Array.isArray(parsed)) {
          // 检查是否是新的填空题格式（包含blankId和value的对象数组）
          const firstNonNullItem = parsed.find(item => item !== null && item !== undefined)
          if (firstNonNullItem && typeof firstNonNullItem === 'object' && firstNonNullItem.hasOwnProperty('blankId')) {
            // 新格式填空题：按填空项序号显示
            return parsed.map((item, index) => {
              if (item && typeof item === 'object' && item.hasOwnProperty('value')) {
                const value = item.value
                const displayValue = (value === '' || value === null || value === undefined) ? '(未填写)' : value
                return `（${index + 1}）${displayValue}`
              }
              return `（${index + 1}）(未填写)`
            }).join('\n')
          }
          // 原有的数组格式处理
          return parsed.map(item => {
            if (item === null || item === undefined) return ''
            if (typeof item === 'object') {
              return JSON.stringify(item)
            }
            return String(item)
          }).filter(val => val !== '').join('、') || '未作答'
        }
        return parsed
      } catch (e) {
        console.error('formatAnswerContent 解析失败:', e)
        return content
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
          let allNodes = parsed.questions.map(q => ({ node: q, parents: [], displayNum: '', isRoot: true }));
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
                  nodeById[targetId].parents.push({ id: q.id, optIdx: rule.optionIndex });
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

    // 獲取邏輯表單狀態
    getLogicFormState(question) {
      const questionId = question.questionId;
      if (!this.logicFormStates[questionId]) {
        // 初始化狀態
        const logicData = this.getLogicFormData(question);
        let rootId = null;
        if (logicData && logicData.roots && logicData.roots.length > 0) {
          rootId = logicData.roots[0].node.id;
        }
        this.logicFormStates[questionId] = {
          activeNodeId: rootId,
          historyStack: [],
          answers: {},
          isComplete: false
        };
      }
      return this.logicFormStates[questionId];
    },

    // 格式化填空題內容
    formatFillBlankContent(content) {
      if (!content) return '';
      // 將 {{fillblank-n}} 替換為有編號的底線，方便用戶對應作答
      let index = 1;
      return content.replace(/\{\{fillblank-\d+\}\}/g, () => {
        return `<span style="border-bottom: 1px solid #333; display: inline-block; width: 40px; margin: 0 5px; text-align: center; font-size: 12px; color: #64748b;">(${index++})</span>`;
      });
    },

    // 格式化填空題內容用於顯示（已提交的答案）
    formatFillBlankContentForDisplay(content) {
      if (!content) return '';
      // 將 {{fillblank-n}} 或 fillblank-n 替換為帶有下划线的格式
      return content.replace(/\{?\{?fillblank-(\d+)\}?\}?/g, (match, num) => {
        return `<span style="display: inline-flex; flex-direction: column; align-items: center; margin: 0 4px;"><span>( ${num} )</span><span style="border-bottom: 2px solid #94a3b8; width: 50px; margin-top: 2px;"></span></span>`;
      });
    },

    // 獲取填空題空格數量
    getFillBlanksCount(content) {
      if (!content) return 0;
      const matches = content.match(/\{\{fillblank-\d+\}\}/g);
      return matches ? matches.length : 0;
    },

    // 獲取特定的填空題答案
    getLogicFillBlankAnswer(questionId, nodeId, index) {
      const state = this.logicFormStates[questionId];
      if (!state || !state.answers[nodeId]) return '';
      const ansArr = state.answers[nodeId];
      return (Array.isArray(ansArr) ? ansArr[index] : '') || '';
    },

    // 更新特定的填空題答案
    updateLogicFillBlankAnswer(questionId, nodeId, index, value) {
      const state = this.logicFormStates[questionId];
      if (!state) return;
      if (!Array.isArray(state.answers[nodeId])) {
        state.answers[nodeId] = [];
      }
      state.answers[nodeId][index] = value;
    },

    // DFS 可見性判斷
    isNodeVisible(nodeId, question) {
      const logicData = this.getLogicFormData(question);
      const state = this.getLogicFormState(question);
      return this.checkNodeVisibilityRecursively(nodeId, logicData.allNodes, state.answers);
    },

    checkNodeVisibilityRecursively(nodeId, allNodes, answers) {
      const nodeWrapper = allNodes.find(n => n.node.id === nodeId);
      if (!nodeWrapper) return false;
      if (nodeWrapper.isRoot) return true;

      for (let p of nodeWrapper.parents) {
        if (this.checkNodeVisibilityRecursively(p.id, allNodes, answers)) {
          const parentAns = answers[p.id];
          if (Array.isArray(parentAns) && parentAns.includes(p.optIdx)) {
            return true;
          }
        }
      }
      return false;
    },

    // 獲取當前活動題目
    getActiveNode(question) {
      const state = this.getLogicFormState(question);
      if (!state.activeNodeId) return null;
      const logicData = this.getLogicFormData(question);
      return logicData.allNodes.find(n => n.node.id === state.activeNodeId);
    },

    // 判斷選項是否選中
    isLogicOptionSelected(questionId, nodeId, optIdx) {
      const state = this.logicFormStates[questionId];
      if (!state) return false;
      const ans = state.answers[nodeId] || [];
      return ans.includes(optIdx);
    },

    // 處理選項點擊
    handleLogicOptionClick(question, node, optIdx) {
      const state = this.getLogicFormState(question);
      let ans = state.answers[node.id] || [];
      if (String(node.type) === '1') {
        // 單選
        ans = [optIdx];
        state.answers[node.id] = ans;
        // 單選可自動進入下一題，提供極致流暢體驗
        setTimeout(() => {
          this.handleLogicNext(question);
        }, 300);
      } else if (String(node.type) === '2') {
        // 多選
        const minOptions = node.minOptions || 0;
        const maxOptions = node.maxOptions || null;

        // 如果已選中，則取消選擇
        if (ans.includes(optIdx)) {
          // 檢查是否達到最小選擇數量
          if (minOptions > 0 && ans.length <= minOptions) {
            this.showToast(`至少需要選擇 ${minOptions} 個選項`);
            return;
          }
          ans = ans.filter(i => i !== optIdx);
        } else {
          // 如果未選中，則添加選擇
          // 檢查是否達到最大選擇數量
          if (maxOptions !== null && ans.length >= maxOptions) {
            this.showToast(`最多只能選擇 ${maxOptions} 個選項`);
            return;
          }
          ans.push(optIdx);
        }
        state.answers[node.id] = ans;
      }
    },

    // 觸發文件上傳框
    triggerUpload(questionId, nodeId) {
      const refName = 'fileInput_' + nodeId;
      const inputs = this.$refs[refName];
      if (inputs) {
        if (Array.isArray(inputs)) inputs[0].click();
        else inputs.click();
      }
    },

    // 處理附件選擇
    async handleFileUpload(event, questionId, nodeId) {
      const file = event.target.files[0];
      if (!file) return;

      // ── 前端驗證：格式 ──
      const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp'];
      const allowedMimeTypes  = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp'];
      const fileExt = file.name.split('.').pop().toLowerCase();
      const isHeic  = fileExt === 'heic' || fileExt === 'heif' ||
                      file.type === 'image/heic' || file.type === 'image/heif';

      if (isHeic) {
        this.showToast('不支援 HEIF/HEIC 格式。請在 iPhone「設定 → 相機 → 格式」中選擇「相容性最高」後重新拍照上傳', 'warning');
        event.target.value = '';
        return;
      }

      if (!allowedExtensions.includes(fileExt) || !allowedMimeTypes.includes(file.type)) {
        this.showToast(`不支援「.${fileExt}」格式，請上傳 JPG、PNG、GIF 或 BMP 圖片`, 'error');
        event.target.value = '';
        return;
      }

      // ── 前端驗證：大小（5MB）──
      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        const sizeMB = (file.size / 1024 / 1024).toFixed(1);
        this.showToast(`圖片大小 ${sizeMB}MB 超過限制，請上傳 5MB 以內的圖片`, 'error');
        event.target.value = '';
        return;
      }

      try {
        // 顯示上傳中狀態（持續顯示，不自動關閉）
        this.showToast('正在上傳文件...', 'info', 0);

        // 创建 FormData
        const formData = new FormData();
        formData.append('file', file);

        // 获取当前选中的学生ID（从localStorage或sessionStorage）
        const studentUserId = localStorage.getItem('currentStudentUserId') || sessionStorage.getItem('currentStudentUserId');
        console.log('当前学生ID:', studentUserId);

        if (!studentUserId) {
          this.showToast('請先切換學生後再上傳文件', 'warning');
          event.target.value = '';
          return;
        }

        formData.append('studentUserId', studentUserId);

        // 调用上传接口（不要手动设置 headers，让 axios 拦截器自动处理）
        const response = await service.post(API_ENDPOINTS.FILE_UPLOAD, formData);

        if (response.data.code === 200) {
          // 上传成功，保存文件URL
          const result = response.data.data;
          const fileUrl = result.url;
          const fileName = result.fileName || file.name; // 使用后端返回的文件名，如果没有则使用原始文件名
          const state = this.logicFormStates[questionId];
          if (state) {
            state.answers[nodeId] = {
              name: fileName,
              url: fileUrl,
              size: file.size,
              type: file.type
            };
          }
          this.showToast('文件上傳成功', 'success');
        } else {
          this.showToast(response.data.msg || '文件上傳失敗', 'error');
        }
      } catch (error) {
        console.error('文件上傳失敗:', error);
        // 显示后端返回的错误信息
        const errorMsg = error.response?.data?.msg || error.message || '文件上傳失敗，請重試';
        this.showToast(errorMsg, 'error');
      } finally {
        // 清空input，允许重复选择同一文件
        event.target.value = '';
      }
    },


    // 下一題
    handleLogicNext(question) {
      const state = this.getLogicFormState(question);
      const currentNode = this.getActiveNode(question);
      if (!currentNode) return;
      const nodeData = currentNode.node;

      const answerData = state.answers[nodeData.id];
      let hasAnswer = false;

      if (String(nodeData.type) === '3') {
        const blanksCount = this.getFillBlanksCount(nodeData.content);
        if (blanksCount > 0) {
          if (Array.isArray(answerData)) {
            let filledCount = 0;
            for (let i = 0; i < blanksCount; i++) {
              if (answerData[i] && String(answerData[i]).trim() !== '') {
                filledCount++;
              }
            }
            hasAnswer = filledCount === blanksCount;
          }
        } else {
          hasAnswer = !!(answerData && String(answerData).trim() !== '');
        }
      } else if (String(nodeData.type) === '4') {
        hasAnswer = !!answerData; // File 對象存在即代表已填答
      } else if (String(nodeData.type) === '2') {
        // 多选题：验证选项数量限制
        const answerIndices = answerData || [];
        const minOptions = nodeData.minOptions || 0;
        const maxOptions = nodeData.maxOptions || null;

        // 验证最小选项数
        if (minOptions > 0 && answerIndices.length < minOptions) {
          this.showToast(`「${nodeData.title}」至少需要選擇 ${minOptions} 個選項`);
          return;
        }

        // 验证最大选项数
        if (maxOptions !== null && answerIndices.length > maxOptions) {
          this.showToast(`「${nodeData.title}」最多只能選擇 ${maxOptions} 個選項`);
          return;
        }

        hasAnswer = answerIndices.length >= minOptions;
      } else {
        const answerIndices = answerData || [];
        hasAnswer = answerIndices.length > 0;
      }

      if (nodeData.required && !hasAnswer) {
        this.showToast('此題目是必答的！');
        return;
      }

      const logicData = this.getLogicFormData(question);
      const allNodes = logicData.allNodes;

      // 檢查是否明確要求中止跳轉 (JumpTarget = 'end')
      let jumpToEnd = false;
      if (nodeData.logicRuleList && nodeData.logicRuleList.length > 0 && Array.isArray(answerData) && !['3'].includes(String(nodeData.type))) {
        for (let rule of nodeData.logicRuleList) {
          if (answerData.includes(rule.optionIndex) && rule.jumpTarget === 'end') {
            jumpToEnd = true;
            break;
          }
        }
      }

      if (jumpToEnd) {
        if (!state.isComplete) {
          state.isComplete = true;
          this.showCompletePopup();
        } else {
          state.isComplete = true;
        }
        return;
      }

      // DFS 順序遍歷：找出物理陣列中「出現在此節點之後」，且「當前可見的」第一個節點
      let nextId = null;
      const currentIdx = allNodes.findIndex(n => n.node.id === nodeData.id);

      for (let i = currentIdx + 1; i < allNodes.length; i++) {
        const candidateId = allNodes[i].node.id;
        if (this.isNodeVisible(candidateId, question)) {
          nextId = candidateId;
          break;
        }
      }

      // 如果後面已經沒有可見節點了，就視為作答完畢
      if (!nextId) {
        if (!state.isComplete) {
          state.isComplete = true;
          this.showCompletePopup();
        } else {
          state.isComplete = true;
        }
        return;
      }

      state.isComplete = false;
      state.historyStack.push(nodeData.id);
      state.activeNodeId = nextId;
    },

    // 返回上一題
    handleLogicBack(questionId) {
      const state = this.logicFormStates[questionId];
      if (!state) return;
      state.isComplete = false; // 取消完成狀態，直接顯示上一題
      if (state.historyStack.length > 0) {
        const prevId = state.historyStack.pop();
        state.activeNodeId = prevId;
      }
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

      // 统一使用相对路径，通过Nginx代理访问后端
      // 开发环境（localhost）和生产环境（Nginx代理）都使用相同的路径
      const origin = window.location.origin
      if (url.startsWith('/')) {
        return origin + url
      }
      return origin + '/' + url
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

    // 處理安全下載附件（帶Token）
    async handleSecureDownload(attachment) {
      if (!attachment) return;

      const fileName = this.getAttachmentName(attachment);
      let url = typeof attachment === 'object' ? attachment.url : attachment;
      if (!url) return;

      // 清理URL中的雙斜線
      url = url.replace(/\/+/g, '/');

      // Android 設備特殊處理：跳過 Blob 下載，直接使用 URL 進行下載/預覽
      const isAndroid = /Android/i.test(navigator.userAgent);
      if (isAndroid) {
        const directUrl = window.location.origin + API_ENDPOINTS.FILE_UPLOAD.replace('/upload', '/download/resource') + '?resource=' + encodeURIComponent(url);
        this.showToast('開始下載', 'success');
        window.location.href = directUrl;
        return;
      }

      this.showToast('準備下載...', 'info');

      try {
        // 使用 axios 請求二進制流，這樣會自動帶上 Token
        const response = await service.get(API_ENDPOINTS.FILE_UPLOAD.replace('/upload', '/download/resource'), {
          params: { resource: url },
          responseType: 'blob', // 重要：指定為 blob
          timeout: 60000 // 文件下載可能較慢，延長超時時間
        });

        // 創建一個 Blob 對象
        const blob = new Blob([response.data]);

        // 如果後端有返回文件名（從 header 取 Content-Disposition），可以使用後端的文件名
        let downloadName = fileName;
        const disposition = response.headers['content-disposition'];
        if (disposition && disposition.indexOf('filename=') !== -1) {
          const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
          if (matches != null && matches[1]) {
            downloadName = decodeURIComponent(matches[1].replace(/['"]/g, ''));
          }
        }

        // 創建下載鏈接並觸發點擊
        const blobUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.style.display = 'none';
        link.href = blobUrl;
        link.download = downloadName;

        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(blobUrl);

        this.showToast('開始下載', 'success');
      } catch (error) {
        console.error('下載失敗:', error);
        this.showToast('下載失敗，請確保您有權限或稍後再試', 'error');
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
    async submitAnswers() {
      try {
        // 验证必填题
        const validation = this.validateRequiredQuestions();
        if (!validation.valid) {
          this.showToast(validation.message);
          return;
        }

        // 收集所有答案
        const answers = this.collectAllAnswers();

        if (answers.length === 0) {
          this.showToast('請至少回答一個問題');
          return;
        }

        this.submitting = true;

        // 从localStorage获取当前选中的学生ID
        const studentUserId = localStorage.getItem('currentStudentUserId');
        if (!studentUserId) {
          ElMessage.error('请指定学生ID');
          this.submitting = false;
          return;
        }

        // 调用后端API提交答案（只传第一个问题的答案）
        const notificationId = this.$route.params.id;
        const response = await service.post(`${API_ENDPOINTS.NOTICE_DETAIL}/${notificationId}/submit`, {
          answer: answers[0],  // 只传单个问题对象
          studentUserId: studentUserId  // 传递学生ID
        });

        if (response.data.code === 200) {
          ElMessage.success('提交成功！');
          // 重新加载详情数据
          await this.loadNoticeDetail();
          // 滚动到顶部（使用平滑滚动，移动端兼容性更好）
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else if (response.data.code === 409) {
          // 409 表示重复提交，显示提示后刷新页面
          ElMessage.warning(response.data.msg || '您已回答过此问题');
          // 延迟 1 秒后刷新页面，让用户看到提示
          setTimeout(async () => {
            await this.loadNoticeDetail();
            // 滚动到顶部（使用平滑滚动，移动端兼容性更好）
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }, 1000);
        } else {
          ElMessage.error(response.data.msg || '提交失敗，請重試');
        }
      } catch (error) {
        console.error('提交回答失败:', error);
        ElMessage.error('網絡錯誤，請稍後重試');
      } finally {
        this.submitting = false;
      }
    },

    // 验证必填问题
    validateRequiredQuestions() {
      for (const question of this.questions) {
        // 跳过非必答问题
        if (question.isRequired !== '1') continue;

        // 检查逻辑表单
        if (question.questionType === '5') {
          const state = this.logicFormStates[question.questionId];
          if (!state || !state.isComplete) {
            return {
              valid: false,
              message: `問題「${question.questionTitle}」尚未完成作答`
            };
          }

          // 验证多选题的选项数量限制
          const validation = this.validateLogicFormOptions(question, state);
          if (!validation.valid) {
            return validation;
          }
        } else {
          // 检查普通问题
          // TODO: 这里需要根据实际的表单绑定来实现验证
          // 目前先假设用户已经填写
        }
      }
      return { valid: true };
    },

    // 验证逻辑表单的选项数量限制
    validateLogicFormOptions(question, state) {
      const logicData = this.getLogicFormData(question);
      if (!logicData) return { valid: true };

      // 遍历所有节点，验证多选题的 minOptions 和 maxOptions
      for (const nodeInfo of logicData.allNodes) {
        const node = nodeInfo.node;
        if (String(node.type) !== '2') continue; // 只验证多选题

        const answerValue = state.answers[node.id];
        if (!answerValue || !Array.isArray(answerValue)) continue;

        const selectedCount = answerValue.length;
        const minOptions = node.minOptions || 0;
        const maxOptions = node.maxOptions || null;

        // 验证最小选项数
        if (minOptions > 0 && selectedCount < minOptions) {
          return {
            valid: false,
            message: `「${node.title}」至少需要選擇 ${minOptions} 個選項`
          };
        }

        // 验证最大选项数
        if (maxOptions !== null && selectedCount > maxOptions) {
          return {
            valid: false,
            message: `「${node.title}」最多只能選擇 ${maxOptions} 個選項`
          };
        }
      }

      return { valid: true };
    },

    // 收集所有答案
    collectAllAnswers() {
      const answersMap = new Map(); // 使用Map按questionId分组
      const notificationId = this.$route.params.id;

      this.questions.forEach(question => {
        // 处理逻辑表单 (题型 5)
        if (question.questionType === '5') {
          const logicAnswers = this.collectLogicFormAnswers(question, notificationId);

          // 将同一问题的多个节点答案合并
          if (logicAnswers.length > 0) {
            // 如果该问题还没有答案，初始化
            if (!answersMap.has(question.questionId)) {
              answersMap.set(question.questionId, {
                notificationId: notificationId,
                questionId: question.questionId,
                answerData: []
              });
            }
            // 将所有节点答案添加到answerData数组中
            const existingAnswer = answersMap.get(question.questionId);
            existingAnswer.answerData.push(...logicAnswers);
          }
        } else {
          // 处理普通问题 (题型 1-4)
          const normalAnswer = this.collectNormalQuestionAnswer(question, notificationId);
          if (normalAnswer) {
            answersMap.set(question.questionId, normalAnswer);
          }
        }
      });

      // 将Map转换为数组
      return Array.from(answersMap.values());
    },

    // 收集逻辑表单答案
    collectLogicFormAnswers(question, notificationId) {
      const nodeAnswers = [];
      const state = this.logicFormStates[question.questionId];
      if (!state || !state.answers) return nodeAnswers;

      const logicData = this.getLogicFormData(question);
      if (!logicData) return nodeAnswers;

      // 遍历所有已回答的节点
      Object.keys(state.answers).forEach(nodeId => {
        let answerValue = state.answers[nodeId];
        if (answerValue === null || answerValue === undefined) return;

        // 查找对应的节点信息
        const nodeInfo = logicData.allNodes.find(n => n.node.id === Number(nodeId));
        if (!nodeInfo) return;

        const node = nodeInfo.node;
        let answerContent = '';
        let attachmentUrls = null;

        // 根据题目类型处理答案
        if (String(node.type) === '1' || String(node.type) === '2') {
          // 单选或多选：转换为实际选项文本
          if (Array.isArray(answerValue) && node.options) {
            const selectedOptions = answerValue.map(idx => node.options[idx]).filter(opt => opt !== undefined);
            answerContent = JSON.stringify(selectedOptions);
          } else {
            answerContent = JSON.stringify(answerValue);
          }
        } else if (String(node.type) === '3') {
          // 填空题：存储填空答案数组，包含每个填空项的ID信息
          // 获取fillBlanks配置
          const fillBlanks = node.fillBlanks || [];
          const blanksCount = fillBlanks.length > 0 ? fillBlanks.length : this.getFillBlanksCount(node.content);

          // 确保答案数组存在
          if (!Array.isArray(answerValue)) {
            answerValue = [];
          }

          // 将答案转换为包含blankId和value的对象数组
          const fillBlankAnswers = [];
          for (let i = 0; i < blanksCount; i++) {
            const blankId = fillBlanks[i] ? fillBlanks[i].id : `fillblank-${i + 1}`;
            // 使用答案数组中的值，如果不存在则为空字符串
            const value = (answerValue[i] !== undefined && answerValue[i] !== null) ? String(answerValue[i]) : '';
            fillBlankAnswers.push({
              blankId: blankId,
              value: value
            });
          }
          answerContent = JSON.stringify(fillBlankAnswers);
        } else if (String(node.type) === '4') {
          // 附件上传：存储文件URL
          if (answerValue && typeof answerValue === 'object' && answerValue.url) {
            answerContent = answerValue.name;
            attachmentUrls = JSON.stringify([{
              name: answerValue.name,
              url: answerValue.url,
              size: answerValue.size,
              type: answerValue.type
            }]);
          }
        }

        if (answerContent) {
          // 只返回节点级别的答案数据，不包含questionId和notificationId
          nodeAnswers.push({
            nodeId: nodeId,
            nodeTitle: node.title,
            nodeType: node.type,
            nodeContent: node.content || '',  // 添加题目内容
            answerContent: answerContent,
            attachmentUrls: attachmentUrls
          });
        }
      });

      return nodeAnswers;
    },

    // 收集普通问题答案
    collectNormalQuestionAnswer(question, notificationId) {
      // TODO: 这里需要根据实际的表单数据绑定来实现
      // 目前返回null，实际需要获取用户输入的值
      return null;
    },

    // 计算单个逻辑表单的进度
    getQuestionProgress(questionId) {
      const state = this.logicFormStates[questionId];
      if (!state) return 0;

      // 如果已经完成，直接返回100%
      if (state.isComplete) return 100;

      const question = this.questions.find(q => q.questionId === questionId);
      if (!question) return 0;

      const logicData = this.logicFormDataCache[questionId];
      if (!logicData || !logicData.allNodes) return 0;

      // 篩選出目前所有可見的節點
      const visibleNodes = logicData.allNodes.filter(n => this.isNodeVisible(n.node.id, question));
      const totalVisible = visibleNodes.length;
      if (totalVisible === 0) return 0;

      // 統計已回答且當前可見的節點數
      let answeredCount = 0;
      visibleNodes.forEach(nodeInfo => {
        const nodeId = nodeInfo.node.id;
        const answer = state.answers[nodeId];
        if (answer !== null && answer !== undefined) {
          if (Array.isArray(answer)) {
            if (answer.length > 0) {
              answeredCount++;
            }
          } else if (typeof answer === 'object' && answer.name) {
            // 文件上传
            answeredCount++;
          } else if (String(answer).trim() !== '') {
            answeredCount++;
          }
        }
      });

      return Math.round((answeredCount / totalVisible) * 100);
    },

    // 顯示表單完成彈窗
    showCompletePopup() {
      this.showCompleteDialog = true;
    },

    // 關閉表單完成彈窗並平滑滾動到提交按鈕
    closeCompleteDialog() {
      this.showCompleteDialog = false;
      this.scrollToSubmitButton();
    },

    // 平滑滾動到提交按鈕
    scrollToSubmitButton() {
      this.$nextTick(() => {
        const btn = document.querySelector('.submit-button');
        if (btn) {
          btn.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      });
    },

    // 图片加载错误处理
    handleImageError(event) {
      event.target.style.display = 'none'
    },

    // 显示居中提示
    showToast(message, type = 'info', duration = 1500) {
      this.toastMessage = message;
      this.toastType = type;
      this.showCenterToast = true;
      // 清除上一個計時器，避免疊加
      if (this._toastTimer) {
        clearTimeout(this._toastTimer);
        this._toastTimer = null;
      }
      // duration = 0 表示持續顯示，不自動關閉
      if (duration > 0) {
        this._toastTimer = setTimeout(() => {
          this.showCenterToast = false;
        }, duration);
      }
    },
    hideToast() {
      if (this._toastTimer) {
        clearTimeout(this._toastTimer);
        this._toastTimer = null;
      }
      this.showCenterToast = false;
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
  justify-content: space-between;
  padding: 15px 15px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.student-name-display {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.85);
  color: #1e293b;
  border: 1.5px solid rgba(37, 99, 235, 0.35);
  font-weight: 600;
  font-size: 14px;
  user-select: none;
  max-width: 160px;
  min-width: 0;
  flex-shrink: 1;
  box-sizing: border-box;
}

.student-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.back-button {
  display: flex;
  align-items: center;
  flex-shrink: 0;
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

.deadline-item {
  color: #f56c6c;
  font-weight: 600;
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
  word-break: break-word;
  overflow-wrap: break-word;
}

/* 确保内容文本中的链接不会溢出 */
.content-text :deep(a) {
  word-break: break-all;
  overflow-wrap: break-word;
}

/* 居中提示弹窗 */
.center-toast {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  pointer-events: none;
}

.toast-content {
  background: white;
  color: #1f2937;
  padding: 24px 32px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  animation: toastFadeIn 0.3s ease;
  max-width: 320px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

/* 成功类型 */
.toast-content.toast-success {
  border-top: 4px solid #10b981;
}

.toast-content.toast-success .toast-icon {
  color: #10b981;
}

/* 错误类型 */
.toast-content.toast-error {
  border-top: 4px solid #ef4444;
}

.toast-content.toast-error .toast-icon {
  color: #ef4444;
}

/* 警告类型 */
.toast-content.toast-warning {
  border-top: 4px solid #f59e0b;
}

.toast-content.toast-warning .toast-icon {
  color: #f59e0b;
}

/* 信息类型 */
.toast-content.toast-info {
  border-top: 4px solid #3b82f6;
}

.toast-content.toast-info .toast-icon {
  color: #3b82f6;
}

.toast-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f3f4f6;
}

.toast-content.toast-success .toast-icon-wrapper {
  background: #d1fae5;
}

.toast-content.toast-error .toast-icon-wrapper {
  background: #fee2e2;
}

.toast-content.toast-warning .toast-icon-wrapper {
  background: #fef3c7;
}

.toast-content.toast-info .toast-icon-wrapper {
  background: #dbeafe;
}

.toast-message {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  text-align: center;
}

@keyframes toastFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
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

/* 外部跳轉連結 */
.jump-link-wrapper {
  margin-top: 25px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.jump-link-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.3s ease;
  width: 100%;
  max-width: 300px;
}

.jump-link-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

.jump-link-btn:active {
  transform: translateY(0);
}

.jump-link-icon {
  font-size: 18px;
}

.jump-link-url {
  font-size: 12px;
  color: #64748b;
  word-break: break-all;
  text-align: center;
  padding: 0 8px;
  line-height: 1.5;
}

/* 问题部分重构 */
.questions-section {
  background: white;
  border-radius: 12px;
  padding: 0;
  margin-bottom: 20px;
  overflow: hidden;
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-bottom: 2px solid #bae6fd;
}

.questions-header .section-title {
  margin: 0;
  padding: 0;
  border: none;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #0284c7;
}

.questions-header .icon {
  color: #0284c7;
  font-size: 22px;
}

/* 进度条样式 */
.progress-wrapper {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.progress-wrapper.stepper-progress {
  padding: 12px 0;
  margin-bottom: 16px;
  border-bottom: none;
  background: transparent;
}

.question-type-label {
  text-align: center;
  margin-bottom: 16px;
  padding: 6px 12px;
  background: #f0f9ff;
  border-radius: 6px;
  display: block;
}

.question-type-label span {
  font-size: 13px;
  color: #0284c7;
  font-weight: 600;
  letter-spacing: 1px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.progress-percent {
  font-size: 14px;
  font-weight: 700;
  color: #3b82f6;
}

.progress-bar-container {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%);
  border-radius: 4px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.questions-content-wrapper {
  padding: 20px;
}

.logic-stepper-view {
  background: #ffffff;
  border: none;
  padding: 0;
}

.active-node-container {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
}

.logic-question-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: baseline;
  gap: 6px;
  text-align: left;
}

.question-number-wrapper {
  display: inline-flex;
  align-items: flex-start;
  position: relative;
  flex-shrink: 0;
}

.question-number {
  display: inline;
  color: #3b82f6;
  font-size: 16px;
  font-weight: 700;
}

.question-number-suffix {
  color: #3b82f6;
  font-size: 16px;
  font-weight: 700;
  margin-right: 0;
}

.logic-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.logic-option-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.logic-option-item:hover {
  border-color: #93c5fd;
  background: #f0f9ff;
}

.logic-option-item:active {
  transform: scale(0.99);
}

.logic-option-item.is-selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.opt-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  line-height: 1;
  border-radius: 50%;
  background: #f1f5f9;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.25s;
}

.logic-option-item.is-selected .opt-label {
  background: #3b82f6;
  color: white;
}

.opt-text {
  font-size: 15px;
  color: #334155;
  font-weight: 500;
  white-space: nowrap;
}

.check-icon {
  margin-left: auto;
  color: #3b82f6;
  font-size: 20px;
  font-weight: bold;
  opacity: 0;
  transition: opacity 0.2s;
}

.logic-option-item.is-selected .check-icon {
  opacity: 1;
}

.logic-inputs, .logic-upload {
  margin-top: 10px;
}

.logic-content-html {
  font-size: 15px;
  color: #334155;
  line-height: 1.6;
  margin-bottom: 25px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.logic-fill-blanks {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.fill-blank-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.blank-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.logic-input-text {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  color: #334155;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.logic-input-text:focus {
  border-color: #3b82f6;
  background: #eff6ff;
}

.logic-textarea {
  width: 100%;
  padding: 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  color: #334155;
  resize: vertical;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.logic-textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.custom-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  background: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 10px;
}

.custom-upload-area:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon-wrapper {
  background: white;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  margin-bottom: 8px;
}

.upload-icon {
  font-size: 24px;
  color: #3b82f6;
}

.upload-hint {
  font-size: 15px;
  font-weight: 600;
  color: #475569;
}

.upload-sub-hint {
  font-size: 12px;
  color: #94a3b8;
}

.upload-file-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
  border: 1px solid #e2e8f0;
}

.file-icon {
  font-size: 20px;
  color: #64748b;
}

.file-name {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-success {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #10b981;
  font-size: 14px;
  font-weight: 600;
}

.logic-action-bar {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.back-step-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: transparent;
  color: #64748b;
  border: 1px solid #d1d5db;
  padding: 10px 10px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.25s;
}

.back-step-btn:hover {
  background: #f9fafb;
  border-color: #9ca3af;
  color: #374151;
}

.next-step-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  padding: 10px 10px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.25s;
  margin-left: auto;
}

.next-step-btn:active {
  transform: scale(0.98);
}

.logic-complete-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: #f0fdf4;
  border: 1px dashed #86efac;
  border-radius: 12px;
}

.complete-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.complete-text {
  font-size: 18px;
  font-weight: 600;
  color: #166534;
}

.complete-sub-text {
  font-size: 14px;
  color: #15803d;
  margin-top: 8px;
  font-weight: normal;
  text-align: center;
}

.fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 自訂答題完成提示彈窗樣式 */
.custom-complete-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.custom-complete-dialog-content {
  background: #ffffff;
  border-radius: 20px;
  width: 85%;
  max-width: 320px;
  padding: 30px 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes scaleUp {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.dialog-success-icon-wrapper {
  background: #f0fdf4;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border: 1px solid #dcfce7;
}

.dialog-success-icon {
  font-size: 32px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.dialog-message {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 24px 0;
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
  border: none;
  border-radius: 10px;
  padding: 12px 0;
  width: 100%;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}

.dialog-confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px -2px rgba(59, 130, 246, 0.4);
}

.dialog-confirm-btn:active {
  transform: translateY(1px);
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
  align-items: baseline;
  gap: 6px;
  margin-bottom: 12px;
  text-align: left;
}

.question-number-wrapper {
  display: inline-flex;
  align-items: flex-start;
  position: relative;
  flex-shrink: 0;
}

.question-number {
  display: inline;
  color: #3b82f6;
  font-size: 16px;
  font-weight: 700;
}

.question-number-suffix {
  color: #3b82f6;
  font-size: 16px;
  font-weight: 700;
  margin-right: 0;
}

.question-title {
  display: inline;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.5;
  flex: 1;
}

.required-mark {
  color: #f56c6c;
  font-size: 14px;
  font-weight: bold;
  position: absolute;
  top: -6px;
  right: -10px;
  line-height: 1;
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
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 10px;
  border-left: 4px solid #0284c7;
  text-align: center;
}

.logic-form-header .form-title {
  font-size: 17px;
  font-weight: 700;
  color: #0369a1;
  margin: 0 0 10px 0;
}

.logic-form-header .form-desc {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
  text-align: justify;
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
  margin-top: 15px;
  padding-top: 1px;
}

.submit-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
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

/* 已提交提示 */
.submitted-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 8px;
  color: white;
}

.hint-icon {
  font-size: 20px;
  font-weight: bold;
}

.hint-text {
  font-size: 14px;
  margin: 0;
}

/* 过期提示 */
.expired-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  border: 2px solid #f59e0b;
  margin: 20px 0;
}

.expired-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.expired-title {
  font-size: 18px;
  font-weight: bold;
  color: #92400e;
  margin: 0 0 8px 0;
}

.expired-text {
  font-size: 14px;
  color: #a16207;
  margin: 0;
  text-align: center;
}

/* 已提交后查看答案 */
.submitted-answers-wrapper {
  margin-top: 20px;
}

.answer-review-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid #3b82f6;
  line-height: 1.5;
  text-align: left;
}

.answerer-info {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  margin-left: 8px;
}

.all-nodes-review {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.node-answer-item {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.node-answer-item:active {
  background: #fafafa;
}

.node-answer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  text-align: left;
}

.node-num {
  background: #3b82f6;
  color: white;
  width: 22px;
  height: 22px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.node-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  line-height: 1.4;
  flex: 1;
  text-align: left;
}

.node-answer-content {
  margin-left: 32px;
  text-align: left;
}

/* 题目内容显示样式 */
.node-question-content {
  margin-bottom: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border-left: 3px solid #0ea5e9;
  border-radius: 4px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-line;
}

.answer-text {
  display: block;
  padding: 8px 10px;
  background: #f9fafb;
  border-radius: 6px;
  color: #374151;
  font-size: 13px;
  line-height: 1.5;
  word-wrap: break-word;
  word-break: break-word;
  text-align: left;
  white-space: pre-line; /* 支持换行符显示 */
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
    padding: 12px 10px;
  }

  .back-button {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
  }

  .student-name-display {
    padding: 8px 12px;
    font-size: 14px;
    gap: 5px;
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
