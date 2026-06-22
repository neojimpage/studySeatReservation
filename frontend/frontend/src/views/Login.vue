<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo-area">
        <div class="logo-icon">📚</div>
        <h2>自习座位预约</h2>
        <p>校园智能自习管理</p>
      </div>
      <div class="form-group">
        <label>学号</label>
        <input type="text" v-model="studentId" placeholder="请输入学号" />
      </div>
      <div class="form-group">
        <label>姓名</label>
        <input type="text" v-model="password" placeholder="请输入姓名" />
      </div>
      <button class="login-btn" @click="login">登 录</button>
      <p class="register-link">还没有账号？<a href="#" @click.prevent="showRegister = true">立即注册</a></p>
    </div>

    <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
      <div class="modal-card">
        <h3>学生注册</h3>
        <div class="form-group">
          <label>学号</label>
          <input type="text" v-model="registerStudentId" placeholder="请输入学号" />
        </div>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" v-model="registerName" placeholder="请输入姓名" />
        </div>
        <div class="modal-buttons">
          <button class="btn-primary" @click="register">注册</button>
          <button class="btn-cancel" @click="showRegister = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      studentId: '',
      password: '',
      showRegister: false,
      registerStudentId: '',
      registerName: ''
    }
  },
  methods: {
    async login() {
      const response = await fetch('/api/student/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ studentId: this.studentId, password: this.password })
      })
      const result = await response.json()
      if (result.code === 200) {
        localStorage.setItem('user', JSON.stringify(result.data))
        if (result.data.role === 'admin') {
          this.$router.push('/admin/dashboard')
        } else {
          this.$router.push('/home')
        }
      } else {
        alert(result.message)
      }
    },
    async register() {
      const response = await fetch('/api/student/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ studentId: this.registerStudentId, name: this.registerName })
      })
      const result = await response.json()
      if (result.code === 200) {
        alert('注册成功，初始密码为学号后6位')
        this.showRegister = false
      } else {
        alert(result.message)
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-card {
  background: white;
  border-radius: 16px;
  padding: 36px 28px;
  width: 360px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.logo-area {
  text-align: center;
  margin-bottom: 28px;
}
.logo-icon {
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px;
  margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px;
}
.logo-area h2 { color: #333; font-size: 18px; margin: 0; }
.logo-area p { color: #999; font-size: 13px; margin: 4px 0 0; }
.form-group { margin-bottom: 18px; }
.form-group label { font-size: 13px; color: #666; display: block; margin-bottom: 6px; }
.form-group input {
  width: 100%; padding: 10px 14px;
  border: 1.5px solid #e0e0e0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color 0.3s;
}
.form-group input:focus { border-color: #667eea; }
.login-btn {
  width: 100%; padding: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px;
  font-size: 16px; font-weight: bold; cursor: pointer; margin-bottom: 16px;
}
.register-link { text-align: center; font-size: 13px; color: #999; }
.register-link a { color: #667eea; text-decoration: none; }
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-card {
  background: white; border-radius: 16px; padding: 30px; width: 360px;
}
.modal-card h3 { margin-bottom: 20px; text-align: center; color: #333; }
.modal-buttons { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary {
  flex: 1; padding: 10px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 8px; cursor: pointer;
}
.btn-cancel {
  flex: 1; padding: 10px; background: #f0f0f0;
  color: #333; border: none; border-radius: 8px; cursor: pointer;
}
</style>
