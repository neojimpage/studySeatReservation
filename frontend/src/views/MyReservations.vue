<template>
  <div class="page">
    <div class="tab-bar">
      <div v-for="t in tabs" :key="t.key"
           :class="['tab', { active: activeTab === t.key }]"
           @click="activeTab = t.key">{{ t.label }}</div>
    </div>

    <div v-if="activeTab === 'current'" class="content">
      <div v-if="currentReservations.length === 0" class="empty">暂无当前预约</div>
      <div v-for="r in currentReservations" :key="r.id" class="card">
        <div class="card-left">
          <span class="seat-name">{{ seats[r.seatId]?.seatNumber || '未知' }}</span>
          <span :class="['badge', badgeClass(r.status)]">{{ r.status }}</span>
          <span class="time">📅 {{ fmt(r.startTime) }} - {{ fmt(r.endTime) }}</span>
        </div>
        <div class="card-right">
          <button v-if="r.status === '已预约'" class="btn btn-red" @click="cancelReservation(r.id)">取消</button>
          <button v-if="r.status === '已开始'" class="btn btn-blue" @click="leave(r.id)">暂离</button>
          <button v-if="r.status === '暂离'" class="btn btn-green" @click="back(r.id)">返回</button>
          <button v-if="r.status === '已开始' || r.status === '暂离'" class="btn btn-orange" @click="finish(r.id)">结束</button>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'history'" class="content">
      <div v-if="historyReservations.length === 0" class="empty">暂无历史预约</div>
      <div v-for="r in historyReservations" :key="r.id" class="card">
        <div class="card-left">
          <span class="seat-name">{{ seats[r.seatId]?.seatNumber || '未知' }}</span>
          <span :class="['badge', badgeClass(r.status)]">{{ r.status }}</span>
          <span class="time">📅 {{ fmt(r.startTime) }} - {{ fmt(r.endTime) }}</span>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'profile'" class="content">
      <div class="profile-card">
        <div class="info-row"><span>学号</span><span>{{ user?.studentId }}</span></div>
        <div class="info-row"><span>姓名</span><span>{{ user?.name }}</span></div>
        <div class="info-row"><span>违规次数</span><span :class="user?.violationCount > 0 ? 'text-red' : 'text-green'">{{ user?.violationCount || 0 }}</span></div>
        <div class="info-row"><span>账号状态</span><span :class="user?.isRestricted ? 'text-red' : 'text-green'">{{ user?.isRestricted ? '已限制' : '正常' }}</span></div>
      </div>
      <button class="logout-btn" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyReservations',
  data() {
    return {
      activeTab: 'current',
      tabs: [{ key:'current', label:'当前预约' }, { key:'history', label:'历史预约' }, { key:'profile', label:'个人中心' }],
      currentReservations: [], historyReservations: [], user: null, seats: {}
    }
  },
  mounted() { this.user = JSON.parse(localStorage.getItem('user')); if (this.user) { this.loadCurrent(); this.loadHistory() } },
  methods: {
    async loadCurrent() {
      const r = await fetch(`/api/student/reservations/current?userId=${this.user.id}`)
      const j = await r.json()
      if (j.code === 200) { this.currentReservations = j.data; j.data.forEach(res => this.getSeat(res.seatId)) }
    },
    async loadHistory() {
      const r = await fetch(`/api/student/reservations?userId=${this.user.id}`)
      const j = await r.json()
      if (j.code === 200) { this.historyReservations = j.data.filter(res => ['已结束','已取消','爽约'].includes(res.status)); j.data.forEach(res => this.getSeat(res.seatId)) }
    },
    async getSeat(id) {
      if (!this.seats[id]) { const r = await fetch(`/api/student/seats/${id}`); const j = await r.json(); if (j.code === 200) this.seats[id] = j.data }
    },
    badgeClass(s) { const m = { '已预约':'badge-orange', '已开始':'badge-green', '暂离':'badge-blue', '已结束':'badge-gray', '已取消':'badge-red', '爽约':'badge-red' }; return m[s] || 'badge-gray' },
    fmt(d) { return new Date(d).toLocaleString('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }) },
    async cancelReservation(id) { await this.action(() => fetch(`/api/student/reservations/${id}/cancel`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '取消成功') },
    async leave(id) { await this.action(() => fetch(`/api/student/reservations/${id}/leave`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '暂离成功') },
    async back(id) { await this.action(() => fetch(`/api/student/reservations/${id}/back`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '返回成功') },
    async finish(id) { await this.action(() => fetch(`/api/student/reservations/${id}/finish`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '结束成功') },
    async action(fn, ok) { const r = await fn(); const j = await r.json(); if (j.code === 200) { alert(ok); this.loadCurrent(); this.loadHistory() } else alert(j.message) },
    logout() { localStorage.removeItem('user'); this.$router.push('/') }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6fa; }
.tab-bar { display: flex; background: white; border-bottom: 1px solid #eee; }
.tab { flex: 1; text-align: center; padding: 14px; font-size: 14px; color: #999; cursor: pointer; border-bottom: 2px solid transparent; }
.tab.active { color: #667eea; border-bottom-color: #667eea; font-weight: bold; }
.content { padding: 16px; }
.empty { text-align: center; padding: 80px 0; color: #999; font-size: 16px; }
.card { background: white; border-radius: 12px; padding: 16px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: flex-start; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.card-left { display: flex; flex-direction: column; gap: 4px; }
.seat-name { font-weight: bold; font-size: 16px; color: #333; }
.time { font-size: 13px; color: #666; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: bold; width: fit-content; }
.badge-green { background: #e6f7ee; color: #52c41a; }
.badge-orange { background: #fff1e6; color: #fa8c16; }
.badge-blue { background: #e6f7ff; color: #1890ff; }
.badge-gray { background: #f0f0f0; color: #999; }
.badge-red { background: #fff2f0; color: #ff4d4f; }
.card-right { display: flex; flex-direction: column; gap: 6px; }
.btn { padding: 5px 12px; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; color: white; }
.btn-red { background: #ff4d4f; }
.btn-blue { background: #1890ff; }
.btn-green { background: #52c41a; }
.btn-orange { background: #fa8c16; }
.profile-card { background: white; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.info-row { display: flex; justify-content: space-between; padding: 10px 0; font-size: 14px; border-bottom: 1px solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.info-row span:first-child { color: #999; }
.info-row span:last-child { color: #333; font-weight: bold; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.logout-btn { width: 100%; padding: 14px; background: #ff4d4f; color: white; border: none; border-radius: 10px; font-size: 16px; font-weight: bold; cursor: pointer; }
</style>
