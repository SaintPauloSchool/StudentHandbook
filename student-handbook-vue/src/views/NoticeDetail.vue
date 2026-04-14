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
        <div class="questions-header">
          <h3 class="section-title">
            <el-icon class="icon"><Document /></el-icon>
            問題表單
          </h3>
        </div>

        <!-- 直接顯示內容 -->
        <div class="questions-content-wrapper">
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

                <!-- 问题类型标签 -->
                <div class="question-type-label">
                  <span v-if="String(getActiveNode(question).node.type) === '1'">單選題</span>
                  <span v-else-if="String(getActiveNode(question).node.type) === '2'">多選題</span>
                  <span v-else-if="String(getActiveNode(question).node.type) === '3'">填空題</span>
                  <span v-else-if="String(getActiveNode(question).node.type) === '4'">附件上傳</span>
                </div>

                <div class="active-node-container transition-wrapper fade-in" v-if="!getLogicFormState(question).isComplete">
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
                            @change="handleFileUpload($event, question.questionId, getActiveNode(question).node.id)"
                         />
                         
                         <!-- 未上傳時的佔位符 -->
                         <div class="upload-placeholder" v-if="!getLogicFormState(question).answers[getActiveNode(question).node.id]">
                            <div class="upload-icon-wrapper">
                              <el-icon class="upload-icon"><UploadFilled /></el-icon>
                            </div>
                            <span class="upload-hint">點擊上傳圖片或文件</span>
                            <span class="upload-sub-hint">支援 JPG, PNG, PDF 格式</span>
                         </div>
                         
                         <!-- 已上傳預覽 -->
                         <div class="upload-file-preview" v-else>
                            <div class="file-info">
                               <el-icon class="file-icon"><Document /></el-icon>
                               <span class="file-name">{{ getLogicFormState(question).answers[getActiveNode(question).node.id].name }}</span>
                            </div>
                            <span class="file-success"><el-icon><Check /></el-icon> 已準備就緒</span>
                         </div>
                      </div>
                   </div>
                   
                   <!-- 顯示下一題按鈕 -->
                   <div class="logic-action-bar" v-if="!getLogicFormState(question).isComplete">
                     <button class="back-step-btn" v-if="getLogicFormState(question).historyStack.length > 0" @click="handleLogicBack(question.questionId)">
                       <el-icon><ArrowLeft /></el-icon> 返回上一題
                     </button>
                     <button class="next-step-btn" @click="handleLogicNext(question)">
                       下一題 <el-icon><ArrowRight /></el-icon>
                     </button>
                   </div>
                </div>

                <!-- 完成狀態提示 -->
                <div class="logic-complete-state fade-in" v-else>
                   <div class="complete-icon">🎉</div>
                   <p class="complete-text">問題表單作答完成</p>
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
          
        <!-- 提交按钮 -->
        <div class="submit-section">
          <button class="submit-button" @click="submitAnswers">提交回答</button>
        </div>
      </div> <!-- 結束 questions-content-wrapper -->
    </div>

    </div>

    <!-- 错误状态 -->
    <div class="error-state" v-else>
      <div class="error-icon">❌</div>
      <p class="error-text">加載失敗，請重試</p>
      <button class="retry-button" @click="loadNoticeDetail">重試</button>
    </div>

    <!-- 居中提示弹窗 -->
    <div class="center-toast" v-if="showCenterToast">
      <div class="toast-content">
        <div class="toast-icon">⚠️</div>
        <p class="toast-message">{{ toastMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { User, Clock, ArrowLeft, ArrowRight, Document, Select, UploadFilled, Check } from '@element-plus/icons-vue'

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
    Check
  },
  data() {
    return {
      notice: null,
      questions: [],
      loading: false,
      logicFormDataCache: {}, // 緩存解析結果
      logicFormStates: {}, // 邏輯表單狀態緩存
      showCenterToast: false,
      toastMessage: ''
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
  mounted() {
    // 组件挂载时立即滚动到顶部
    window.scrollTo(0, 0)
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
         if (ans.includes(optIdx)) {
           ans = ans.filter(i => i !== optIdx);
         } else {
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
    handleFileUpload(event, questionId, nodeId) {
      const file = event.target.files[0];
      if (file) {
        const state = this.logicFormStates[questionId];
        if (state) {
          state.answers[nodeId] = file;
        }
      }
      event.target.value = '';
    },

    // 下一題
    handleLogicNext(question) {
      const state = this.getLogicFormState(question);
      if (state.isComplete) return;
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
      if (nodeData.logicRuleList && nodeData.logicRuleList.length > 0 && Array.isArray(answerData) && !['3'].includes(String(nodeData.type))) {
        for (let rule of nodeData.logicRuleList) {
           if (answerData.includes(rule.optionIndex) && rule.jumpTarget === 'end') {
              state.isComplete = true;
              return;
           }
        }
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
         state.isComplete = true;
         return;
      }
      
      state.historyStack.push(nodeData.id);
      state.activeNodeId = nextId;
    },

    // 返回上一題
    handleLogicBack(questionId) {
      const state = this.logicFormStates[questionId];
      if (!state) return;
      if (state.isComplete) {
         state.isComplete = false; // 取消完成狀態，直接顯示上一題
      } else {
         if (state.historyStack.length > 0) {
            const prevId = state.historyStack.pop();
            state.activeNodeId = prevId;
         }
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

    // 计算单个逻辑表单的进度
    getQuestionProgress(questionId) {
      const state = this.logicFormStates[questionId];
      if (!state) return 0;
      
      // 如果已经完成，直接返回100%
      if (state.isComplete) return 100;
      
      const logicData = this.logicFormDataCache[questionId];
      if (!logicData || !logicData.allNodes) return 0;
      
      // 统计已回答的节点数
      let answeredCount = 0;
      Object.keys(state.answers).forEach(nodeId => {
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
      
      const totalNodes = logicData.allNodes.length;
      return totalNodes === 0 ? 0 : Math.round((answeredCount / totalNodes) * 100);
    },

    // 图片加载错误处理
    handleImageError(event) {
      event.target.style.display = 'none'
    },

    // 显示居中提示
    showToast(message) {
      this.toastMessage = message;
      this.showCenterToast = true;
      setTimeout(() => {
        this.showCenterToast = false;
      }, 1000);
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
.content-text >>> a,
.content-text >>> :deep(a) {
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
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 20px 30px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  animation: toastFadeIn 0.3s ease;
  max-width: 300px;
}

.toast-icon {
  font-size: 36px;
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

.fade-in {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
