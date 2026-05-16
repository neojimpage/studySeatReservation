<template>
  <div>
    <h3 class="page-title">仪表盘</h3>
    <div class="stats">
      <div class="stat-card green"><div class="num">{{ stats.totalSeats }}</div><div class="desc">总座位数</div></div>
      <div class="stat-card blue"><div class="num">{{ stats.availableSeats }}</div><div class="desc">空闲座位</div></div>
      <div class="stat-card orange"><div class="num">{{ stats.todayReservations }}</div><div class="desc">今日预约</div></div>
      <div class="stat-card red"><div class="num">{{ stats.violationUsers }}</div><div class="desc">违规用户</div></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() { return { stats: {} } },
  mounted() { this.load() },
  methods: {
    async load() {
      const r = await fetch('/api/admin/dashboard', { headers: { 'X-User-Role': 'admin' } })
      const j = await r.json()
      if (j.code === 200) this.stats = j.data
    }
  }
}
</script>

<style scoped>
.page-title { font-size: 20px; color: #333; margin-bottom: 24px; }
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card { border-radius: 12px; padding: 20px; text-align: center; }
.stat-card.green { background: #e6f7ee; }
.stat-card.blue { background: #e6f7ff; }
.stat-card.orange { background: #fff1e6; }
.stat-card.red { background: #fff2f0; }
.num { font-size: 30px; font-weight: bold; }
.stat-card.green .num { color: #52c41a; }
.stat-card.blue .num { color: #1890ff; }
.stat-card.orange .num { color: #fa8c16; }
.stat-card.red .num { color: #ff4d4f; }
.desc { font-size: 13px; color: #666; margin-top: 4px; }
</style>
