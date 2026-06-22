<template>
  <div>
    <div class="page-header">
      <h3>区域管理</h3>
      <button class="add-btn" @click="openModal(null)">+ 新增区域</button>
    </div>
    <table class="table" v-if="areas.length">
      <thead><tr><th>ID</th><th>名称</th><th>楼层</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="a in areas" :key="a.id">
          <td>{{ a.id }}</td><td>{{ a.name }}</td><td>{{ a.floor }}F</td>
          <td><span :class="a.isEnabled ? 'badge-green' : 'badge-red'">{{ a.isEnabled ? '启用' : '禁用' }}</span></td>
          <td>
            <button class="link-btn" @click="openModal(a)">编辑</button>
            <button :class="['link-btn', a.isEnabled ? 'text-red' : 'text-green']" @click="toggleArea(a)">{{ a.isEnabled ? '禁用' : '启用' }}</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h4>{{ editId ? '编辑' : '新增' }}区域</h4>
        <div class="form-group"><label>名称</label><input v-model="form.name" /></div>
        <div class="form-group"><label>楼层</label><input v-model="form.floor" type="number" /></div>
        <div class="modal-btns">
          <button class="btn-primary" @click="save">保存</button>
          <button class="btn-cancel" @click="showModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AreaManage',
  data() { return { areas: [], showModal: false, editId: null, form: { name: '', floor: 1 } } },
  mounted() { this.load() },
  methods: {
    async load() { const r = await fetch('/api/admin/areas', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.areas = j.data },
    openModal(a) { if (a) { this.editId = a.id; this.form = { name: a.name, floor: a.floor } } else { this.editId = null; this.form = { name: '', floor: 1 } } this.showModal = true },
    async save() {
      const h = { 'Content-Type': 'application/x-www-form-urlencoded', 'X-User-Role': 'admin' }
      const body = new URLSearchParams({ name: this.form.name, floor: this.form.floor })
      const r = this.editId
        ? await fetch(`/api/admin/areas/${this.editId}`, { method: 'PUT', headers: h, body })
        : await fetch('/api/admin/areas', { method: 'POST', headers: h, body })
      const j = await r.json()
      if (j.code === 200) { alert('保存成功'); this.showModal = false; this.load() } else alert(j.message)
    },
    async toggleArea(a) {
      const r = await fetch(`/api/admin/areas/${a.id}/disable?disabled=${a.isEnabled}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 18px; color: #333; margin: 0; }
.add-btn { padding: 8px 18px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 13px; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge-green { background: #e6f7ee; color: #52c41a; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge-red { background: #fff2f0; color: #ff4d4f; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 14px; padding: 28px; width: 380px; }
.modal-card h4 { margin-bottom: 20px; color: #333; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.form-group input { width: 100%; padding: 9px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.modal-btns { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary { flex: 1; padding: 10px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; }
.btn-cancel { flex: 1; padding: 10px; background: #f0f0f0; border: none; border-radius: 8px; cursor: pointer; }
</style>
