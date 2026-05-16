<template>
  <div>
    <h3 class="page-title">用户管理</h3>
    <table class="table" v-if="users.length">
      <thead><tr><th>学号</th><th>姓名</th><th>违规次数</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.studentId }}</td><td>{{ u.name }}</td>
          <td>{{ u.violationCount }}</td>
          <td><span :class="u.isRestricted ? 'badge-red' : 'badge-green'">{{ u.isRestricted ? '已限制' : '正常' }}</span></td>
          <td>
            <button :class="['link-btn', u.isRestricted ? 'text-green' : 'text-red']" @click="toggleBan(u)">{{ u.isRestricted ? '解封' : '封禁' }}</button>
            <button class="link-btn" @click="resetViolation(u.id)">重置违规</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'UserManage',
  data() { return { users: [] } },
  mounted() { this.load() },
  methods: {
    async load() { const r = await fetch('/api/admin/users', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.users = j.data },
    async toggleBan(u) {
      if (!confirm(`确定${u.isRestricted ? '解封' : '封禁'}用户 ${u.name} 吗？`)) return
      const r = await fetch(`/api/admin/users/${u.id}/ban?banned=${!u.isRestricted}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    },
    async resetViolation(id) {
      if (!confirm('确定重置违规次数吗？')) return
      const r = await fetch(`/api/admin/users/${id}/violation-reset`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page-title { font-size: 18px; color: #333; margin-bottom: 20px; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge-green { background: #e6f7ee; color: #52c41a; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge-red { background: #fff2f0; color: #ff4d4f; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
</style>
