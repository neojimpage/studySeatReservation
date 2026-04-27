<template>
  <div class="home-container">
    <div class="home-header">
      <div class="header-top">
        <h2>当前可预约座位</h2>
        <button class="profile-button" @click="goToProfile">个人中心</button>
      </div>
      <div class="area-selector">
        <select v-model="selectedAreaId" @change="getSeats">
          <option value="">选择区域</option>
          <option v-for="area in areas" :key="area.id" :value="area.id">{{ area.name }}</option>
        </select>
      </div>
    </div>
    
    <div class="seat-grid">
      <div v-for="seat in seats" :key="seat.id" 
           :class="['seat', getSeatClass(seat.status)]"
           @click="selectSeat(seat)">
        <span class="seat-number">{{ seat.seatNumber }}</span>
        <span v-if="seat.status === '空闲'" class="available">可预约</span>
      </div>
    </div>
    
    <div class="status-legend">
      <div class="legend-item">
        <div class="legend-color available"></div>
        <span>可预约</span>
      </div>
      <div class="legend-item">
        <div class="legend-color reserved"></div>
        <span>已预约</span>
      </div>
      <div class="legend-item">
        <div class="legend-color occupied"></div>
        <span>已占用</span>
      </div>
    </div>
    
    <div v-if="selectedSeat" class="seat-detail">
      <h3>座位详情</h3>
      <p>座位号：{{ selectedSeat.seatNumber }}</p>
      <p>状态：{{ getStatusText(selectedSeat.status) }}</p>
      <button v-if="selectedSeat.status === '空闲'" class="reserve-button" @click="goToSeatSelection">预约</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      areas: [],
      seats: [],
      selectedAreaId: '',
      selectedSeat: null
    }
  },
  mounted() {
    this.getAreas()
  },
  methods: {
    async getAreas() {
      const response = await fetch('/api/student/areas')
      const result = await response.json()
      if (result.code === 200) {
        this.areas = result.data
      }
    },
    async getSeats() {
      if (this.selectedAreaId) {
        const response = await fetch(`/api/student/seats?areaId=${this.selectedAreaId}`)
        const result = await response.json()
        if (result.code === 200) {
          this.seats = result.data
        }
      } else {
        this.seats = []
      }
      this.selectedSeat = null
    },
    selectSeat(seat) {
      this.selectedSeat = seat
    },
    goToSeatSelection() {
      this.$router.push({
        path: '/seat-selection',
        query: { seatId: this.selectedSeat.id, areaId: this.selectedAreaId }
      })
    },
    getSeatClass(status) {
      const classMap = {
        '空闲': '可预约',
        '已预约': '已预约',
        '占用': '已占用',
        '暂离': '暂离',
        '禁用': '禁用'
      }
      return classMap[status] || status
    },
    getStatusText(status) {
      const textMap = {
        '空闲': '空闲',
        '已预约': '已预约',
        '占用': '已占用',
        '暂离': '暂离',
        '禁用': '禁用'
      }
      return textMap[status] || status
    },
    goToProfile() {
      this.$router.push('/my-reservations')
    }
  }
}
</script>

<style scoped>
.home-container {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
}

.home-header {
  margin-bottom: 30px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-top h2 {
  text-align: center;
  color: #333;
  margin: 0;
}

.profile-button {
  padding: 8px 16px;
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.profile-button:hover {
  background: #3a56d7;
}

.area-selector {
  display: flex;
  justify-content: center;
}

.area-selector select {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.seat {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.seat:hover {
  transform: scale(1.05);
}

.seat.可预约 {
  background: #e6f7ee;
  border: 2px solid #52c41a;
}

.seat.已预约 {
  background: #fff1e6;
  border: 2px solid #fa8c16;
}

.seat.已占用 {
  background: #f0f0f0;
  border: 2px solid #d9d9d9;
}

.seat.暂离 {
  background: #e6f7ff;
  border: 2px solid #1890ff;
}

.seat.禁用 {
  background: #fff2f0;
  border: 2px solid #ff4d4f;
}

.seat-number {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.available {
  font-size: 12px;
  color: #52c41a;
  margin-top: 5px;
}

.status-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 3px;
}

.legend-color.available {
  background: #e6f7ee;
  border: 2px solid #52c41a;
}

.legend-color.reserved {
  background: #fff1e6;
  border: 2px solid #fa8c16;
}

.legend-color.occupied {
  background: #f0f0f0;
  border: 2px solid #d9d9d9;
}

.seat-detail {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
}

.seat-detail h3 {
  margin-bottom: 15px;
  color: #333;
}

.seat-detail p {
  margin-bottom: 10px;
  color: #666;
}

.reserve-button {
  padding: 10px 30px;
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 15px;
}

.reserve-button:hover {
  background: #3a56d7;
}
</style>