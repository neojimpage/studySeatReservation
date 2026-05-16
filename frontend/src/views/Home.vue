<template>
  <div class="home-page">
    <div class="top-bar">
      <h3 class="title">📚 自习座位预约</h3>
      <div class="top-actions">
        <button class="ai-btn" @click="$router.push('/ai-assistant')">🤖 AI 助手</button>
        <button class="profile-btn" @click="$router.push('/my-reservations')">👤 我的</button>
      </div>
    </div>
    <div class="filter-bar">
      <span>选择区域：</span>
      <select v-model="selectedAreaId" @change="getSeats">
        <option value="">全部区域</option>
        <option v-for="area in areas" :key="area.id" :value="area.id">{{ area.name }} ({{ area.floor }}F)</option>
      </select>
    </div>
    <div class="seat-grid">
      <div v-for="seat in seats" :key="seat.id"
           :class="['seat-item', statusClass(seat.status)]"
           @click="selectSeat(seat)">
        <span class="seat-num">{{ seat.seatNumber }}</span>
        <span class="seat-label">{{ statusText(seat.status) }}</span>
      </div>
    </div>
    <div class="legend">
      <span v-for="item in legendItems" :key="item.text">
        <i :style="{color: item.color}">●</i> {{ item.text }}
      </span>
    </div>
    <div v-if="selectedSeat" class="seat-detail">
      <p><strong>{{ selectedSeat.seatNumber }}</strong> · {{ selectedAreaName }} · {{ statusText(selectedSeat.status) }}</p>
      <button v-if="selectedSeat.status === '空闲'" class="reserve-btn"
              @click="goReserve">预约此座位</button>
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
      selectedSeat: null,
      legendItems: [
        { text: '空闲', color: '#52c41a' },
        { text: '已预约', color: '#fa8c16' },
        { text: '占用', color: '#999' },
        { text: '暂离', color: '#1890ff' },
      ]
    }
  },
  computed: {
    selectedAreaName() {
      const a = this.areas.find(a => a.id === this.selectedAreaId)
      return a ? a.name : ''
    }
  },
  mounted() { this.getAreas() },
  methods: {
    async getAreas() {
      const resp = await fetch('/api/student/areas')
      const r = await resp.json()
      if (r.code === 200) this.areas = r.data
    },
    async getSeats() {
      if (!this.selectedAreaId) { this.seats = []; this.selectedSeat = null; return }
      const resp = await fetch(`/api/student/seats?areaId=${this.selectedAreaId}`)
      const r = await resp.json()
      if (r.code === 200) this.seats = r.data
      this.selectedSeat = null
    },
    selectSeat(seat) { this.selectedSeat = seat },
    goReserve() {
      this.$router.push({
        path: '/seat-selection',
        query: { seatId: this.selectedSeat.id, areaId: this.selectedAreaId }
      })
    },
    statusClass(s) {
      const m = { '空闲':'free', '已预约':'reserved', '占用':'occupied', '暂离':'away', '禁用':'disabled' }
      return m[s] || 'occupied'
    },
    statusText(s) {
      const m = { '空闲':'空闲', '已预约':'已预约', '占用':'占用', '暂离':'暂离', '禁用':'禁用' }
      return m[s] || s
    }
  }
}
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f6fa; }
.top-bar {
  background: white; padding: 14px 20px;
  display: flex; justify-content: space-between; align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.title { font-size: 18px; color: #333; margin: 0; }
.top-actions { display: flex; gap: 10px; }
.ai-btn {
  padding: 6px 14px; border-radius: 20px; border: none; cursor: pointer;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 13px; font-weight: bold;
}
.profile-btn {
  padding: 6px 14px; border-radius: 20px; border: none; cursor: pointer;
  background: #f0f0f0; font-size: 13px;
}
.filter-bar {
  padding: 16px 20px; display: flex; gap: 10px; align-items: center;
  font-size: 14px; color: #666;
}
.filter-bar select {
  padding: 8px 14px; border: 1.5px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; background: white;
}
.seat-grid {
  padding: 0 20px; display: grid;
  grid-template-columns: repeat(5, 1fr); gap: 10px;
}
.seat-item {
  aspect-ratio: 1; border-radius: 10px; border: 2px solid #eee;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; transition: transform 0.2s;
}
.seat-item:hover { transform: scale(1.05); }
.seat-item.free { background: #e6f7ee; border-color: #52c41a; }
.seat-item.reserved { background: #fff1e6; border-color: #fa8c16; }
.seat-item.occupied { background: #f0f0f0; border-color: #d9d9d9; }
.seat-item.away { background: #e6f7ff; border-color: #1890ff; }
.seat-item.disabled { background: #fff2f0; border-color: #ff4d4f; }
.seat-num { font-size: 14px; font-weight: bold; color: #333; }
.seat-label { font-size: 11px; margin-top: 2px; }
.seat-item.free .seat-label { color: #52c41a; }
.seat-item.reserved .seat-label { color: #fa8c16; }
.seat-item.occupied .seat-label { color: #999; }
.seat-item.away .seat-label { color: #1890ff; }
.legend {
  padding: 16px 20px; display: flex; gap: 18px; justify-content: center;
  font-size: 12px; color: #999;
}
.seat-detail {
  margin: 0 20px 20px; background: white; border-radius: 12px;
  padding: 16px; text-align: center;
}
.seat-detail p { font-size: 14px; color: #333; margin-bottom: 10px; }
.reserve-btn {
  padding: 10px 30px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px; font-size: 15px;
  font-weight: bold; cursor: pointer;
}
</style>
