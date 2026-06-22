<template>
  <div>
    <div class="page-header">
      <div><h3>座位管理</h3><select v-model="filterArea" @change="load" class="area-filter"><option value="">全部区域</option><option v-for="a in areas" :key="a.id" :value="a.id">{{ a.name }}</option></select></div>
      <button class="add-btn" @click="openModal(null)">+ 新增座位</button>
    </div>
    <table class="table" v-if="seats.length">
      <thead><tr><th>座位号</th><th>区域</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="s in seats" :key="s.id">
          <td>{{ s.seatNumber }}</td><td>{{ areaName(s.areaId) }}</td>
          <td><span :class="['badge', statusBadge(s.status)]">{{ s.status }}</span></td>
          <td>
            <button class="link-btn" @click="openModal(s)">编辑</button>
            <button v-if="s.status === '占用' || s.status === '暂离'" class="link-btn text-orange" @click="release(s.id)">释放</button>
            <button :class="['link-btn', s.isEnabled ? 'text-red' : 'text-green']" @click="toggleSeat(s)">{{ s.isEnabled ? '禁用' : '启用' }}</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h4>{{ editId ? '编辑' : '新增' }}座位</h4>
        <div class="form-group"><label>区域</label><select v-model="form.areaId"><option v-for="a in areas" :key="a.id" :value="a.id">{{ a.name }}</option></select></div>
        <div class="form-group"><label>座位号</label><input v-model="form.seatNumber" /></div>
        <div class="modal-btns"><button class="btn-primary" @click="save">保存</button><button class="btn-cancel" @click="showModal = false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SeatManage',
  data() { return { seats: [], areas: [], filterArea: '', showModal: false, editId: null, form: { areaId: '', seatNumber: '' } } },
  mounted() { this.loadAreas(); this.load() },
  methods: {
    async loadAreas() { const r = await fetch('/api/admin/areas', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.areas = j.data },
    async load() { const q = this.filterArea ? `?areaId=${this.filterArea}` : ''; const r = await fetch(`/api/admin/seats${q}`, { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.seats = j.data },
    areaName(id) { const a = this.areas.find(x => x.id === id); return a ? a.name : '--' },
    statusBadge(s) { const m = { '空闲':'badge-green', '已预约':'badge-orange', '占用':'badge-gray', '暂离':'badge-blue', '禁用':'badge-red' }; return m[s] || 'badge-gray' },
    openModal(s) { if (s) { this.editId = s.id; this.form = { areaId: s.areaId, seatNumber: s.seatNumber } } else { this.editId = null; this.form = { areaId: '', seatNumber: '' } } this.showModal = true },
    async save() {
      const h = { 'Content-Type': 'application/x-www-form-urlencoded', 'X-User-Role': 'admin' }
      const body = new URLSearchParams({ areaId: this.form.areaId, seatNumber: this.form.seatNumber })
      const r = this.editId
        ? await fetch(`/api/admin/seats/${this.editId}`, { method: 'PUT', headers: h, body })
        : await fetch('/api/admin/seats', { method: 'POST', headers: h, body })
      const j = await r.json(); if (j.code === 200) { alert('保存成功'); this.showModal = false; this.load() } else alert(j.message)
    },
    async release(id) { const r = await fetch(`/api/admin/seats/${id}/release`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message) },
    async toggleSeat(s) { const r = await fetch(`/api/admin/seats/${s.id}/disable?disabled=${s.isEnabled}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message) }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 18px; color: #333; margin: 0 0 8px 0; }
.area-filter { margin-top: 6px; padding: 6px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.add-btn { padding: 8px 18px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 13px; height: fit-content; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: bold; }
.badge-green { background: #e6f7ee; color: #52c41a; }
.badge-orange { background: #fff1e6; color: #fa8c16; }
.badge-blue { background: #e6f7ff; color: #1890ff; }
.badge-gray { background: #f0f0f0; color: #999; }
.badge-red { background: #fff2f0; color: #ff4d4f; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.text-orange { color: #fa8c16; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 14px; padding: 28px; width: 380px; }
.modal-card h4 { margin-bottom: 20px; color: #333; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.form-group input, .form-group select { width: 100%; padding: 9px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.modal-btns { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary { flex: 1; padding: 10px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; }
.btn-cancel { flex: 1; padding: 10px; background: #f0f0f0; border: none; border-radius: 8px; cursor: pointer; }
</style>
