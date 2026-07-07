<template>
  <div v-if="modelValue" class="ssd-overlay" @click="close">
    <div class="ssd-dialog" @click.stop>
      <div class="ssd-header">
        <h3 class="ssd-title">請選擇學生</h3>
        <button class="ssd-close-btn" @click="close" aria-label="關閉">×</button>
      </div>

      <div class="ssd-body">
        <div
          v-for="rel in studentRelations"
          :key="rel.studentId"
          class="ssd-option"
          :class="{ 'ssd-option--active': selectedId === rel.studentId }"
          @click="selectedId = rel.studentId"
        >
          <div class="ssd-avatar">
            <StudentPhoto :profile-number="rel.studentProfileNumber" :size="48" round />
          </div>
          <div class="ssd-info">
            <div v-if="rel.classSection" class="ssd-class">{{ rel.classSection }}</div>
            <div class="ssd-name">{{ rel.studentName }}</div>
          </div>
          <div class="ssd-check-slot">
            <span v-if="selectedId === rel.studentId" class="ssd-check">✓</span>
          </div>
        </div>
        <div v-if="studentRelations.length === 0 && !loading" class="ssd-empty">暫無學生數據</div>
        <div v-if="loading" class="ssd-empty">載入中...</div>
      </div>

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
import StudentPhoto from '@/components/StudentPhoto.vue'
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'

export default {
  name: 'StudentSwitchDialog',
  components: { StudentPhoto },
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
          const savedId = localStorage.getItem('currentStudentId')
          this.selectedId = savedId && this.studentRelations.some(r => r.studentId === savedId)
            ? savedId
            : this.studentRelations[0].studentId
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
      const rel = this.studentRelations.find(r => r.studentId === this.selectedId)
      if (!rel) {
        ElMessage.warning('請選擇一個學生')
        return
      }
      this.confirming = true
      try {
        const res = await service.post(API_ENDPOINTS.SWITCH_STUDENT, {
          studentId: rel.studentId
        })
        if (res.data.code === 200) {
          localStorage.setItem('currentStudentId', rel.studentId)
          localStorage.setItem('currentStudentName', rel.studentName)
          localStorage.setItem('currentStudentClassSection', rel.classSection || '')
          localStorage.setItem('currentStudentProfileNumber', rel.studentProfileNumber || '')
          ElMessage.success({
            message: '已切換至 ' + rel.studentName,
            duration: 1000
          })
          this.close()
          this.$emit('switched', {
            studentId: rel.studentId,
            studentName: rel.studentName,
            classSection: rel.classSection || '',
            studentProfileNumber: rel.studentProfileNumber || ''
          })
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
.ssd-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(6px);
}

.ssd-dialog {
  background: #ffffff;
  border-radius: 18px;
  width: 92%;
  max-width: 400px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.18);
  overflow: hidden;
  animation: ssdScaleIn 0.22s ease-out;
}

@keyframes ssdScaleIn {
  from { transform: scale(0.94) translateY(8px); opacity: 0; }
  to   { transform: scale(1) translateY(0); opacity: 1; }
}

.ssd-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-bottom: 1px solid #dbeafe;
}

.ssd-title {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.ssd-close-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 18px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 0.15s;
}

.ssd-close-btn:hover {
  background: #fef2f2;
  border-color: #f87171;
  color: #b91c1c;
}

.ssd-body {
  padding: 16px 20px;
  max-height: 55vh;
  overflow-y: auto;
}

.ssd-option {
  display: grid;
  grid-template-columns: 48px 1fr 40px;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.15s, box-shadow 0.15s;
  margin-bottom: 8px;
  background: #f8fafc;
  box-shadow: inset 0 0 0 1px #e2e8f0;
}

.ssd-option:last-child {
  margin-bottom: 0;
}

.ssd-option:hover {
  background: #f1f5f9;
}

.ssd-option--active {
  background: #eff6ff;
  box-shadow: inset 0 0 0 2px #3b82f6;
}

.ssd-avatar {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ssd-info {
  min-width: 0;
  text-align: center;
  justify-self: center;
  width: 100%;
}

.ssd-class {
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
  line-height: 1.3;
}

.ssd-name {
  margin-top: 2px;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.ssd-check-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 28px;
  justify-self: end;
}

.ssd-check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #2563eb;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.ssd-empty {
  text-align: center;
  color: #94a3b8;
  padding: 28px 0;
  font-size: 14px;
}

.ssd-footer {
  display: flex;
  gap: 12px;
  padding: 14px 20px 18px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-top: 1px solid #dbeafe;
}

.ssd-btn {
  flex: 1;
  padding: 13px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, transform 0.1s, opacity 0.15s;
}

.ssd-btn--cancel {
  background: #ffffff;
  border: 1px solid #93c5fd;
  color: #1e40af;
}

.ssd-btn--cancel:active {
  background: #eff6ff;
  border-color: #60a5fa;
}

.ssd-btn--confirm {
  border: 1px solid #1d4ed8;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: #ffffff;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.25);
}

.ssd-btn--confirm:active,
.ssd-btn--confirm:disabled {
  opacity: 0.85;
  transform: scale(0.98);
}
</style>
