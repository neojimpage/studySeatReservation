<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <div class="ai-avatar">🤖</div>
      <div class="ai-info"><span class="ai-name">自习助手</span><span class="ai-status">在线 · DeepSeek</span></div>
    </div>

    <div class="messages" ref="msgContainer">
      <div v-for="(m, i) in messages" :key="i"
           :class="['msg-row', m.role === 'user' ? 'row-user' : 'row-bot']">
        <div v-if="m.role === 'bot'" class="bot-avatar">🤖</div>
        <div :class="['bubble', m.role === 'user' ? 'bubble-user' : 'bubble-bot']" v-html="formatMsg(m.content)"></div>
      </div>
      <div v-if="loading" class="msg-row row-bot">
        <div class="bot-avatar">🤖</div>
        <div class="bubble bubble-bot typing">对方正在输入...</div>
      </div>
    </div>

    <div class="input-area">
      <input v-model="input" placeholder="输入消息..." @keyup.enter="send" />
      <button class="send-btn" @click="send" :disabled="!input.trim() || loading">→</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatPage',
  data() {
    // Restore conversationId from localStorage so history survives refresh
    let cid = localStorage.getItem('chatConversationId')
    if (!cid) {
      cid = crypto.randomUUID()
      localStorage.setItem('chatConversationId', cid)
    }
    // Restore messages from localStorage if available
    let saved = null
    try { saved = JSON.parse(localStorage.getItem('chatMessages')) } catch(e) {}
    const defaultMsg = [{ role: 'bot', content: '你好！我是校园自习助手 🎓\n\n我可以帮你：\n• 预约自习座位\n• 查询可用座位\n• 规划学习计划\n• 管理你的预约\n\n请问有什么需要？' }]
    return {
      messages: (saved && saved.length > 0) ? saved : defaultMsg,
      input: '',
      loading: false,
      conversationId: cid,
    }
  },
  methods: {
    async send() {
      if (!this.input.trim() || this.loading) return
      const msg = this.input.trim()
      this.input = ''
      this.messages.push({ role: 'user', content: msg })
      this.loading = true
      this.$nextTick(() => this.scrollBottom())

      try {
        const user = JSON.parse(localStorage.getItem('user'))
        const r = await fetch(`/api/student/chat?userId=${user.id}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message: msg, conversationId: this.conversationId })
        })
        const j = await r.json()
        if (j.code === 200) {
          this.messages.push({ role: 'bot', content: j.data.reply })
        } else {
          this.messages.push({ role: 'bot', content: '抱歉，' + j.message })
        }
      } catch (e) {
        this.messages.push({ role: 'bot', content: '网络错误，请稍后重试' })
      } finally {
        this.loading = false
        localStorage.setItem('chatMessages', JSON.stringify(this.messages))
        this.$nextTick(() => this.scrollBottom())
      }
    },
    formatMsg(t) {
      return t.replace(/\n/g, '<br>')
    },
    scrollBottom() {
      const el = this.$refs.msgContainer
      if (el) el.scrollTop = el.scrollHeight
    }
  }
}
</script>

<style scoped>
.chat-page { height: 100vh; display: flex; flex-direction: column; background: #f5f6fa; }
.chat-header {
  background: linear-gradient(135deg, #667eea, #764ba2); color: white;
  padding: 14px 16px; display: flex; align-items: center; gap: 10px;
  flex-shrink: 0;
}
.back-btn { background: none; border: none; color: white; font-size: 18px; cursor: pointer; }
.ai-avatar { width: 36px; height: 36px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.ai-name { font-weight: bold; font-size: 16px; display: block; }
.ai-status { font-size: 12px; opacity: 0.8; }
.messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.msg-row { display: flex; gap: 8px; align-items: flex-start; max-width: 85%; }
.row-user { align-self: flex-end; }
.row-bot { align-self: flex-start; }
.bot-avatar { width: 32px; height: 32px; background: #667eea; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 14px; flex-shrink: 0; }
.bubble { padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.6; }
.bubble-bot { background: white; color: #333; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.bubble-user { background: #667eea; color: white; border-bottom-right-radius: 4px; }
.typing { color: #999; font-style: italic; }
.input-area { padding: 12px 16px; background: white; border-top: 1px solid #eee; display: flex; gap: 10px; flex-shrink: 0; }
.input-area input { flex: 1; padding: 10px 14px; border: 1.5px solid #e0e0e0; border-radius: 20px; font-size: 14px; outline: none; }
.input-area input:focus { border-color: #667eea; }
.send-btn { width: 40px; height: 40px; background: #667eea; color: white; border: none; border-radius: 50%; cursor: pointer; font-size: 18px; }
.send-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>
