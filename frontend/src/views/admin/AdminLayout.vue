<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="sidebar-title">自习座位管理</div>
      <router-link v-for="item in nav" :key="item.path"
                   :to="item.path" class="nav-item"
                   :class="{ active: $route.path.startsWith(item.path) }">
        {{ item.label }}
      </router-link>
      <div class="sidebar-bottom">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>
    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      nav: [
        { label: '仪表盘', path: '/admin/dashboard' },
        { label: '区域管理', path: '/admin/areas' },
        { label: '座位管理', path: '/admin/seats' },
        { label: '用户管理', path: '/admin/users' },
      ]
    }
  },
  methods: {
    logout() { localStorage.removeItem('user'); this.$router.push('/') }
  }
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar { width: 200px; background: #1a1a2e; color: white; display: flex; flex-direction: column; padding-top: 20px; flex-shrink: 0; }
.sidebar-title { padding: 0 20px 20px; border-bottom: 1px solid #333; margin-bottom: 12px; font-size: 16px; font-weight: bold; color: #667eea; }
.nav-item { display: block; padding: 12px 20px; color: #aaa; text-decoration: none; font-size: 14px; margin: 2px 10px; border-radius: 6px; }
.nav-item:hover { color: white; }
.nav-item.active { background: #667eea; color: white; font-weight: bold; }
.sidebar-bottom { margin-top: auto; padding: 20px; }
.logout-btn { width: 100%; padding: 8px; background: #ff4d4f; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; }
.main { flex: 1; background: #f5f6fa; padding: 24px; }
</style>
