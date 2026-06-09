<template>
  <div class="notice-detail-container">
    <!-- 頂部導航欄 -->
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

    <!-- 加載狀態 -->
    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>

    <!-- 錯誤狀態 -->
    <div class="error-state" v-else-if="errorMessage">
      <div class="error-icon">❌</div>
      <p class="error-text">{{ errorMessage }}</p>
      <button class="retry-button" @click="goBack">返回</button>
    </div>

    <!-- 通知詳情內容 -->
    <div class="detail-content" v-else-if="notice">
      <!-- 通知頭部信息 -->
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

        <!-- 附件/圖片 -->
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

      <!-- 問題列表（如果有） -->
      <div class="questions-section" v-if="questions && questions.length > 0">
        <div class="questions-header">
          <h3 class="section-title">
            <el-icon class="icon"><Document /></el-icon>
            問題表單
          </h3>
        </div>

        <!-- 直接顯示內容 -->
        <div class="questions-content-wrapper">
          <!-- 問題表單 -->
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

                <!-- 過期提示 - 僅在邏輯表單內顯示 -->
                <div class="expired-notice" v-if="isExpired && !hasSubmitted" style="margin: 20px 0;">
                  <div class="expired-icon">⏰</div>
                  <p class="expired-title">回覆時間已過</p>
                  <p class="expired-text">當前回覆時間已過，無法作答</p>
                </div>

                <!-- 答題區域 - 未過期時顯示 -->
                <template v-if="!isExpired">
                  <!-- 進度條（僅邏輯表單顯示） -->
                  <div class="progress-wrapper stepper-progress" v-if="question.questionType === '5'">
                    <div class="progress-info">
                      <span class="progress-text">作答進度</span>
                      <span class="progress-percent">{{ getQuestionProgress(question.questionId) }}%</span>
                    </div>
                    <div class="progress-bar-container">
                      <div class="progress-bar" :style="{ width: getQuestionProgress(question.questionId) + '%' }"></div>
                    </div>
                  </div>

                  <!-- 問題類型標籤 - 只在未提交時顯示 -->
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

                      <!-- 如果找不到佔位符格式，降級為單一輸入框 -->
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
                        <template v-if="isLastQuestion(question)">
                          完成作答 <el-icon><Check /></el-icon>
                        </template>
                        <template v-else>
                          下一題 <el-icon><ArrowRight /></el-icon>
                        </template>
                      </button>
                    </div>
                  </div>

                </template>

                <!-- 已提交後顯示所有節點和答案 -->
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
                        <!-- 填空題：顯示題目內容 -->
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

              <!-- 單選題 -->
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

              <!-- 多選題 -->
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

              <!-- 填空題 -->
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

              <!-- 附件上傳 -->
              <div class="question-upload" v-else-if="question.questionType === '4'">
                <button class="upload-button">📤 上傳附件</button>
              </div>
            </div> <!-- 結束 normal-question-wrapper -->
          </div>



          <!-- 已提交提示 - 已提交時顯示（無論是否過期） -->
          <div class="submitted-hint" v-if="hasSubmitted">
            <div class="hint-icon">✓</div>
            <p class="hint-text">您已完成作答</p>
          </div>
        </div> <!-- 結束 questions-content-wrapper -->
      </div>

    </div>

    <!-- 居中提示彈窗 -->
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
        <p class="dialog-message">您已完成所有題目的填寫，請確認是否提交回答。</p>
        <div class="dialog-action-buttons">
          <button class="dialog-cancel-btn" @click="closeCompleteDialog">返回修改</button>
          <button class="dialog-submit-btn" @click="submitAnswers" :disabled="submitting">
            <span v-if="!submitting">提交回答</span>
            <span v-else>正在提交...</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { User, Clock, ArrowLeft, ArrowRight, Document, Select, UploadFilled, Check, Download, Close, Warning, InfoFilled, Link } from '@element-plus/icons-vue'
import settings from '@/config/settings' // 導入全局配置設置
import CryptoJS from 'crypto-js' // 導入crypto-js庫用於MD5加密

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
      submitting: false, // 提交中狀態
      hasSubmitted: false, // 是否已提交
      userAnswer: null, // 用戶已提交的答案（單個對象）
      answererInfo: '', // 作答人信息，例如：「吳煜鍵 - 媽媽」
      isExpired: false, // 是否已過期
      logicFormDataCache: {}, // 緩存解析結果
      logicFormStates: {}, // 邏輯表單狀態緩存
      showCenterToast: false,
      showCompleteDialog: false, // 是否顯示自訂完成彈窗
      activeCompleteQuestion: null, // 當前完成的邏輯表單問題對象
      toastMessage: '',
      errorMessage: '', // 錯誤信息
      isFromWechatLink: false, // 是否從微信鏈接進入（帶有sid參數）
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
        // 如果不是JSON，可能是單個URL字符串
        if (typeof this.notice.attachmentUrls === 'string' && this.notice.attachmentUrls.trim()) {
          return [this.notice.attachmentUrls]
        }
        return []
      }
    },
    hasLogicQuestion() {
      return this.questions && this.questions.some(q => q.questionType === '5');
    },

  },
  async mounted() {
    // 組件掛載時立即滾動到頂部
    window.scrollTo(0, 0)
    // 加載時獲取學生列表，設置默認學生
    await this.loadStudentList()
    await this.loadNoticeDetail()
  },
  methods: {
    // MD5加密函數（使用crypto-js庫）
    md5Encrypt(text) {
      return CryptoJS.MD5(text).toString();
    },

    // 加載學生列表，設置默認學生
    async loadStudentList() {
      try {
        // 檢查是否啓用Token驗證
        if (settings.enableTokenAuth) {
          // 從前端存儲獲取token
          const token = localStorage.getItem('token') || sessionStorage.getItem('token');

          if (!token) {
            console.warn('未找到token，無法獲取學生列表');
            return;
          }
        }

        // 調用後端API獲取當前token關聯的學生列表
        const response = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS);

        if (response.data.code === 200) {
          const relations = response.data.data;

          if (relations && relations.length > 0) {
            // 檢查URL中是否有加密的學生ID（sid參數）
            const urlSid = this.$route.query.sid;

            if (urlSid) {
              // 標記爲從微信鏈接進入
              this.isFromWechatLink = true;

              // 遍歷學生列表，對每個學生ID進行MD5加密並匹配
              let matchedStudent = null;
              const encryptionSalt = settings.studentIdEncryptionSalt; // 從配置中獲取加密鹽值

              for (const relation of relations) {
                const studentUserId = relation.studentUserId;
                // 對學生ID進行MD5加密：studentUserId + salt
                const encryptedId = this.md5Encrypt(studentUserId + encryptionSalt);

                if (encryptedId === urlSid) {
                  matchedStudent = relation;
                  break;
                }
              }

              if (matchedStudent) {
                // 匹配成功，使用匹配到的學生ID和姓名
                localStorage.setItem('currentStudentUserId', matchedStudent.studentUserId);
                localStorage.setItem('currentStudentName', matchedStudent.studentName);
                this.currentStudentName = matchedStudent.studentName;
              } else {
                // 匹配失敗，設置錯誤信息並停止加載
                this.errorMessage = '無效的訪問鏈接，無法識別學生信息';
                this.loading = false;
              }
            } else {
              // 沒有sid參數，檢查localStorage中是否已有選中的學生
              const savedStudentUserId = localStorage.getItem('currentStudentUserId');
              if (savedStudentUserId) {
                // 驗證保存的學生ID是否在當前關係中
                const isValid = relations.some(r => r.studentUserId === savedStudentUserId);
                if (!isValid) {
                  // 如果緩存的學生ID無效，使用第一個學生
                  localStorage.setItem('currentStudentUserId', relations[0].studentUserId);
                  localStorage.setItem('currentStudentName', relations[0].studentName);
                  this.currentStudentName = relations[0].studentName;
                } else {
                  this.currentStudentName = localStorage.getItem('currentStudentName');
                }
              } else {
                // 沒有緩存，使用第一個學生作爲默認
                localStorage.setItem('currentStudentUserId', relations[0].studentUserId);
                localStorage.setItem('currentStudentName', relations[0].studentName);
                this.currentStudentName = relations[0].studentName;
              }
            }
          } else {
            console.warn('當前帳號未關聯任何學生');
          }
        } else {
          console.error('獲取學生列表失敗:', response.data.msg);
        }
      } catch (error) {
        console.error('獲取學生列表失敗:', error);
      }
    },

    // 返回上一頁或通知列表
    goBack() {
      // 如果從微信鏈接進入，返回通知列表
      if (this.isFromWechatLink) {
        this.$router.push('/notice')
      } else {
        // 否則返回瀏覽器歷史上一頁
        if (window.history.length > 1) {
          this.$router.back()
        } else {
          this.$router.push('/notice')
        }
      }
    },

    // 加載通知詳情
    async loadNoticeDetail() {
      const notificationId = this.$route.params.id
      if (!notificationId) {
        this.errorMessage = '通知ID不存在'
        this.loading = false
        return
      }

      this.loading = true
      try {
        // 從localStorage獲取當前選中的學生ID
        const studentUserId = localStorage.getItem('currentStudentUserId')

        // 如果沒有學生ID，顯示錯誤
        if (!studentUserId) {
          this.errorMessage = '無法獲取學生信息，請重新登錄'
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

          // 檢查是否已過期
          this.checkIfExpired()

          // 如果已提交，初始化答案顯示
          if (this.hasSubmitted && this.userAnswer) {
            this.initUserAnswer()
          }

          // 標記爲已讀（靜默調用，內部已捕獲異常，不影響主流程）
          await this.markAsRead(notificationId)

          // 數據加載完成後，確保滾動到頁面頂部
          setTimeout(() => {
            window.scrollTo({ top: 0, behavior: 'auto' })
          }, 100)
        } else {
          this.errorMessage = response.data.msg || '獲取通知詳情失敗'
          ElMessage.error(this.errorMessage)
        }
      } catch (error) {
        console.error('獲取通知詳情失敗:', error)
        this.errorMessage = '網絡錯誤，請稍後重試'
        ElMessage.error(this.errorMessage)
      } finally {
        this.loading = false
      }
    },

    // 標記通知爲已讀（靜默，不影響主流程）
    async markAsRead(notificationId) {
      try {
        // 從localStorage獲取當前選中的學生ID
        const studentUserId = localStorage.getItem('currentStudentUserId')

        const params = {}
        if (studentUserId) {
          params.studentUserId = studentUserId
        }

        await service.post(`${API_ENDPOINTS.NOTICE_MARK_READ}/${notificationId}/read`, null, {
          params: params
        })
      } catch (e) {
        // 靜默忽略，不影響用戶體驗
        console.warn('標記已讀失敗（已忽略）:', e)
      }
    },

    // 檢查是否已過期
    checkIfExpired() {
      if (!this.notice) {
        this.isExpired = false
        return
      }

      // 獲取截止時間（後端返回的是駝峯命名 replyDeadline）
      const deadlineStr = this.notice.replyDeadline

      if (!deadlineStr) {
        // 如果沒有截止時間，默認不過期
        this.isExpired = false
        return
      }

      try {
        const deadline = new Date(deadlineStr)
        const now = new Date()
        this.isExpired = now > deadline
      } catch (e) {
        console.error('解析截止時間失敗:', e)
        this.isExpired = false
      }
    },

    // 初始化用戶已提交的答案（單個對象）
    initUserAnswer() {
      if (!this.userAnswer) return

      // 遍歷所有問題，初始化邏輯表單狀態
      this.questions.forEach(question => {
        if (question.questionType === '5' && this.userAnswer.questionId === question.questionId) {
          this.initLogicFormAnswerFromObject(question, this.userAnswer)
        }
      })
    },

    // 從單個答案對象初始化邏輯表單
    initLogicFormAnswerFromObject(question, answer) {
      const logicData = this.getLogicFormData(question)
      if (!logicData) return

      const state = this.getLogicFormState(question)

      try {
        const answerData = JSON.parse(answer.answerData)
        if (Array.isArray(answerData)) {
          // answerData是數組，包含多個節點答案
          answerData.forEach(nodeAnswer => {
            const nodeId = String(nodeAnswer.nodeId)
            let answerValue = null

            // 解析answerContent
            try {
              answerValue = JSON.parse(nodeAnswer.answerContent)
              // 如果是填空題的新格式（包含blankId和value的對象數組），轉換爲簡單數組
              if (Array.isArray(answerValue) && answerValue.length > 0 &&
                  typeof answerValue[0] === 'object' && answerValue[0].hasOwnProperty('blankId')) {
                answerValue = answerValue.map(item => item.value)
              }
            } catch (e) {
              answerValue = nodeAnswer.answerContent
            }

            // 如果是附件類型，從attachmentUrls恢復文件信息
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
                console.error('解析附件URL失敗:', e)
              }
            }

            state.answers[nodeId] = answerValue
          })
        }
      } catch (e) {
        console.error('解析答案數據失敗:', e)
      }

      // 標記爲已完成
      state.isComplete = true

      // 設置activeNodeId爲第一個root節點
      if (logicData.roots && logicData.roots.length > 0) {
        state.activeNodeId = logicData.roots[0].node.id
      }
    },

    // 初始化邏輯表單的答案
    initLogicFormAnswers(question, answers) {
      const logicData = this.getLogicFormData(question)
      if (!logicData) return

      const state = this.getLogicFormState(question)

      // 將答案填充到state.answers中
      answers.forEach(answer => {
        try {
          const answerData = JSON.parse(answer.answerData)
          if (Array.isArray(answerData)) {
            // answerData是數組，包含多個節點答案
            answerData.forEach(nodeAnswer => {
              const nodeId = String(nodeAnswer.nodeId)
              let answerValue = null

              // 解析answerContent
              try {
                answerValue = JSON.parse(nodeAnswer.answerContent)
                // 如果是填空題的新格式（包含blankId和value的對象數組），轉換爲簡單數組
                if (Array.isArray(answerValue) && answerValue.length > 0 &&
                    typeof answerValue[0] === 'object' && answerValue[0].hasOwnProperty('blankId')) {
                  answerValue = answerValue.map(item => item.value)
                }
              } catch (e) {
                answerValue = nodeAnswer.answerContent
              }

              // 如果是附件類型，從attachmentUrls恢復文件信息
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
                  console.error('解析附件URL失敗:', e)
                }
              }

              state.answers[nodeId] = answerValue
            })
          }
        } catch (e) {
          console.error('解析答案數據失敗:', e)
        }
      })

      // 標記爲已完成
      state.isComplete = true

      // 設置activeNodeId爲第一個root節點
      if (logicData.roots && logicData.roots.length > 0) {
        state.activeNodeId = logicData.roots[0].node.id
      }
    },

    // 獲取審查模式下的答案列表（從單個對象解析）
    getReviewAnswers(question) {
      if (!this.userAnswer) return []

      try {
        const data = JSON.parse(this.userAnswer.answerData)
        return Array.isArray(data) ? data : []
      } catch (e) {
        console.error('解析審查答案失敗:', e)
        return []
      }
    },

    // 格式化答案內容顯示
    formatAnswerContent(content) {
      if (!content) return '未作答'
      try {
        // 如果content已經是對象或數組，直接使用
        let parsed = content
        // 如果是字符串，嘗試解析
        if (typeof content === 'string') {
          parsed = JSON.parse(content)
        }

        if (Array.isArray(parsed)) {
          // 檢查是否是新的填空題格式（包含blankId和value的對象數組）
          const firstNonNullItem = parsed.find(item => item !== null && item !== undefined)
          if (firstNonNullItem && typeof firstNonNullItem === 'object' && firstNonNullItem.hasOwnProperty('blankId')) {
            // 新格式填空題：按填空項序號顯示
            return parsed.map((item, index) => {
              if (item && typeof item === 'object' && item.hasOwnProperty('value')) {
                const value = item.value
                const displayValue = (value === '' || value === null || value === undefined) ? '(未填寫)' : value
                return `（${index + 1}）${displayValue}`
              }
              return `（${index + 1}）(未填寫)`
            }).join('\n')
          }
          // 原有的數組格式處理
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
        console.error('formatAnswerContent 解析失敗:', e)
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

    // 格式化日期時間
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

    // 解析選項
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
      // 將 {{fillblank-n}} 或 fillblank-n 替換為帶有下劃線的格式
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

        // 創建 FormData
        const formData = new FormData();
        formData.append('file', file);

        // 獲取當前選中的學生ID（從localStorage或sessionStorage）
        const studentUserId = localStorage.getItem('currentStudentUserId') || sessionStorage.getItem('currentStudentUserId');
        console.log('當前學生ID:', studentUserId);

        if (!studentUserId) {
          this.showToast('請先切換學生後再上傳文件', 'warning');
          event.target.value = '';
          return;
        }

        formData.append('studentUserId', studentUserId);

        // 調用上傳接口（不要手動設置 headers，讓 axios 攔截器自動處理）
        const response = await service.post(API_ENDPOINTS.FILE_UPLOAD, formData);

        if (response.data.code === 200) {
          // 上傳成功，保存文件URL
          const result = response.data.data;
          const fileUrl = result.url;
          const fileName = result.fileName || file.name; // 使用後端返回的文件名，如果沒有則使用原始文件名
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
        // 顯示後端返回的錯誤信息
        const errorMsg = error.response?.data?.msg || error.message || '文件上傳失敗，請重試';
        this.showToast(errorMsg, 'error');
      } finally {
        // 清空input，允許重複選擇同一文件
        event.target.value = '';
      }
    },


    // 判斷是否為最後一題
    isLastQuestion(question) {
      const state = this.getLogicFormState(question);
      const currentNode = this.getActiveNode(question);
      if (!currentNode) return false;
      const nodeData = currentNode.node;

      const logicData = this.getLogicFormData(question);
      if (!logicData) return false;
      const allNodes = logicData.allNodes;

      // 檢查當前選中的選項是否有跳轉到結束的邏輯
      const answerData = state.answers[nodeData.id];
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
        return true;
      }

      // 檢查物理順序後面是否還有可見節點
      const currentIdx = allNodes.findIndex(n => n.node.id === nodeData.id);
      let nextId = null;
      for (let i = currentIdx + 1; i < allNodes.length; i++) {
        const candidateId = allNodes[i].node.id;
        if (this.isNodeVisible(candidateId, question)) {
          nextId = candidateId;
          break;
        }
      }
      return !nextId;
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
        // 多選題：驗證選項數量限制
        const answerIndices = answerData || [];
        const minOptions = nodeData.minOptions || 0;
        const maxOptions = nodeData.maxOptions || null;

        // 驗證最小選項數
        if (minOptions > 0 && answerIndices.length < minOptions) {
          this.showToast(`「${nodeData.title}」至少需要選擇 ${minOptions} 個選項`);
          return;
        }

        // 驗證最大選項數
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
          this.showCompletePopup(question);
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
          this.showCompletePopup(question);
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

    // 獲取完整的附件URL
    getFullAttachmentUrl(attachment) {
      if (!attachment) return ''

      // 如果是對象，提取url屬性
      let url = typeof attachment === 'object' ? attachment.url : attachment

      if (!url || typeof url !== 'string') return ''

      // 如果已經是完整URL（以http或https開頭），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }

      // 清理URL中的雙斜槓
      url = url.replace(/\/+/g, '/')

      // 統一使用相對路徑，通過Nginx代理訪問後端
      // 開發環境（localhost）和生產環境（Nginx代理）都使用相同的路徑
      const origin = window.location.origin
      if (url.startsWith('/')) {
        return origin + url
      }
      return origin + '/' + url
    },

    // 獲取附件名稱
    getAttachmentName(attachment) {
      if (!attachment) return '未知文件'

      // 如果是對象，提取name屬性
      if (typeof attachment === 'object') {
        return attachment.name || this.getFileNameFromUrl(attachment.url)
      }

      // 如果是字符串，從URL提取文件名
      return this.getFileNameFromUrl(attachment)
    },

    // 從URL中提取文件名
    getFileNameFromUrl(url) {
      if (!url || typeof url !== 'string') return '未知文件'
      try {
        // 移除查詢參數
        const urlWithoutParams = url.split('?')[0]
        // 獲取最後一個/後面的部分
        const fileName = urlWithoutParams.split('/').pop()
        // 解碼URL編碼的字符
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

    // 判斷是否爲圖片
    isImage(attachment) {
      if (!attachment) return false

      // 如果是對象，提取url屬性
      let url = typeof attachment === 'object' ? attachment.url : attachment

      if (!url || typeof url !== 'string') return false
      const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']
      return imageExtensions.some(ext => url.toLowerCase().endsWith(ext))
    },

    // 提交回答
    async submitAnswers() {
      try {
        // 驗證必填題
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

        // 從localStorage獲取當前選中的學生ID
        const studentUserId = localStorage.getItem('currentStudentUserId');
        if (!studentUserId) {
          ElMessage.error('請指定學生ID');
          this.submitting = false;
          return;
        }

        // 調用後端API提交答案（只傳第一個問題的答案）
        const notificationId = this.$route.params.id;
        const response = await service.post(`${API_ENDPOINTS.NOTICE_DETAIL}/${notificationId}/submit`, {
          answer: answers[0],  // 只傳單個問題對象
          studentUserId: studentUserId  // 傳遞學生ID
        });

        if (response.data.code === 200) {
          ElMessage.success('提交成功！');
          // 重新加載詳情數據
          await this.loadNoticeDetail();
          // 滾動到頂部（使用平滑滾動，移動端兼容性更好）
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else if (response.data.code === 409) {
          // 409 表示重複提交，顯示提示後刷新頁面
          ElMessage.warning(response.data.msg || '您已回答過此問題');
          // 延遲 1 秒後刷新頁面，讓用戶看到提示
          setTimeout(async () => {
            await this.loadNoticeDetail();
            // 滾動到頂部（使用平滑滾動，移動端兼容性更好）
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }, 1000);
        } else {
          ElMessage.error(response.data.msg || '提交失敗，請重試');
        }
      } catch (error) {
        console.error('提交回答失敗:', error);
        ElMessage.error('網絡錯誤，請稍後重試');
      } finally {
        this.submitting = false;
        this.showCompleteDialog = false;
      }
    },

    // 驗證必填問題
    validateRequiredQuestions() {
      for (const question of this.questions) {
        // 跳過非必答問題
        if (question.isRequired !== '1') continue;

        // 檢查邏輯表單
        if (question.questionType === '5') {
          const state = this.logicFormStates[question.questionId];
          if (!state || !state.isComplete) {
            return {
              valid: false,
              message: `問題「${question.questionTitle}」尚未完成作答`
            };
          }

          // 驗證多選題的選項數量限制
          const validation = this.validateLogicFormOptions(question, state);
          if (!validation.valid) {
            return validation;
          }
        } else {
          // 檢查普通問題
          // TODO: 這裡需要根據實際的表單綁定來實現驗證
          // 目前先假設用戶已經填寫
        }
      }
      return { valid: true };
    },

    // 驗證邏輯表單的選項數量限制
    validateLogicFormOptions(question, state) {
      const logicData = this.getLogicFormData(question);
      if (!logicData) return { valid: true };

      // 遍歷所有節點，驗證多選題的 minOptions 和 maxOptions
      for (const nodeInfo of logicData.allNodes) {
        const node = nodeInfo.node;
        if (String(node.type) !== '2') continue; // 只驗證多選題

        const answerValue = state.answers[node.id];
        if (!answerValue || !Array.isArray(answerValue)) continue;

        const selectedCount = answerValue.length;
        const minOptions = node.minOptions || 0;
        const maxOptions = node.maxOptions || null;

        // 驗證最小選項數
        if (minOptions > 0 && selectedCount < minOptions) {
          return {
            valid: false,
            message: `「${node.title}」至少需要選擇 ${minOptions} 個選項`
          };
        }

        // 驗證最大選項數
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
      const answersMap = new Map(); // 使用Map按questionId分組
      const notificationId = this.$route.params.id;

      this.questions.forEach(question => {
        // 處理邏輯表單 (題型 5)
        if (question.questionType === '5') {
          const logicAnswers = this.collectLogicFormAnswers(question, notificationId);

          // 將同一問題的多個節點答案合併
          if (logicAnswers.length > 0) {
            // 如果該問題還沒有答案，初始化
            if (!answersMap.has(question.questionId)) {
              answersMap.set(question.questionId, {
                notificationId: notificationId,
                questionId: question.questionId,
                answerData: []
              });
            }
            // 將所有節點答案添加到answerData數組中
            const existingAnswer = answersMap.get(question.questionId);
            existingAnswer.answerData.push(...logicAnswers);
          }
        } else {
          // 處理普通問題 (題型 1-4)
          const normalAnswer = this.collectNormalQuestionAnswer(question, notificationId);
          if (normalAnswer) {
            answersMap.set(question.questionId, normalAnswer);
          }
        }
      });

      // 將Map轉換爲數組
      return Array.from(answersMap.values());
    },

    // 收集邏輯表單答案
    collectLogicFormAnswers(question, notificationId) {
      const nodeAnswers = [];
      const state = this.logicFormStates[question.questionId];
      if (!state || !state.answers) return nodeAnswers;

      const logicData = this.getLogicFormData(question);
      if (!logicData) return nodeAnswers;

      // 遍歷所有已回答的節點
      Object.keys(state.answers).forEach(nodeId => {
        let answerValue = state.answers[nodeId];
        if (answerValue === null || answerValue === undefined) return;

        // 查找對應的節點信息
        const nodeInfo = logicData.allNodes.find(n => n.node.id === Number(nodeId));
        if (!nodeInfo) return;

        const node = nodeInfo.node;
        let answerContent = '';
        let attachmentUrls = null;

        // 根據題目類型處理答案
        if (String(node.type) === '1' || String(node.type) === '2') {
          // 單選或多選：轉換爲實際選項文本
          if (Array.isArray(answerValue) && node.options) {
            const selectedOptions = answerValue.map(idx => node.options[idx]).filter(opt => opt !== undefined);
            answerContent = JSON.stringify(selectedOptions);
          } else {
            answerContent = JSON.stringify(answerValue);
          }
        } else if (String(node.type) === '3') {
          // 填空題：存儲填空答案數組，包含每個填空項的ID信息
          // 獲取fillBlanks配置
          const fillBlanks = node.fillBlanks || [];
          const blanksCount = fillBlanks.length > 0 ? fillBlanks.length : this.getFillBlanksCount(node.content);

          // 確保答案數組存在
          if (!Array.isArray(answerValue)) {
            answerValue = [];
          }

          // 將答案轉換爲包含blankId和value的對象數組
          const fillBlankAnswers = [];
          for (let i = 0; i < blanksCount; i++) {
            const blankId = fillBlanks[i] ? fillBlanks[i].id : `fillblank-${i + 1}`;
            // 使用答案數組中的值，如果不存在則爲空字符串
            const value = (answerValue[i] !== undefined && answerValue[i] !== null) ? String(answerValue[i]) : '';
            fillBlankAnswers.push({
              blankId: blankId,
              value: value
            });
          }
          answerContent = JSON.stringify(fillBlankAnswers);
        } else if (String(node.type) === '4') {
          // 附件上傳：存儲文件URL
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
          // 只返回節點級別的答案數據，不包含questionId和notificationId
          nodeAnswers.push({
            nodeId: nodeId,
            nodeTitle: node.title,
            nodeType: node.type,
            nodeContent: node.content || '',  // 添加題目內容
            answerContent: answerContent,
            attachmentUrls: attachmentUrls
          });
        }
      });

      return nodeAnswers;
    },

    // 收集普通問題答案
    collectNormalQuestionAnswer(question, notificationId) {
      // TODO: 這裡需要根據實際的表單數據綁定來實現
      // 目前返回null，實際需要獲取用戶輸入的值
      return null;
    },

    // 計算單個邏輯表單的進度
    getQuestionProgress(questionId) {
      const state = this.logicFormStates[questionId];
      if (!state) return 0;

      // 如果已經完成，直接返回100%
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
            // 文件上傳
            answeredCount++;
          } else if (String(answer).trim() !== '') {
            answeredCount++;
          }
        }
      });

      return Math.round((answeredCount / totalVisible) * 100);
    },

    // 顯示表單完成彈窗
    showCompletePopup(question) {
      this.showCompleteDialog = true;
      this.activeCompleteQuestion = question;
    },

    // 關閉表單完成彈窗
    closeCompleteDialog() {
      this.showCompleteDialog = false;
      if (this.activeCompleteQuestion) {
        const state = this.getLogicFormState(this.activeCompleteQuestion);
        if (state) {
          state.isComplete = false; // 重置完成狀態，使「完成作答」按鈕重新顯示出來，方便用戶修改
        }
      }
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

    // 圖片加載錯誤處理
    handleImageError(event) {
      event.target.style.display = 'none'
    },

    // 顯示居中提示
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

/* 頂部導航欄 */
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

/* 詳情內容 */
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

/* 確保內容文本中的鏈接不會溢出 */
.content-text :deep(a) {
  word-break: break-all;
  overflow-wrap: break-word;
}

/* 居中提示彈窗 */
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

/* 成功類型 */
.toast-content.toast-success {
  border-top: 4px solid #10b981;
}

.toast-content.toast-success .toast-icon {
  color: #10b981;
}

/* 錯誤類型 */
.toast-content.toast-error {
  border-top: 4px solid #ef4444;
}

.toast-content.toast-error .toast-icon {
  color: #ef4444;
}

/* 警告類型 */
.toast-content.toast-warning {
  border-top: 4px solid #f59e0b;
}

.toast-content.toast-warning .toast-icon {
  color: #f59e0b;
}

/* 信息類型 */
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

/* 問題部分重構 */
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

/* 進度條樣式 */
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

.dialog-action-buttons {
  display: flex;
  gap: 12px;
  width: 100%;
}

.dialog-cancel-btn {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 12px 0;
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dialog-cancel-btn:hover {
  background: #e2e8f0;
  color: #334155;
  transform: translateY(-1px);
}

.dialog-cancel-btn:active {
  transform: translateY(1px);
}

.dialog-submit-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #ffffff;
  border: none;
  border-radius: 10px;
  padding: 12px 0;
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}

.dialog-submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 12px -2px rgba(59, 130, 246, 0.4);
}

.dialog-submit-btn:active:not(:disabled) {
  transform: translateY(1px);
}

.dialog-submit-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
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

/* 選項樣式 */
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

/* 輸入框樣式 */
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

/* 填空題列表 */
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

/* 上傳按鈕 */
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

/* 提交按鈕 */
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

/* 過期提示 */
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

/* 已提交後查看答案 */
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

/* 題目內容顯示樣式 */
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
  white-space: pre-line; /* 支持換行符顯示 */
}



/* 錯誤狀態 */
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

/* 移動端適配 */
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
