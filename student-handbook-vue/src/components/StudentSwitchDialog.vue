<template>
  <div v-if="modelValue" class="ssd-overlay" @click="close">
    <div class="ssd-dialog" @click.stop>
      <!-- 標題欄 -->
      <div class="ssd-header">
        <h3 class="ssd-title">請選擇學生</h3>
        <button class="ssd-close-btn" @click="close">×</button>
      </div>

      <!-- 學生列表 -->
      <div class="ssd-body">
        <div
          v-for="rel in studentRelations"
          :key="rel.studentUserId"
          class="ssd-option"
          :class="{ 'ssd-option--active': selectedId === rel.studentUserId }"
          @click="selectedId = rel.studentUserId"
        >
          <span class="ssd-avatar">👤</span>
          <span class="ssd-name">{{ rel.studentName }}</span>
          <span v-if="selectedId === rel.studentUserId" class="ssd-check">✓</span>
        </div>
        <div v-if="studentRelations.length === 0 && !loading" class="ssd-empty">暫無學生數據</div>
        <div v-if="loading" class="ssd-empty">載入中...</div>
      </div>

      <!-- 底部按鈕 -->
      <div class="ssd-footer">
        <button class="ssd-btn ssd-btn--cancel" @click="close">取消</button>
        <button class="ssd-btn ssd-btn--confirm" :disabled="confirming" @click="confirm">
          {{ confirming ? '切換中...' : '確認' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'

export default {
  name: 'StudentSwitchDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'switched'],
  data() {
    return {
      loading: false,
      confirming: false,
      studentRelations: [],
      selectedId: ''
    }
  },
  watch: {
    // 每次打開時重新拉學生列表
    modelValue(val) {
      if (val) this.loadStudents()
    }
  },
  methods: {
    async loadStudents() {
      this.loading = true
      try {
        const res = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS)
        if (res.data.code === 200 && res.data.data?.length > 0) {
          this.studentRelations = res.data.data
          // 預選當前學生
          const savedId = localStorage.getItem('currentStudentUserId')
          this.selectedId = savedId && this.studentRelations.some(r => r.studentUserId === savedId)
            ? savedId
            : this.studentRelations[0].studentUserId
        } else {
          this.studentRelations = []
        }
      } catch (e) {
        ElMessage.error('獲取學生列表失敗')
      } finally {
        this.loading = false
      }
    },

    close() {
      this.$emit('update:modelValue', false)
    },

    async confirm() {
      const rel = this.studentRelations.find(r => r.studentUserId === this.selectedId)
      if (!rel) {
        ElMessage.warning('請選擇一個學生')
        return
      }
      this.confirming = true
      try {
        const res = await service.post(API_ENDPOINTS.SWITCH_STUDENT, {
          studentName: rel.studentName,
          studentUserId: rel.studentUserId
        })
        if (res.data.code === 200) {
          // 更新 localStorage
          localStorage.setItem('currentStudentUserId', rel.studentUserId)
          localStorage.setItem('currentStudentName', rel.studentName)
          ElMessage.success('已切換至 ' + rel.studentName)
          this.close()
          // 通知父元件
          this.$emit('switched', { studentUserId: rel.studentUserId, studentName: rel.studentName })
        } else {
          ElMessage.error(res.data.msg || '切換學生失敗')
        }
      } catch (e) {
        ElMessage.error('切換學生失敗')
      } finally {
        this.confirming = false
      }
    }
  }
}
</script>

<style scoped>
/* 遮罩 */
.ssd-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

/* 彈窗主體 */
.ssd-dialog {
  background: white;
  border-radius: 20px;
  width: 88%;
  max-width: 340px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: ssdScaleIn 0.28s cubic-bezier(0.23, 1, 0.32, 1);
}

@keyframes ssdScaleIn {
  from { transform: scale(0.85); opacity: 0; }
  to   { transform: scale(1);    opacity: 1; }
}

/* 標題欄 */
.ssd-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  padding: 18px 20px;
}

.ssd-title {
  margin: 0;
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.ssd-close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 20px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  line-height: 1;
}

.ssd-close-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* 學生列表區 */
.ssd-body {
  padding: 16px;
  max-height: 50vh;
  overflow-y: auto;
}

.ssd-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 8px;
  border: 2px solid transparent;
  background: #f8fafc;
}

.ssd-option:last-child { margin-bottom: 0; }

.ssd-option:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.ssd-option--active {
  background: #eff6ff;
  border-color: #3b82f6;
}

.ssd-avatar { font-size: 22px; }

.ssd-name {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.ssd-check {
  color: #2563eb;
  font-size: 18px;
  font-weight: 800;
}

.ssd-empty {
  text-align: center;
  color: #94a3b8;
  padding: 24px 0;
  font-size: 15px;
}

/* 底部按鈕 */
.ssd-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #f1f5f9;
}

.ssd-btn {
  flex: 1;
  padding: 13px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ssd-btn--cancel {
  background: #f1f5f9;
  color: #64748b;
}

.ssd-btn--cancel:active { background: #e2e8f0; }

.ssd-btn--confirm {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.ssd-btn--confirm:active,
.ssd-btn--confirm:disabled { opacity: 0.8; transform: scale(0.98); }
</style>
