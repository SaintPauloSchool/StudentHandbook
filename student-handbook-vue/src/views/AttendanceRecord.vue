<template>
  <div class="attendance-container">
    <div class="header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <el-icon class="back-icon"><HomeFilled /></el-icon>
          返回首頁
        </button>
        <div class="date-actions">
          <div class="date-picker-wrapper">
            <button class="date-button" @click="openDatePicker">
              <el-icon class="date-icon"><Calendar /></el-icon>
              選擇時間
            </button>
            <div class="hidden-date-picker">
              <el-date-picker
                ref="datePickerRef"
                v-model="selectedDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :clearable="false"
                @change="onDateChange"
              />
            </div>
          </div>
          <button
            v-if="selectedDate"
            class="reset-date-button"
            @click="resetDateFilter"
          >
            重置時間
          </button>
        </div>
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
        <button class="user-switch-btn" @click="studentDialogVisible = true">切換學生</button>
        <button class="refresh-button" @click="loadRecords" :disabled="loading">
          <el-icon class="refresh-icon" :class="{ rotating: loading }"><Refresh /></el-icon>
          刷新
        </button>
      </div>
    </div>

    <div class="record-list" v-if="records.length > 0">
      <div class="record-item" v-for="record in records" :key="record.id">
        <p class="record-text">{{ formatRecordText(record) }}</p>
      </div>
    </div>

    <div class="empty-state" v-else-if="!loading">
      <div class="empty-icon">📋</div>
      <p class="empty-text">暫無考勤記錄</p>
    </div>

    <div class="loading-state" v-if="loading">
      <div class="loading-spinner"></div>
      <p class="loading-text">加載中...</p>
    </div>

    <StudentSwitchDialog v-model="studentDialogVisible" @switched="onStudentSwitched" />
  </div>
</template>

<script>
import service from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import { API_ENDPOINTS } from '@/config/api.js'
import { HomeFilled, Refresh, Calendar } from '@element-plus/icons-vue'
import StudentSwitchDialog from '@/components/StudentSwitchDialog.vue'
import StudentChip from '@/components/StudentChip.vue'

export default {
  name: 'AttendanceRecord',
  components: { HomeFilled, Refresh, Calendar, StudentSwitchDialog, StudentChip },
  data() {
    return {
      records: [],
      loading: false,
      studentDialogVisible: false,
      selectedDate: null,
      currentStudentName: localStorage.getItem('currentStudentName') || '',
      currentStudentClassSection: localStorage.getItem('currentStudentClassSection') || '',
      currentStudentProfileNumber: localStorage.getItem('currentStudentProfileNumber') || ''
    }
  },
  mounted() {
    this.loadRecords()
    window.addEventListener('studentChanged', this.handleStudentChanged)
  },
  beforeUnmount() {
    window.removeEventListener('studentChanged', this.handleStudentChanged)
  },
  methods: {
    goBack() {
      this.$router.push('/')
    },

    openDatePicker() {
      const picker = this.$refs.datePickerRef
      if (!picker) return
      if (typeof picker.focus === 'function') {
        picker.focus()
      }
      if (typeof picker.handleOpen === 'function') {
        picker.handleOpen()
      }
    },

    onDateChange() {
      if (this.selectedDate) {
        this.loadRecords()
      }
    },

    resetDateFilter() {
      this.selectedDate = null
      this.loadRecords()
    },

    handleStudentChanged() {
      this.currentStudentName = localStorage.getItem('currentStudentName') || ''
      this.currentStudentClassSection = localStorage.getItem('currentStudentClassSection') || ''
      this.currentStudentProfileNumber = localStorage.getItem('currentStudentProfileNumber') || ''
      this.loadRecords()
    },

    onStudentSwitched({ studentName, classSection, studentProfileNumber }) {
      this.currentStudentName = studentName || localStorage.getItem('currentStudentName') || ''
      this.currentStudentClassSection = classSection || localStorage.getItem('currentStudentClassSection') || ''
      this.currentStudentProfileNumber = studentProfileNumber || localStorage.getItem('currentStudentProfileNumber') || ''
      this.loadRecords()
    },

    formatDirection(direction) {
      return String(direction) === '1' ? '離開' : '進入'
    },

    formatRecordText(record) {
      const classSection = record.classSection || this.currentStudentClassSection || ''
      const studentName = record.studentName || this.currentStudentName || ''
      const datetime = record.accessDatetime || ''
      const action = this.formatDirection(record.direction)
      return `貴子弟 ${classSection} ${studentName} 在 ${datetime} ${action} 聖保祿學校`
    },

    async loadRecords() {
      const studentId = localStorage.getItem('currentStudentId')
      if (!studentId) {
        ElMessage.warning('請先選擇學生')
        return
      }

      this.loading = true
      try {
        const params = { studentId }
        if (this.selectedDate) {
          params.accessDate = this.selectedDate
        }
        const response = await service.get(API_ENDPOINTS.STUDENT_ATTENDANCE, { params })
        if (response.data.code === 200) {
          this.records = response.data.rows || []
        } else {
          ElMessage.error(response.data.msg || '獲取考勤記錄失敗')
        }
      } catch (error) {
        console.error('獲取考勤記錄失敗:', error)
        ElMessage.error('獲取考勤記錄失敗，請稍後重試')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.attendance-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f5f9ff;
}

.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: start;
  flex-shrink: 0;
  padding: 15px 20px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
  z-index: 100;
}

.header-left {
  justify-self: start;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.header-center {
  justify-self: center;
  display: flex;
  justify-content: center;
  margin-left: 12px;
}

.header-right {
  justify-self: end;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.student-name-display { max-width: 200px; }

.date-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.date-picker-wrapper {
  position: relative;
}

.date-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #dbeafe 100%);
  color: #1e3a8a;
  border: none;
  box-shadow: 0 4px 6px rgba(147, 197, 253, 0.2);
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.date-button:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.reset-date-button {
  padding: 12px 18px;
  border-radius: 8px;
  background: white;
  color: #64748b;
  border: 1px solid #cbd5e1;
  box-shadow: 0 2px 4px rgba(148, 163, 184, 0.15);
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.reset-date-button:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.date-icon {
  width: 16px;
  height: 16px;
}

.hidden-date-picker {
  position: absolute;
  width: 0;
  height: 0;
  overflow: hidden;
  visibility: hidden;
}

.back-button,
.refresh-button,
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
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  white-space: nowrap;
}

.refresh-button:disabled { opacity: 0.6; cursor: not-allowed; }

.refresh-icon.rotating { animation: rotate 1s linear infinite; }

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.record-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 25px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.record-item {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-left: 4px solid #8b5cf6;
}

.record-text {
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #334155;
  text-align: left;
}

.empty-state,
.loading-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

.empty-icon { font-size: 64px; margin-bottom: 15px; }
.empty-text,
.loading-text { font-size: 16px; color: #909399; }

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e4e7ed;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .header { padding: 12px 15px; }
  .date-button { padding: 8px 12px; font-size: 14px; }
  .reset-date-button { padding: 8px 12px; font-size: 14px; }
  .back-button, .refresh-button, .user-switch-btn { padding: 8px 12px; font-size: 14px; }
  .record-list { padding: 15px; }
  .record-text { font-size: 15px; }
}
</style>
