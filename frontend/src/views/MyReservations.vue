<template>
  <div class="my-reservations-container">
    <div class="header">
      <button class="back-button" @click="$router.back()">返回</button>
      <h2>我的预约</h2>
    </div>
    
    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'current' }]" @click="activeTab = 'current'">当前预约</button>
      <button :class="['tab', { active: activeTab === 'history' }]" @click="activeTab = 'history'">历史预约</button>
      <button :class="['tab', { active: activeTab === 'profile' }]" @click="activeTab = 'profile'">个人中心</button>
    </div>
    
    <div v-if="activeTab === 'current'" class="tab-content">
      <div v-if="currentReservations.length === 0" class="empty-state">
        <p>暂无当前预约</p>
      </div>
      <div v-else class="reservation-list">
        <div v-for="reservation in currentReservations" :key="reservation.id" class="reservation-item">
          <div class="reservation-info">
            <div class="info-row">
              <span class="label">座位号：</span>
              <span class="value">{{ getSeatNumber(reservation.seatId) }}</span>
            </div>
            <div class="info-row">
              <span class="label">时间：</span>
              <span class="value">{{ formatDateTime(reservation.startTime) }} - {{ formatDateTime(reservation.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">状态：</span>
              <span :class="['status', reservation.status]">{{ reservation.status }}</span>
            </div>
          </div>
          <div class="reservation-actions">
            <button v-if="reservation.status === '已预约'" class="action-button cancel" @click="cancelReservation(reservation.id)">取消</button>
            <button v-if="reservation.status === '已开始'" class="action-button leave" @click="leave(reservation.id)">暂离</button>
            <button v-if="reservation.status === '暂离'" class="action-button back" @click="back(reservation.id)">返回</button>
            <button v-if="reservation.status === '已开始' || reservation.status === '暂离'" class="action-button finish" @click="finish(reservation.id)">结束</button>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="historyReservations.length === 0" class="empty-state">
        <p>暂无历史预约</p>
      </div>
      <div v-else class="reservation-list">
        <div v-for="reservation in historyReservations" :key="reservation.id" class="reservation-item">
          <div class="reservation-info">
            <div class="info-row">
              <span class="label">座位号：</span>
              <span class="value">{{ getSeatNumber(reservation.seatId) }}</span>
            </div>
            <div class="info-row">
              <span class="label">时间：</span>
              <span class="value">{{ formatDateTime(reservation.startTime) }} - {{ formatDateTime(reservation.endTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">状态：</span>
              <span :class="['status', reservation.status]">{{ reservation.status }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="activeTab === 'profile'" class="tab-content">
      <div class="profile-info">
        <div class="info-row">
          <span class="label">学号：</span>
          <span class="value">{{ user?.studentId }}</span>
        </div>
        <div class="info-row">
          <span class="label">姓名：</span>
          <span class="value">{{ user?.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">违规次数：</span>
          <span class="value">{{ user?.violationCount || 0 }}</span>
        </div>
        <div class="info-row">
          <span class="label">账号状态：</span>
          <span :class="['status', user?.isRestricted ? 'restricted' : 'normal']">{{ user?.isRestricted ? '已限制' : '正常' }}</span>
        </div>
      </div>
      <button class="logout-button" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyReservations',
  data() {
    return {
      activeTab: 'current',
      currentReservations: [],
      historyReservations: [],
      user: null,
      seats: {}
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    async initData() {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        this.user = user
        await this.getCurrentReservations()
        await this.getHistoryReservations()
      }
    },
    async getCurrentReservations() {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        const response = await fetch(`/api/student/reservations/current?userId=${user.id}`)
        const result = await response.json()
        if (result.code === 200) {
          this.currentReservations = result.data
          for (const reservation of this.currentReservations) {
            await this.getSeatInfo(reservation.seatId)
          }
        }
      }
    },
    async getHistoryReservations() {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        const response = await fetch(`/api/student/reservations?userId=${user.id}`)
        const result = await response.json()
        if (result.code === 200) {
          this.historyReservations = result.data.filter(res => res.status === '已结束' || res.status === '已取消' || res.status === '爽约')
          for (const reservation of this.historyReservations) {
            await this.getSeatInfo(reservation.seatId)
          }
        }
      }
    },
    async getSeatInfo(seatId) {
      if (!this.seats[seatId]) {
        const response = await fetch(`/api/student/seats/${seatId}`)
        const result = await response.json()
        if (result.code === 200) {
          this.seats[seatId] = result.data
        }
      }
    },
    getSeatNumber(seatId) {
      return this.seats[seatId]?.seatNumber || '未知'
    },
    formatDateTime(dateTime) {
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    async cancelReservation(reservationId) {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        try {
          const response = await fetch(`/api/student/reservations/${reservationId}/cancel`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
              userId: user.id
            })
          })
          const result = await response.json()
          if (result.code === 200) {
            alert('取消成功')
            this.getCurrentReservations()
          } else {
            alert(result.message)
          }
        } catch (error) {
          console.error('取消预约失败:', error)
          alert('取消预约失败，请稍后重试')
        }
      }
    },
    async leave(reservationId) {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        const response = await fetch(`/api/student/reservations/${reservationId}/leave`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: new URLSearchParams({
            userId: user.id
          })
        })
        const result = await response.json()
        if (result.code === 200) {
          alert('暂离成功')
          this.getCurrentReservations()
        } else {
          alert(result.message)
        }
      }
    },
    async back(reservationId) {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        const response = await fetch(`/api/student/reservations/${reservationId}/back`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: new URLSearchParams({
            userId: user.id
          })
        })
        const result = await response.json()
        if (result.code === 200) {
          alert('返回成功')
          this.getCurrentReservations()
        } else {
          alert(result.message)
        }
      }
    },
    async finish(reservationId) {
      const user = JSON.parse(localStorage.getItem('user'))
      if (user) {
        const response = await fetch(`/api/student/reservations/${reservationId}/finish`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: new URLSearchParams({
            userId: user.id
          })
        })
        const result = await response.json()
        if (result.code === 200) {
          alert('结束成功')
          this.getCurrentReservations()
          this.getHistoryReservations()
        } else {
          alert(result.message)
        }
      }
    },
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.my-reservations-container {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.back-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
  color: #666;
}

.header h2 {
  color: #333;
}

.tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #ddd;
}

.tab {
  padding: 15px 30px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab:hover {
  color: #4a6cf7;
}

.tab.active {
  color: #4a6cf7;
  border-bottom-color: #4a6cf7;
  font-weight: bold;
}

.tab-content {
  min-height: 300px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #999;
  font-size: 18px;
}

.reservation-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reservation-item {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.reservation-info {
  flex: 1;
}

.info-row {
  margin-bottom: 10px;
  display: flex;
  gap: 10px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-weight: bold;
  min-width: 80px;
}

.value {
  color: #333;
}

.status {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status.已预约 {
  background: #fff1e6;
  color: #fa8c16;
}

.status.已开始 {
  background: #e6f7ee;
  color: #52c41a;
}

.status.已结束 {
  background: #f0f0f0;
  color: #999;
}

.status.已取消 {
  background: #fff2f0;
  color: #ff4d4f;
}

.status.爽约 {
  background: #fff2f0;
  color: #ff4d4f;
}

.status.暂离 {
  background: #e6f7ff;
  color: #1890ff;
}

.status.normal {
  background: #e6f7ee;
  color: #52c41a;
}

.status.restricted {
  background: #fff2f0;
  color: #ff4d4f;
}

.reservation-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-button {
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-button.cancel {
  background: #ff4d4f;
  color: white;
}

.action-button.leave {
  background: #1890ff;
  color: white;
}

.action-button.back {
  background: #52c41a;
  color: white;
}

.action-button.finish {
  background: #fa8c16;
  color: white;
}

.action-button:hover {
  opacity: 0.9;
}

.profile-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
}

.logout-button {
  width: 100%;
  padding: 15px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-button:hover {
  background: #ff7875;
}
</style>