<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">学生端登录</h2>
      <div class="form-group">
        <label for="studentId">学号</label>
        <input type="text" id="studentId" v-model="studentId" placeholder="请输入学号" />
      </div>
      <div class="form-group">
        <label for="password">姓名</label>
        <input type="text" id="password" v-model="password" placeholder="请输入姓名" />
      </div>
      <button class="login-button" @click="login">登录</button>
      <div class="register-link">
        <span>还没有账号？</span>
        <a href="#" @click.prevent="showRegister = true">立即注册</a>
      </div>
    </div>
    
    <!-- 注册弹窗 -->
    <div v-if="showRegister" class="modal">
      <div class="modal-content">
        <h3>学生注册</h3>
        <div class="form-group">
          <label for="registerStudentId">学号</label>
          <input type="text" id="registerStudentId" v-model="registerStudentId" placeholder="请输入学号" />
        </div>
        <div class="form-group">
          <label for="registerName">姓名</label>
          <input type="text" id="registerName" v-model="registerName" placeholder="请输入姓名" />
        </div>
        <div class="modal-buttons">
          <button @click="register">注册</button>
          <button @click="showRegister = false">取消</button>
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
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
          studentId: this.studentId,
          password: this.password
        })
      })
      const result = await response.json()
      if (result.code === 200) {
        localStorage.setItem('user', JSON.stringify(result.data))
        this.$router.push('/home')
      } else {
        alert(result.message)
      }
    },
    async register() {
      const response = await fetch('/api/student/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
          studentId: this.registerStudentId,
          name: this.registerName
        })
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
.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
}

.login-button {
  width: 100%;
  padding: 12px;
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  margin-bottom: 20px;
}

.login-button:hover {
  background: #3a56d7;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #4a6cf7;
  text-decoration: none;
  margin-left: 5px;
}

.register-link a:hover {
  text-decoration: underline;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 10px;
  padding: 30px;
  width: 90%;
  max-width: 400px;
}

.modal-content h3 {
  margin-bottom: 20px;
  color: #333;
  text-align: center;
}

.modal-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.modal-buttons button {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
}

.modal-buttons button:first-child {
  background: #4a6cf7;
  color: white;
}

.modal-buttons button:last-child {
  background: #ddd;
  color: #333;
}
</style>