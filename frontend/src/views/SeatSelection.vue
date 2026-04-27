<template>
  <div class="seat-selection-container">
    <div class="header">
      <button class="back-button" @click="$router.back()">返回</button>
      <h2>选择座位</h2>
    </div>
    
    <div class="seat-info">
      <div class="info-item">
        <span class="label">座位号：</span>
        <span class="value">{{ selectedSeat?.seatNumber || '未选择' }}</span>
      </div>
      <div class="info-item">
        <span class="label">区域：</span>
        <span class="value">{{ selectedArea?.name || '未选择' }}</span>
      </div>
    </div>
    
    <div class="time-selection">
      <h3>选择日期</h3>
      <div class="date-picker">
        <button v-for="(date, index) in availableDates" :key="index" 
                :class="['date-button', { active: selectedDate === date.date }]"
                @click="selectDate(date)">
          <div class="date-day">{{ date.day }}</div>
          <div class="date-date">{{ date.date }}</div>
        </button>
      </div>
      
      <h3>选择时段</h3>
      <div class="time-slots">
        <button v-for="(slot, index) in timeSlots" :key="index" 
                :class="['time-slot', { available: slot.available, active: selectedSlot === slot }]"
                @click="selectTimeSlot(slot)">
          <span>{{ slot.startTime }} - {{ slot.endTime }}</span>
        </button>
      </div>
    </div>
    
    <button class="confirm-button" @click="confirmReservation" :disabled="!selectedDate || !selectedSlot || !selectedSlot.available">
      确认预约
    </button>
  </div>
</template>

<script>
export default {
  name: 'SeatSelection',
  data() {
    return {
      selectedSeat: null,
      selectedArea: null,
      availableDates: [],
      selectedDate: '',
      timeSlots: [
        { startTime: '08:00', endTime: '10:00', available: true },
        { startTime: '10:00', endTime: '12:00', available: true },
        { startTime: '14:00', endTime: '16:00', available: true },
        { startTime: '16:00', endTime: '18:00', available: true },
        { startTime: '18:00', endTime: '20:00', available: true },
        { startTime: '20:00', endTime: '22:00', available: true }
      ],
      selectedSlot: null
    }
  },
  mounted() {
    this.initData()
    this.generateAvailableDates()
  },
  methods: {
    async initData() {
      const seatId = this.$route.query.seatId
      const areaId = this.$route.query.areaId
      if (seatId && areaId) {
        // 获取座位信息
        const seatResponse = await fetch(`/api/student/seats/${seatId}`)
        const seatResult = await seatResponse.json()
        if (seatResult.code === 200) {
          this.selectedSeat = seatResult.data
        }
        
        // 获取区域信息
        const areaResponse = await fetch(`/api/student/areas/${areaId}`)
        const areaResult = await areaResponse.json()
        if (areaResult.code === 200) {
          this.selectedArea = areaResult.data
        }
      }
    },
    generateAvailableDates() {
      const dates = []
      const today = new Date()
      for (let i = 0; i < 7; i++) {
        const date = new Date(today)
        date.setDate(today.getDate() + i)
        const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        dates.push({
          day: dayNames[date.getDay()],
          date: date.toISOString().split('T')[0]
        })
      }
      this.availableDates = dates
      this.selectedDate = dates[0].date
    },
    selectDate(date) {
      this.selectedDate = date.date
      this.selectedSlot = null
      // 这里可以根据日期和座位查询可用时段
    },
    selectTimeSlot(slot) {
      if (slot.available) {
        this.selectedSlot = slot
      }
    },
    async confirmReservation() {
      if (!this.selectedDate || !this.selectedSlot || !this.selectedSlot.available) {
        return
      }
      
      const user = JSON.parse(localStorage.getItem('user'))
      if (!user) {
        alert('请先登录')
        this.$router.push('/')
        return
      }
      
      const startTime = `${this.selectedDate}T${this.selectedSlot.startTime}:00`
      const endTime = `${this.selectedDate}T${this.selectedSlot.endTime}:00`
      
      const response = await fetch('/api/student/reservations', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
          userId: user.id,
          seatId: this.selectedSeat.id,
          startTime: startTime,
          endTime: endTime
        })
      })
      
      const result = await response.json()
      if (result.code === 200) {
        alert('预约成功')
        this.$router.push('/my-reservations')
      } else {
        alert(result.message)
      }
    }
  }
}
</script>

<style scoped>
.seat-selection-container {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
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

.seat-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-weight: bold;
}

.value {
  color: #333;
}

.time-selection {
  margin-bottom: 30px;
}

.time-selection h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 16px;
}

.date-picker {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.date-button {
  flex: 1;
  min-width: 80px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s ease;
}

.date-button.active {
  background: #4a6cf7;
  color: white;
  border-color: #4a6cf7;
}

.date-day {
  font-size: 14px;
  margin-bottom: 5px;
}

.date-date {
  font-size: 16px;
  font-weight: bold;
}

.time-slots {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.time-slot {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 10px;
  background: #f5f5f5;
  cursor: not-allowed;
  text-align: center;
  transition: all 0.3s ease;
}

.time-slot.available {
  background: white;
  cursor: pointer;
}

.time-slot.available:hover {
  border-color: #4a6cf7;
  color: #4a6cf7;
}

.time-slot.active {
  background: #4a6cf7;
  color: white;
  border-color: #4a6cf7;
}

.confirm-button {
  width: 100%;
  padding: 15px;
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-button:hover:not(:disabled) {
  background: #3a56d7;
}

.confirm-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>