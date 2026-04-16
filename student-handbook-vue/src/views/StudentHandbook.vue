<template>
  <div class="student-handbook"
       @touchstart="handleTouchStart"
       @touchend="handleTouchEnd">
    <div class="header-container">
      <!-- 頁面標題和按鈕容器 -->
      <div class="title-and-buttons">
        <div class="top-buttons">
          <!-- 返回首頁按鈕 -->
          <el-button class="home-btn" type="primary" @click="goHome">
            <template #icon>
              <el-icon><HomeFilled /></el-icon>
            </template>
            返回首頁
          </el-button>

          <!-- 用戶切換按鈕 -->
          <el-button class="user-switch-btn" type="primary" plain @click="toggleUserMenu">
            <template #icon>
              <el-icon><User /></el-icon>
            </template>
            切換學生
          </el-button>
        </div>

        <!-- 左右方向按鈕 -->
        <div class="navigation-buttons">
          <el-button class="nav-arrow" :class="{'active': activeButton === 'pastMonth'}" type="primary"
                     @click="showPastMonthData" :disabled="false">過去一個月
          </el-button>
          <el-button class="nav-arrow prev-button" :class="{'active': activeButton === 'today'}" type="primary"
                     @click="showTodayData" :disabled="false">當天
          </el-button>
          <el-button class="nav-arrow next-button" :class="{'active': activeButton === 'future'}" type="primary"
                     @click="showNextSevenDaysData" :disabled="false">未來七天
          </el-button>
        </div>
      </div>
    </div>

    <!-- 卡片式數據展示 -->
    <div class="handbook-container" v-loading="loading">
      <div
          class="handbook-card"
          v-for="(item,index) in paginatedGroupedHandbookList"
          :key="index"
      >
        <div class="card-header">
          <h3 class="card-title">{{ item.timeRange }} {{ getDayOfWeek(item.timeRange) }}</h3>
        </div>

        <div class="card-content">
          <!-- 按類別分組顯示條目 -->
          <div v-for="(categoryGroup, categoryIndex) in item.categoryGroups"
               :key="categoryIndex"
               class="category-group">
            <!-- 類別標籤 -->
            <div class="category-container">
              <div class="category-row">
                <span
                    class="category-badge"
                    :class="{ 'exam-badge': categoryGroup.category === '測驗' || categoryGroup.category === '考试' }"
                >
                  {{ categoryGroup.category }}
                </span>
              </div>
            </div>

            <!-- 該類別下的條目 -->
            <div
                class="card-field"
                v-for="(entry, entryIndex) in categoryGroup.entries"
                :key="entryIndex"

            >
              <span class="field-value">{{ entry.subject }} : {{ entry.content }}</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 空狀態 -->
      <div v-if="paginatedGroupedHandbookList.length === 0 && !loading" class="empty-state">
        <p class="no-data-text">暂无数据</p>
      </div>
    </div>
    <!-- 回到頂部按鈕 -->
    <div class="back-to-top" v-show="showBackToTop" @click="scrollToTop">
      <span>回到頂部</span>
    </div>

    <!-- 學生選擇對話框 -->
    <div v-if="studentSelectionDialogVisible" class="custom-modal-overlay" @click="closeStudentSelectionDialog">
      <div class="custom-student-dialog" @click.stop>
        <div class="modal-header">
          <h3>請選擇要切換的學生</h3>
          <button class="close-btn" @click="closeStudentSelectionDialog">×</button>
        </div>
        <div class="student-selection-content">
          <div class="student-list">
            <div class="student-options-group">
              <div
                  v-for="relation in studentRelations"
                  :key="relation.studentUserId"
                  class="student-item-radio"
                  :class="{ 'selected': selectedStudentUserId === relation.studentUserId }"
                  @click="selectStudent(relation.studentUserId, relation.studentName)"
              >
                <span class="student-name">{{ relation.studentName }}</span>
              </div>
            </div>
            <!-- 学生列表为空时的提示 -->
            <div v-if="studentRelations.length === 0" class="empty-student-list">
              <p class="no-student-text">暂无学生数据</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <el-button @click="studentSelectionDialogVisible = false" size="large" class="dialog-cancel-btn">取消
          </el-button>
          <el-button type="primary" @click="confirmStudentSwitchTemp" size="large" class="dialog-confirm-btn">確認
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import service from '@/utils/request.js'
import {API_ENDPOINTS} from '@/config/api.js'
import {ElMessage} from 'element-plus'
import settings from '@/config/settings' // 导入全局配置设置
import { HomeFilled, User } from '@element-plus/icons-vue'

export default {
  name: 'StudentHandbook',
  components: {
    HomeFilled,
    User
  },

  data() {
    return {
      loading: false,
      allGroupedHandbookList: [], // 存儲所有分組後的數據
      currentPage: 1, // 當前頁碼
      pageSize: 7, //每頁顯示條數
      isMobile: false,
      showBackToTop: false,
      activeButton: 'today', // 追蹤當前選中的按鈕

      // 學生選擇相關
      studentSelectionDialogVisible: false, // 控制學生選擇對話框顯示
      selectedStudent: '', // 已選擇的學生姓名（用于显示）
      selectedStudentUserId: '', // 已選擇的學生ID（用于数据传输）
      studentRelations: [], // 存儲家長與學生關係的完整數據

      // 滑動相關數據
      touchStartX: 0,
      touchStartY: 0,
      touchEndX: 0,
      touchEndY: 0
    }
  },
  computed: {
    // 計算當前頁需要顯示的數據
    paginatedGroupedHandbookList() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.allGroupedHandbookList.slice(startIndex, endIndex);
    }
  },
  mounted() {
    this.checkIsMobile()
    this.activeButton = 'today'; // 初始化時設置當天按鈕為活躍狀態
    this.fetchTodayHandbookList()
    window.addEventListener('resize', this.checkIsMobile)
    // 添加滾動事件監聽器
    window.addEventListener('scroll', this.handleScroll)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkIsMobile)
    // 移除滾動事件監聽器
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    //檢查是否為移動設備
    checkIsMobile() {
      this.isMobile = window.innerWidth < 768
    },
    // 返回首頁
    goHome() {
      this.$router.push('/');
    },

    // 切換用戶菜單顯示狀態
    async toggleUserMenu() {
      try {
        // 检查是否启用Token验证
        if (settings.enableTokenAuth) {
          // 从前端存储获取token
          const token = localStorage.getItem('token') || sessionStorage.getItem('token');

          if (!token) {
            ElMessage.error('請先登錄獲取訪問令牌');
            return;
          }
        }

        // 调用后端API获取当前token关联的学生列表
        // token会在axios拦截器中自动添加到请求头
        const response = await service.get(API_ENDPOINTS.STUDENT_HANDBOOK_STUDENTS);

        if (response.data.code === 200) {
          const relations = response.data.data;

          if (relations && relations.length > 0) {
            // 存储完整的关系数据，包含studentName和studentUserId
            this.studentRelations = relations;
            this.selectedStudent = relations[0].studentName; // 默认显示第一个学生的姓名
            this.selectedStudentUserId = relations[0].studentUserId; // 默认选择第一个学生的ID
            this.studentSelectionDialogVisible = true;
          } else {
            ElMessage.info('當前帳號未關聯任何學生');
          }
        } else {
          ElMessage.error(response.data.msg || '獲取學生列表失敗');
        }
      } catch (error) {
        console.error('獲取學生列表失敗:', error);
        ElMessage.error('獲取學生列表失敗: ' + (error.message || '網絡錯誤'));
      }
    },

    //處理滾動事件，控制回到頂部按鈕的顯示
    handleScroll() {
      // 當滾動超過300px時顯示回到頂部按鈕
      this.showBackToTop = window.pageYOffset > 300
    },

    // 滾動到頂部
    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth' // 平滑滾動
      })
    },

    handleTouchStart(event) {
      this.touchStartX = event.touches[0].clientX;
    },
    
    handleTouchEnd(event) {
      this.touchEndX = event.changedTouches[0].clientX;
      this.handleSwipeGesture();
    },
    
    handleSwipeGesture() {
      const deltaX = this.touchEndX - this.touchStartX;
      // 向右滑動返回首頁
      if (Math.abs(deltaX) > 50 && deltaX > 0) {
        this.$router.push('/');
      }
    },

    // 數據分組處理
    groupDataByTime(data) {
      const grouped = {};
      const dataArray = Array.isArray(data) ? data : [];

      dataArray.forEach(item => {
        if (item.courseType !== '功課' && item.courseType !== '測驗') return;

        const timeKey = item.startDate || item.updateDate || '未設定日期';
        if (!grouped[timeKey]) {
          grouped[timeKey] = {
            timeRange: timeKey,
            entries: [],
            categories: {}
          };
        }

        const entry = {
          subject: item.course || '未設定課程',
          content: item.content || '無內容',
          category: item.courseType || '未分類'
        };
        
        grouped[timeKey].entries.push(entry);

        const category = item.courseType || '未分類';
        if (!grouped[timeKey].categories[category]) {
          grouped[timeKey].categories[category] = {
            category: category,
            entries: []
          };
        }
        grouped[timeKey].categories[category].entries.push(entry);
      });

      this.processGroupedData(grouped);
      this.currentPage = 1;
    },

    // 處理分組後的數據（帶排序）
    processGroupedData(grouped) {
      // 按時間排序
      this.allGroupedHandbookList = Object.values(grouped).sort((a, b) => 
        this.parseDate(a.timeRange) - this.parseDate(b.timeRange)
      );

      // 處理每個分組
      this.allGroupedHandbookList.forEach(item => {
        // 過濾並排序類別
        item.categoryGroups = Object.values(item.categories)
          .filter(cat => cat.category === '功課' || cat.category === '測驗')
          .sort((a, b) => {
            if (a.category === '測驗' && b.category !== '測驗') return -1;
            if (b.category === '測驗' && a.category !== '測驗') return 1;
            return a.category.localeCompare(b.category);
          });

        // 排序每個類別內的條目
        item.categoryGroups.forEach(categoryGroup => {
          categoryGroup.entries.sort((a, b) => 
            a.subject.localeCompare(b.subject) || a.content.localeCompare(b.content)
          );
        });
      });
    },

    // 解析日期字符串為Date對象的輔助函數
    parseDate(dateString) {
      if (!dateString) return new Date();

      // 如果是包含時間的完整日期格式
      if (typeof dateString === 'string' && dateString.includes(':')) {
        return new Date(dateString);
      }

      // 如果是 dd/mm/yyyy 格式
      if (typeof dateString === 'string' && dateString.includes('/')) {
        const parts = dateString.split('/');
        const day = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1; // 月份從0開始
        const year = parseInt(parts[2], 10);
        return new Date(year, month, day);
      }

      // 如果是 yyyy-mm-dd 格式
      if (typeof dateString === 'string' && dateString.includes('-')) {
        return new Date(dateString);
      }

      return new Date();
    },

    // 獲取日期對應的星期幾
    getDayOfWeek(dateString) {
      if (!dateString) return '';

      const date = this.parseDate(dateString);
      if (!(date instanceof Date) || isNaN(date)) return '';

      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return `(${weekdays[date.getDay()]})`;
    },

    // 顯示當天數據
    showTodayData() {
      this.activeButton = 'today'; // 更新活動按鈕狀態
      // 直接調用新的接口獲取當天的數據
      this.fetchTodayHandbookList();
      // 滾動到頂部
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    },

    // 通用數據獲取方法
    async fetchData(endpoint, groupMethod = 'groupDataByTime') {
      this.loading = true;
      try {
        // 構建帶有studentUserId參數的URL
        let url = endpoint;
        if (this.selectedStudentUserId) {
          const separator = url.includes('?') ? '&' : '?';
          url += `${separator}studentUserId=${encodeURIComponent(this.selectedStudentUserId)}`;
        }
        
        // 使用封裝的service實例，確保攜帶token
        const response = await service.get(url);

        // 根據後端返回的數據結構處理數據
        let rawData = [];
        if (response.data && response.data.rows) {
          rawData = response.data.rows;
        } else if (Array.isArray(response.data)) {
          rawData = response.data;
        } else {
          rawData = response.data;
        }

        // 根據指定方法分組數據
        this[groupMethod](rawData);

        // 重置到第一頁
        this.currentPage = 1;

      } catch (error) {
        console.error(`獲取數據失敗:`, error);
        ElMessage.error('獲取數據失敗: ' + (error.message || '未知錯誤'));
        // 使用空數組
        this.groupDataByTime([]);
      } finally {
        this.loading = false;
      }
    },

    // 獲取當天的學生手冊列表
    async fetchTodayHandbookList() {
      await this.fetchData(API_ENDPOINTS.STUDENT_HANDBOOK_TODAY, 'groupDataByTime');
    },

    // 顯示過去一個月數據
    showPastMonthData() {
      this.activeButton = 'pastMonth'; // 更新活動按鈕狀態
      // 直接調用新的接口獲取過去一個月的數據
      this.fetchPastMonthHandbookList();
      // 滾動到頂部
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    },

    // 獲取過去一個月的學生手冊列表
    async fetchPastMonthHandbookList() {
      await this.fetchData(API_ENDPOINTS.STUDENT_HANDBOOK_PAST_MONTH, 'groupDataByTimeWithoutSort');
    },

    //按時間分組數據但不排序（保持後端返回的順序）
    groupDataByTimeWithoutSort(data) {
      const grouped = {};
      const order = []; // 保存原始順序

      //按時間分組，使用class_log表的字段
      data.forEach(item => {
        // 过滤非'功課'和'測驗'类型的条目
        if (item.courseType !== '功課' && item.courseType !== '測驗') {
          return;
        }

        // 使用startDate作为分组键
        const timeKey = item.startDate || item.updateDate || '未設定日期';
        if (!grouped[timeKey]) {
          grouped[timeKey] = {
            timeRange: timeKey, // 使用日期作为卡片标题
            entries: [],
            categories: {} // 用於存儲類別分組
          };
          order.push(timeKey); // 记录原始顺序
        }

        //添加條目到總列表
        grouped[timeKey].entries.push({
          subject: item.course || '未設定課程',
          content: item.content || '無內容',
          category: item.courseType || '未分類'
        });

        //按類別分組
        const category = item.courseType || '未分類';
        if (!grouped[timeKey].categories[category]) {
          grouped[timeKey].categories[category] = {
            category: category,
            entries: []
          };
        }
        grouped[timeKey].categories[category].entries.push({
          subject: item.course || '未設定課程',
          content: item.content || '無內容',
          category: item.courseType || '未分類'
        });
      });

      this.processGroupedDataPreserveOrder(grouped, order);
      this.currentPage = 1;
    },

    // 處理分組數據（保持順序）
    processGroupedDataPreserveOrder(grouped, order) {
      this.allGroupedHandbookList = order.map(timeKey => {
        const item = grouped[timeKey];
        
        // 處理類別
        item.categoryGroups = Object.values(item.categories)
          .filter(cat => cat.category === '功課' || cat.category === '測驗')
          .sort((a, b) => {
            if (a.category === '測驗' && b.category !== '測驗') return -1;
            if (b.category === '測驗' && a.category !== '測驗') return 1;
            return a.category.localeCompare(b.category);
          });

        // 排序列內條目
        item.categoryGroups.forEach(categoryGroup => {
          categoryGroup.entries.sort((a, b) => 
            a.subject.localeCompare(b.subject) || a.content.localeCompare(b.content)
          );
        });

        return item;
      });
    },

    // 顯示未來七天數據
    showNextSevenDaysData() {
      this.activeButton = 'future'; // 更新活動按鈕狀態
      // 直接調用新的接口獲取未來七天（不含今天）的數據
      this.fetchNextSevenDaysHandbookList();
      // 滾動到頂部
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    },

    // 獲取未來七天（不含今天）的學生手冊列表
    async fetchNextSevenDaysHandbookList() {
      await this.fetchData(API_ENDPOINTS.STUDENT_HANDBOOK_NEXT_SEVEN_DAYS, 'groupDataByTime');
    },

    // 关闭学生选择对话框
    closeStudentSelectionDialog() {
      this.studentSelectionDialogVisible = false;
    },

    // 选择学生
    selectStudent(studentUserId, studentName) {
      this.selectedStudentUserId = studentUserId;
      this.selectedStudent = studentName;
    },

    // 確認切換學生
    async confirmStudentSwitchTemp() {
      if (!this.selectedStudentUserId) {
        ElMessage.warning('請選擇一個學生');
        return;
      }

      try {
        // 驗證必要數據
        if (!this.studentRelations?.length || !this.selectedStudent || !this.selectedStudentUserId) {
          ElMessage.error('學生信息不完整或未加載');
          return;
        }

        // 驗證學生關聯關係
        const isValidStudent = this.studentRelations.some(relation => 
          relation.studentName === this.selectedStudent && 
          relation.studentUserId === this.selectedStudentUserId
        );

        if (!isValidStudent) {
          ElMessage.error('家長未關聯該學生，無法切換');
          return;
        }

        // 調用後端API
        const response = await service.post(API_ENDPOINTS.SWITCH_STUDENT, {
          studentName: this.selectedStudent,
          studentUserId: this.selectedStudentUserId
        });

        if (response.data.code === 200) {
          ElMessage.success({
            message: '已成功切換到學生',
            duration: 1000
          });
          this.studentSelectionDialogVisible = false;

          // 根據當前視圖刷新數據
          const refreshMethods = {
            'today': this.fetchTodayHandbookList,
            'pastMonth': this.fetchPastMonthHandbookList,
            'future': this.fetchNextSevenDaysHandbookList
          };
          
          const refreshMethod = refreshMethods[this.activeButton] || this.fetchTodayHandbookList;
          await refreshMethod.call(this);
        } else {
          ElMessage.error(response.data.msg || '切換學生失敗');
        }
      } catch (error) {
        console.error('切換學生失敗:', error);
        ElMessage.error('切換學生失敗: ' + (error.message || '未知錯誤'));
      }
    }
  }
}
</script>

<style scoped>
.student-handbook {
  padding: 0 !important;
  margin: 0 !important;
  position: relative;
  top: 0;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%) !important; /* 更淺的藍色漸變背景 */
  min-height: 100vh;
}

.header-container {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%); /* 更淺的藍色漸變 */
  padding: 15px 30px; /* 減少上方內邊距 */
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
  justify-content: center; /* 讓內容居中 */
}

.title-and-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; /* 內容居中對齊 */
  width: 100%;
  max-width: 800px; /* 限制最大寬度 */
  gap: 15px; /* 組件間距 */
}

.top-buttons {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  width: 100%;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 12px;
  flex-shrink: 0; /* 防止按鈕容器收縮 */
}


.home-btn {
  margin-right: 0; /* 移除固定間距，使用gap控制 */
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); /* 橙色漸層 */
  color: #92400e; /* 深橙色文字 */
  border: none;
  box-shadow: 0 4px 6px rgba(245, 158, 11, 0.2);
  transition: all 0.3s ease;
  white-space: nowrap;
  font-weight: 600;
  font-size: 15px;
}

.home-btn:hover {
  background: linear-gradient(135deg, #fbbf24 0%, #fcd34d 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(245, 158, 11, 0.3);
}

.user-switch-btn {
  margin-right: 0; /* 移除固定間距，使用gap控制 */
  padding: 12px 18px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #dbeafe 100%); /* 按照項目規範的淺藍色漸變 */
  color: #1e3a8a; /* 按照項目規範的深藍色文字 */
  border: none;
  box-shadow: 0 4px 6px rgba(147, 197, 253, 0.2);
  transition: all 0.3s ease;
  white-space: nowrap;
  font-weight: 600;
  font-size: 15px;
}

.user-switch-btn:hover {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 10px rgba(147, 197, 253, 0.3);
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  width: 100%;
  flex-shrink: 0; /* 防止按鈕容器收縮 */
}

.handbook-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
  margin: 15px 0 50px 0 !important; /* 減少上方邊距 */
  padding: 0 25px 0 25px; /* 移除底部內邊距，由按鈕提供空間 */
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%) !important; /* 更淺的藍色漸變背景 */
}

.handbook-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 更輕微的陰影 */
  transition: all 0.3s ease;
  transform: translateY(0);
  background: white;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  border: none;
  position: relative;
  overflow: visible;
}

.handbook-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #7dd3fc, #bae6fd, #bae6fd); /* 更淺的藍色條 */
  z-index: 1;
}

.handbook-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 20px rgba(125, 211, 252, 0.2); /* 淺藍色陰影 */
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center; /* 按照項目規範設置居中對齊 */
  padding: 20px;
  border-bottom: 2px solid #606266; /* 按照項目規範設置分割線 */
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%); /* 更淺的藍色漸變 */
  color: #0284c7; /* 淺藍色文字 */
}

.card-title {
  margin: 0;
  font-size: 24px; /* 按照規範增大字體 */
  font-weight: 700;
  color: #0284c7; /* 淺藍色文字 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center; /* 按照項目規範設置居中對齊 */
}

.card-content {
  padding: 25px; /* 按照規範增加內邊距 */
  text-align: left; /* 按照規範設置左對齊 */
  flex-grow: 1;
}

.category-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin: 12px 0;
  gap: 10px;
}

.category-row {
  display: flex;
  justify-content: flex-start;
  width: 100%;
}

.category-badge {
  background: #409eff; /* 普通類別使用藍色背景 */
  color: white; /* 白色文字 */
  padding: 8px 16px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600; /* 粗體 */
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(125, 211, 252, 0.2);
}

.category-badge:hover {
  transform: scale(1.05);
}

/* 測驗/考試類別的特殊樣式 */
.exam-badge {
  background: #e91e63 !important; /* 粉紅色背景 */
  color: white; /* 白色文字 */
  font-weight: 600; /* 粗體 */
}

.card-field {
  display: flex;
  margin-bottom: 16px;
  font-size: 16px; /* 按照規範增大字體 */
  text-align: left;
  line-height: 1.6;
  padding: 10px 0;
  border-bottom: 1px solid #e0f2fe; /* 使用更協調的淺藍色分割線 */
  transition: all 0.3s ease;
  border-radius: 6px;
  background: #f8fafc;
  padding-left: 15px;
}

.card-field:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.field-value {
  flex: 1;
  color: #475569;
  transition: all 0.3s ease;
  font-weight: 500;
}

.handbook-card:hover .field-value {
  color: #0284c7; /* 懸停時文字變成淺藍色 */
  transform: translateX(5px);
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin: 20px;
}

.no-data-text {
  font-weight: bold;
  text-align: center;
  font-size: 18px;
  color: #606266;
  margin: 0;
}

/* 回到頂部按鈕樣式 */
.back-to-top {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background: linear-gradient(135deg, #7dd3fc 0%, #bae6fd 100%); /* 更淺的藍色漸變 */
  color: #0284c7; /* 淺藍色文字 */
  border-radius: 0; /* 長方形 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 -2px 8px rgba(125, 211, 252, 0.3);
  z-index: 1000;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 16px;
}

.back-to-top:hover {
  background: linear-gradient(135deg, #bae6fd 0%, #e0f2fe 100%);
  box-shadow: 0 -4px 12px rgba(125, 211, 252, 0.4);
}

.back-to-top span {
  color: #0284c7; /* 淺藍色文字 */
}

/* 導航按鈕樣式 */
.nav-arrow {
  padding: 12px 18px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #60a5fa 0%, #93c5fd 100%); /* 按照用戶要求的新漸變配色 */
  color: #1e40af; /* 深藍色文字 */
  border: none;
  box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2);
  white-space: nowrap; /* 防止文字換行 */
  min-width: auto; /* 避免按鈕過大 */
}

.nav-arrow:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  opacity: 0.6;
  color: #6b7280;
}

.nav-arrow.active {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%); /* 統一的高亮藍色漸層 */
  color: white; /* 白色文字提高對比度 */
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.4);
  transform: translateY(-2px);
}

.prev-button:hover, .next-button:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 10px rgba(37, 99, 235, 0.3);
}

/* 手機端優化 */
@media (max-width: 768px) {
  .handbook-container {
    padding: 0 15px 30px;
    gap: 20px;
  }

  .handbook-card {
    border-radius: 10px;
  }

  .card-title {
    font-size: 20px;
  }

  .card-content {
    padding: 20px;
  }

  .category-badge {
    padding: 6px 14px;
    font-size: 13px;
  }

  .card-field {
    font-size: 15px;
  }
}

/* 學生選擇項目的樣式 */
.student-options-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 5px 0;
  align-items: stretch;
}

.student-list {
  padding: 10px;
  border-radius: 8px;
  background: transparent;
  border: none; /* 移除邊框 */
  max-height: unset; /* 移除最大高度限制 */
  overflow-y: visible; /* 移除滾動條 */
}

.empty-student-list {
  text-align: center;
  padding: 20px;
}

.no-student-text {
  font-weight: bold;
  text-align: center;
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.student-item-radio {
  display: block;
  width: calc(100% - 20px);
  max-width: 100%;
  padding: 12px 16px;
  margin: 8px 0;
  border: 1px solid #d1e5f5; /* 添加淡藍色框線 */
  border-radius: 8px;
  background: transparent; /* 透明背景 */
  transition: all 0.3s ease;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-item-radio:hover {
  background: #dbeafe; /* 懸停時的淺藍色背景 */
  border: 1px solid #3b82f6 !important; /* 懸停時的藍色邊框 */
  color: #1e40af !important; /* 懸停時的深藍色文字 */
  transform: translateY(-1px);
}

/* 選中狀態的樣式 */
.student-item-radio.selected {
  background: #2563eb !important; /* 選中時的深藍色背景 */
  border: 2px solid #1d4ed8 !important; /* 選中時的深藍色邊框 */
  color: white !important; /* 選中時的白色文字 */
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4) !important;
  transform: translateY(-2px);
}

.student-item-radio.selected .student-name {
  color: white !important;
  font-weight: 700 !important;
  text-shadow: 0 0 2px rgba(255, 255, 255, 0.5) !important;
}

.student-name {
  font-size: 16px;
  color: #374151; /* 默認深灰色文字 */
  transition: all 0.3s ease;
  font-weight: 500;
}


.dialog-cancel-btn {
  background: linear-gradient(135deg, #60a5fa 0%, #93c5fd 100%) !important;
  border: none !important;
  color: #1e40af !important;
  font-weight: 600;
  padding: 12px 24px !important;
  font-size: 14px !important;
  min-width: 100px;
  margin: 0 8px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2) !important;
  border-radius: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.dialog-cancel-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  color: white !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.4) !important;
}

.dialog-confirm-btn {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%) !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
  padding: 12px 24px !important;
  font-size: 14px !important;
  min-width: 100px;
  margin: 0 8px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 6px rgba(96, 165, 250, 0.2) !important;
  border-radius: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.dialog-confirm-btn:hover {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37, 99, 235, 0.4) !important;
}

/* 自定义模态对话框样式 */
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.4) 0%, rgba(12, 74, 160, 0.6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(8px);
}

.custom-student-dialog {
  background: transparent;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  width: 30%;
  max-width: 90%;
  animation: modalSlideIn 0.3s ease-out;
}

.modal-header {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  padding: 20px;
  border-radius: 16px 16px 0 0;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  color: white;
  font-weight: 700;
  font-size: 18px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.student-selection-content {
  padding: 25px;
  background: transparent;
  color: white;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
  padding: 0 25px 25px;
}

/* 手机端适配 */
@media (max-width: 768px) {
  .custom-student-dialog {
    width: 90%;
  }
}
</style>