<template>
  <div class="calendar-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <el-icon class="back-icon"><HomeFilled /></el-icon>
          返回首頁
        </button>
      </div>
      <div class="header-right">
        <div class="date-picker-wrapper">
          <button class="action-button" @click="openDatePicker">
            <el-icon class="action-icon"><CalendarIcon /></el-icon>
            選擇年月
          </button>
          <div style="position: absolute; width: 0; height: 0; overflow: hidden; visibility: hidden;">
            <el-date-picker
              ref="datePickerRef"
              v-model="selectedMonth"
              type="month"
              format="YYYY-MM"
              value-format="YYYY-MM"
              :clearable="false"
              @change="onMonthChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Second Row: Navigation -->
    <div class="nav-row">
      <el-button class="nav-btn" @click="prevMonth" circle>
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="current-month-display">{{ formattedCurrentMonth }}</h2>
      <el-button class="nav-btn" @click="nextMonth" circle>
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- Calendar List -->
    <div class="calendar-wrapper">
      <div class="calendar-header">
        <div class="col-weekday">星期</div>
        <div class="col-date">日期</div>
        <div class="col-event">行事</div>
      </div>
      <div class="calendar-body">
        <div
          v-for="day in calendarDays"
          :key="day.dateStr"
          :class="['calendar-row', { 'is-today': day.isToday, 'has-event': day.hasEvent }]"
          :ref="day.isToday ? 'todayRow' : null"
        >
          <div class="col-weekday">{{ day.weekday }}</div>
          <div class="col-date">
            <span class="date-badge">{{ day.dateNum }}</span>
          </div>
          <div class="col-event">
            <div v-if="day.events.length > 0" class="events-container">
              <div v-for="(ev, idx) in day.events" :key="idx" :class="['event-item', `event-type-${ev.targetType}`]">
                <span :class="['event-dot', `dot-type-${ev.targetType}`]"></span>
                {{ ev.title }}（{{ getEventTypeName(ev.targetType) }}）
              </div>
            </div>
            <div v-else class="no-event">-</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowLeft, ArrowRight, HomeFilled, Calendar as CalendarIcon } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { API_ENDPOINTS } from '@/config/api'

export default {
  name: 'CalendarView',
  components: { ArrowLeft, ArrowRight, HomeFilled, CalendarIcon },
  data() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    return {
      selectedMonth: `${year}-${month}`,
      calendarDays: [],
      mockEvents: {}
    }
  },
  computed: {
    formattedCurrentMonth() {
      if (!this.selectedMonth) return '';
      const [year, month] = this.selectedMonth.split('-');
      return `${year}年 ${month}月`;
    }
  },
  watch: {
    selectedMonth() {
      this.generateCalendar();
    }
  },
  mounted() {
    this.generateCalendar();
  },
  methods: {
    goBack() {
      this.$router.push('/');
    },
    prevMonth() {
      if (!this.selectedMonth) return;
      let [year, month] = this.selectedMonth.split('-').map(Number);
      month -= 1;
      if (month < 1) {
        month = 12;
        year -= 1;
      }
      this.selectedMonth = `${year}-${String(month).padStart(2, '0')}`;
    },
    nextMonth() {
      if (!this.selectedMonth) return;
      let [year, month] = this.selectedMonth.split('-').map(Number);
      month += 1;
      if (month > 12) {
        month = 1;
        year += 1;
      }
      this.selectedMonth = `${year}-${String(month).padStart(2, '0')}`;
    },
    onMonthChange(val) {
      if (!val) {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        this.selectedMonth = `${year}-${month}`;
      }
    },
    openDatePicker() {
      // 在 Element Plus 中，可以透過 focus() 展開日期選擇器
      if (this.$refs.datePickerRef) {
        this.$refs.datePickerRef.focus();
        // 如果 focus 不行，也可以嘗試 handleOpen (視 Element Plus 版本而定)
        if (typeof this.$refs.datePickerRef.handleOpen === 'function') {
          this.$refs.datePickerRef.handleOpen();
        }
      }
    },
    getEventTypeName(type) {
      switch (type) {
        case 0: return '全校';
        case 1: return '幼稚園';
        case 2: return '小學';
        case 3: return '中學';
        default: return '未知';
      }
    },
    async fetchEvents() {
      try {
        const response = await request.get(API_ENDPOINTS.CALENDAR_LIST, {
          params: { yearMonth: this.selectedMonth }
        });
        if (response.data.code === 200) {
          const eventsList = response.data.data || [];
          // 將 API 返回的陣列轉換為以 dateStr 為 key 的物件
          this.mockEvents = {};
          eventsList.forEach(ev => {
            const dateStr = ev.eventDate;
            if (!this.mockEvents[dateStr]) {
              this.mockEvents[dateStr] = [];
            }
            this.mockEvents[dateStr].push(ev);
          });
        }
      } catch (error) {
        console.error("獲取行事曆失敗", error);
        this.mockEvents = {};
      }
    },
    async generateCalendar() {
      if (!this.selectedMonth) return;
      const [year, month] = this.selectedMonth.split('-').map(Number);
      
      // 先取得該月資料
      await this.fetchEvents();
      
      // 獲取該月的天數
      const daysInMonth = new Date(year, month, 0).getDate();
      
      const today = new Date();
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

      const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      const days = [];

      for (let i = 1; i <= daysInMonth; i++) {
        const dateObj = new Date(year, month - 1, i);
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        const events = this.mockEvents[dateStr] || [];

        days.push({
          dateStr: dateStr,
          dateNum: i,
          weekday: weekdays[dateObj.getDay()],
          isToday: dateStr === todayStr,
          hasEvent: events.length > 0,
          events: events
        });
      }
      this.calendarDays = days;
      
      // 在 DOM 更新後滾動到今天
      this.$nextTick(() => {
        this.scrollToToday();
      });
    },
    scrollToToday() {
      // 確保是當前月份才滾動
      const today = new Date();
      const currentMonthStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
      if (this.selectedMonth === currentMonthStr) {
        setTimeout(() => {
          if (this.$refs.todayRow && this.$refs.todayRow[0]) {
            this.$refs.todayRow[0].scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        }, 100);
      }
    }
  }
}
</script>

<style scoped>
.calendar-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background-color: #f5f9ff;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box;
  overflow: hidden;
}

/* 顶部导航栏 */
.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 15px 20px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%);
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
  margin-bottom: 20px;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
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

.back-button:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.back-icon {
  width: 16px;
  height: 16px;
}

.date-picker-wrapper {
  position: relative;
  display: inline-block;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
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

.action-button:hover {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
}

.action-button:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.action-icon {
  width: 16px;
  height: 16px;
}

/* --- Navigation Row --- */
.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 26px;
}

.nav-btn {
  background: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  color: #334155;
  font-size: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.nav-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
  color: #2563eb;
}

.current-month-display {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
  letter-spacing: 0.5px;
}

/* --- Calendar List --- */
.calendar-wrapper {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin: 0 20px 20px;
}

.calendar-header {
  display: flex;
  background: rgba(241, 245, 249, 0.8);
  padding: 14px 0;
  border-bottom: 2px solid #e2e8f0;
  font-weight: 700;
  color: #475569;
  font-size: 15px;
}

.col-weekday {
  width: 65px;
  text-align: center;
  flex-shrink: 0;
  color: #64748b;
}

.col-date {
  width: 55px;
  text-align: center;
  flex-shrink: 0;
}

.col-event {
  flex: 1;
  padding-left: 12px;
  padding-right: 16px;
}

.calendar-body {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

/* 自定義滾動條 */
.calendar-body::-webkit-scrollbar {
  width: 6px;
}
.calendar-body::-webkit-scrollbar-track {
  background: transparent;
}
.calendar-body::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 10px;
}

.calendar-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e2e8f0;
  transition: background-color 0.2s;
}

.calendar-row:hover {
  background-color: rgba(255, 255, 255, 0.8);
}

/* 當天高亮 */
.calendar-row.is-today {
  background-color: #fef0f0; /* 淺紅色背景突顯當天 */
  position: relative;
}

.calendar-row.is-today::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background-color: #f56c6c;
  border-radius: 0 4px 4px 0;
}

.calendar-row.is-today .date-badge {
  background-color: #f56c6c;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}

/* 有事件的行用淺藍色高亮 */
.calendar-row.has-event {
  background-color: #eff6ff; /* 淺藍色背景 */
  border-bottom-color: #dbeafe; /* 加深分隔線，避免連成一塊 */
}

.calendar-row.has-event:hover {
  background-color: #e0f2fe;
}

.date-badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
}

.no-event {
  color: #94a3b8;
  font-size: 14px;
  font-style: italic;
}

.events-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.event-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  /* 預設樣式 */
  background: white;
  color: #1e3a8a;
}

/* 0: 全校 - 淡藍色 */
.event-type-0 {
  background-color: #dbeafe;
  color: #1e40af;
}

/* 1: 幼稚園 - 淡粉橘色 */
.event-type-1 {
  background-color: #ffedd5;
  color: #9a3412;
}

/* 2: 小學 - 淡綠色 */
.event-type-2 {
  background-color: #dcfce7;
  color: #166534;
}

/* 3: 中學 - 淡黃色 */
.event-type-3 {
  background-color: #fef9c3;
  color: #854d0e;
}

.event-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
  /* 預設樣式 */
  background-color: #3b82f6;
}

/* 0: 全校點 */
.dot-type-0 {
  background-color: #2563eb;
}

/* 1: 幼稚園點 */
.dot-type-1 {
  background-color: #f97316;
}

/* 2: 小學點 */
.dot-type-2 {
  background-color: #22c55e;
}

/* 3: 中學點 */
.dot-type-3 {
  background-color: #eab308;
}
</style>
