<template>
  <div class="page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h3>确认预约</h3>
    </div>

    <div class="info-card">
      <div class="info-left">
        <span class="label">座位</span>
        <span class="value">{{ selectedSeat?.seatNumber || '--' }}</span>
      </div>
      <div class="info-right">
        <span class="label">区域</span>
        <span class="value">{{ selectedArea?.name || '--' }} · {{ selectedArea?.floor }}F</span>
      </div>
    </div>

    <div class="section">
      <span class="section-label">选择日期</span>
      <div class="date-row">
        <div v-for="d in dates" :key="d.date"
             :class="['date-chip', { active: selectedDate === d.date }]"
             @click="selectedDate = d.date">
          <span>{{ d.day }}</span>
          <strong>{{ d.short }}</strong>
        </div>
      </div>
    </div>

    <div class="section">
      <span class="section-label">选择时段</span>
      <div class="slot-grid">
        <div v-for="s in slots" :key="s.start"
             :class="['slot', { active: selectedSlot === s, disabled: !s.available }]"
             @click="selectSlot(s)">
          {{ s.start }} - {{ s.end }}
        </div>
      </div>
    </div>

    <button class="confirm-btn" :disabled="!canConfirm" @click="confirmReservation">确认预约</button>
  </div>
</template>

<script>
export default {
  name: 'SeatSelection',
  data() {
    return {
      selectedSeat: null,
      selectedArea: null,
      selectedDate: '',
      selectedSlot: null,
      dates: [],
      slots: [
        { start: '08:00', end: '10:00', available: true },
        { start: '10:00', end: '12:00', available: true },
        { start: '14:00', end: '16:00', available: true },
        { start: '16:00', end: '18:00', available: true },
        { start: '18:00', end: '20:00', available: true },
        { start: '20:00', end: '22:00', available: false },
      ]
    }
  },
  computed: {
    canConfirm() { return this.selectedDate && this.selectedSlot && this.selectedSlot.available }
  },
  mounted() { this.initData(); this.genDates() },
  methods: {
    async initData() {
      const seatId = this.$route.query.seatId, areaId = this.$route.query.areaId
      if (seatId) {
        const r = await fetch(`/api/student/seats/${seatId}`)
        const j = await r.json(); if (j.code === 200) this.selectedSeat = j.data
      }
      if (areaId) {
        const r = await fetch(`/api/student/areas`)
        const j = await r.json()
        if (j.code === 200) this.selectedArea = j.data.find(a => a.id == areaId)
      }
    },
    genDates() {
      const days = ['周日','周一','周二','周三','周四','周五','周六']
      const now = new Date()
      for (let i = 0; i < 7; i++) {
        const d = new Date(now); d.setDate(now.getDate() + i)
        this.dates.push({
          day: days[d.getDay()],
          short: `${d.getMonth()+1}/${d.getDate()}`,
          date: d.toISOString().split('T')[0]
        })
      }
      this.selectedDate = this.dates[0].date
    },
    selectSlot(s) { if (s.available) this.selectedSlot = s },
    async confirmReservation() {
      const user = JSON.parse(localStorage.getItem('user'))
      if (!user) { alert('请先登录'); this.$router.push('/'); return }
      const start = `${this.selectedDate}T${this.selectedSlot.start}:00`
      const end = `${this.selectedDate}T${this.selectedSlot.end}:00`
      const r = await fetch('/api/student/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ userId: user.id, seatId: this.selectedSeat.id, startTime: start, endTime: end })
      })
      const j = await r.json()
      if (j.code === 200) { alert('预约成功'); this.$router.push('/my-reservations') }
      else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6fa; padding: 0 20px 30px; }
.header { display: flex; align-items: center; padding: 16px 0; }
.back-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #666; margin-right: 12px; }
.header h3 { font-size: 17px; color: #333; margin: 0; }
.info-card {
  background: white; border-radius: 12px; padding: 18px; margin-bottom: 20px;
  display: flex; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.info-left, .info-right { display: flex; flex-direction: column; }
.label { font-size: 12px; color: #999; }
.value { font-size: 16px; font-weight: bold; color: #333; margin-top: 4px; }
.section { margin-bottom: 20px; }
.section-label { font-size: 13px; color: #666; display: block; margin-bottom: 8px; }
.date-row { display: flex; gap: 8px; overflow-x: auto; }
.date-chip {
  flex-shrink: 0; padding: 10px 14px; background: white; border: 1.5px solid #e0e0e0;
  border-radius: 10px; text-align: center; cursor: pointer; min-width: 54px;
  font-size: 13px;
}
.date-chip strong { display: block; }
.date-chip.active { background: #667eea; color: white; border-color: #667eea; }
.slot-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.slot {
  padding: 10px; background: white; border: 1.5px solid #e0e0e0;
  border-radius: 8px; text-align: center; font-size: 13px; cursor: pointer;
}
.slot.active { background: #667eea; color: white; border-color: #667eea; font-weight: bold; }
.slot.disabled { background: #f0f0f0; color: #ccc; cursor: not-allowed; }
.confirm-btn {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px; font-size: 16px;
  font-weight: bold; cursor: pointer; margin-top: 8px;
}
.confirm-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>
