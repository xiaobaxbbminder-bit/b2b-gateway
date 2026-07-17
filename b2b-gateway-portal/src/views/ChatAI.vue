<template>
  <div class="container">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <el-icon :size="20"><ChatDotSquare /></el-icon>
          <span>问答助手</span>
          <span class="stream-badge" :class="{ on: streamMode }" @click="streamMode = !streamMode">
            {{ streamMode ? '流式' : '非流式' }}
          </span>
        </div>
      </template>
      <div class="chat-body">
        <div class="chat-messages" ref="messagesRef">
          <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
            <div class="avatar">
              <el-icon v-if="msg.role === 'user'"><UserFilled /></el-icon>
              <el-icon v-else><ChatDotSquare /></el-icon>
            </div>
            <div class="bubble" v-if="msg.role === 'user'">{{ msg.content }}</div>
            <div class="bubble markdown-body" v-else v-html="renderMarkdown(msg.content)"></div>
          </div>
          <div v-if="streaming" class="message assistant">
            <div class="avatar">
              <el-icon><ChatDotSquare /></el-icon>
            </div>
            <div v-if="currentContent" class="bubble markdown-body" v-html="renderMarkdown(currentContent)"></div>
            <div v-else class="bubble typing-indicator">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <div class="input-wrapper">
            <div class="input-container">
              <div class="cmd-panel" v-if="showCmdPanel">
                <div class="cmd-panel-header">📋 可用命令</div>
                <div
                  v-for="(cmd, idx) in filteredCommands"
                  :key="cmd.key"
                  class="cmd-item"
                  :class="{ active: idx === cmdHighlightIdx }"
                  @click="selectCommand(cmd.key)"
                  @mouseenter="cmdHighlightIdx = idx"
                >
                  <span class="cmd-name">{{ cmd.key }}</span>
                  <span class="cmd-desc">{{ cmd.desc }}</span>
                </div>
                <div v-if="filteredCommands.length === 0" class="cmd-empty">无匹配命令</div>
              </div>
              <el-input
                v-model="inputText"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 8 }"
                :disabled="streaming"
                placeholder="发送消息，输入 / 查看命令"
                @keydown="onInputKeydown"
              />
              <el-button
                class="send-btn"
                :class="{ 'is-loading': streaming }"
                :disabled="!inputText.trim() || streaming"
                circle
                @click="sendMessage"
              >
                <el-icon v-if="!streaming"><ArrowUp /></el-icon>
                <span v-else class="custom-loading">
                  <span class="custom-loading-dot"></span>
                  <span class="custom-loading-dot"></span>
                  <span class="custom-loading-dot"></span>
                </span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ChatDotSquare, ArrowUp } from '@element-plus/icons-vue'
import { chatApi } from '../api/chat'
import { renderMarkdown } from '../utils/markdown'

const messages = ref([
  { role: 'assistant', content: '你好！我是 **SFTP RPA** 系统的 AI 助手，有什么可以帮助你的吗？' }
])
const conversationId = ref('')
const inputText = ref('')
const streaming = ref(false)
const streamMode = ref(false)

const COMMANDS = {
  '/rag-reindex': {
    desc: '重新构建知识库索引',
    handler: async () => {
      const loading = ElMessage({ type: 'info', icon: 'Loading', message: '正在执行，请稍候...', duration: 0 })
      try {
        await chatApi.reindexDocs()
        loading.close()
        ElMessage.success('知识库索引重建完成')
      } catch (e) {
        loading.close()
        ElMessage.error('索引重建失败：' + (e.response?.data?.message || e.message))
      }
    }
  },
  '/stream': {
    desc: '切换流式/非流式模式',
    handler: () => {
      streamMode.value = !streamMode.value
      ElMessage.success(streamMode.value ? '已切换为流式模式' : '已切换为非流式模式')
    }
  },
  '/help': {
    desc: '查看所有可用命令',
    handler: async () => {
      const cmds = Object.entries(COMMANDS)
        .filter(([k]) => k !== '/help')
        .map(([k, v]) => `**${k}** — ${v.desc}`)
        .join('\n')
      ElMessageBox.alert(cmds, '可用命令', { dangerouslyUseHTMLString: false, confirmButtonText: '知道了' })
    }
  }
}

const showCmdPanel = ref(false)
const cmdHighlightIdx = ref(0)

const filteredCommands = computed(() => {
  const text = inputText.value
  if (!text.startsWith('/')) return []
  const query = text.slice(1).toLowerCase()
  return Object.entries(COMMANDS)
    .filter(([key]) => query === '' || key.toLowerCase().includes(query))
    .map(([key, val]) => ({ key, desc: val.desc }))
})

watch(inputText, () => {
  if (inputText.value.startsWith('/')) {
    showCmdPanel.value = filteredCommands.value.length > 0
    cmdHighlightIdx.value = 0
  } else {
    showCmdPanel.value = false
  }
})

function selectCommand(cmd) {
  inputText.value = cmd + ' '
  showCmdPanel.value = false
}

function onInputKeydown(e) {
  if (showCmdPanel.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      cmdHighlightIdx.value = Math.min(cmdHighlightIdx.value + 1, filteredCommands.value.length - 1)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      cmdHighlightIdx.value = Math.max(cmdHighlightIdx.value - 1, 0)
    } else if (e.key === 'Enter' || e.key === 'Tab') {
      e.preventDefault()
      const cmd = filteredCommands.value[cmdHighlightIdx.value]
      if (cmd) selectCommand(cmd.key)
    } else if (e.key === 'Escape') {
      showCmdPanel.value = false
    }
  } else {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }
}

async function handleCommand(text) {
  inputText.value = ''
  const cmd = COMMANDS[text]
  if (!cmd) {
    const hints = Object.keys(COMMANDS).join('、')
    ElMessage.warning(`未知命令 ${text}，可用命令：${hints}`)
    return
  }
  cmd.handler()
}

onMounted(() => {
  conversationId.value = crypto.randomUUID()
})
const currentContent = ref('')
const messagesRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return

  if (text.startsWith('/')) {
    handleCommand(text)
    return
  }

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  if (streamMode.value) {
    await sendMessageStream(text)
  } else {
    await sendMessageSync(text)
  }
}

async function sendMessageStream(text) {
  streaming.value = true
  currentContent.value = ''

  try {
    const response = await chatApi.sendMessage(text, conversationId.value)

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      throw new Error(`请求失败 (${response.status})`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop()

      for (const part of parts) {
        const data = []
        for (const line of part.split('\n')) {
          if (line.startsWith('data:')) {
            data.push(line.slice(5))
          }
        }
        if (data.length) {
          currentContent.value += data.join('\n')
        }
      }
      scrollToBottom()
    }

    if (currentContent.value) {
      messages.value.push({ role: 'assistant', content: currentContent.value })
      currentContent.value = ''
    }
  } catch (e) {
    if (currentContent.value) {
      messages.value.push({ role: 'assistant', content: currentContent.value })
      currentContent.value = ''
    } else {
      ElMessage.error('请求失败，请检查网络后重试')
    }
  } finally {
    streaming.value = false
    scrollToBottom()
  }
}

async function sendMessageSync(text) {
  streaming.value = true
  currentContent.value = ''

  try {
    const { data } = await chatApi.sendMessageSync(text, conversationId.value)
    const content = data?.data
    if (content) {
      currentContent.value = content
      await nextTick()
      scrollToBottom()
      messages.value.push({ role: 'assistant', content })
      currentContent.value = ''
    }
  } catch (e) {
    ElMessage.error('请求失败，请检查网络后重试')
  } finally {
    streaming.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card {
  width: 100% !important;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
  min-height: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.stream-badge {
  margin-left: auto;
  font-size: 11px;
  font-weight: 400;
  padding: 2px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.4);
  user-select: none;
}

.stream-badge.on {
  background: rgba(102, 126, 234, 0.2);
  color: #a0b4f0;
}

.chat-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 0;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px 0;
  min-height: 0;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.assistant {
  align-self: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  margin-top: 4px;
}

.message.user .avatar {
  background: rgba(102, 126, 234, 0.3);
  color: #a0b4f0;
}

.message.assistant .avatar {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
  white-space: pre-wrap;
}

.message.user .bubble {
  background: rgba(102, 126, 234, 0.2);
  color: #ffffff;
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-bottom-right-radius: 4px;
}

.message.assistant .bubble {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom-left-radius: 4px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 16px 20px;
  min-height: 24px;
}

.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.chat-input {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 0 24px 20px;
}

.input-wrapper {
  margin-top: 16px;
}

.input-container {
  position: relative;
  display: flex;
  align-items: flex-end;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 10px 48px 10px 18px;
  transition: border-color 0.2s, box-shadow 0.2s;
  width: 100%;
  box-sizing: border-box;
}

.cmd-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: rgba(30, 30, 50, 0.97);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  margin-bottom: 6px;
  max-height: 160px;
  overflow-y: auto;
  z-index: 10;
  backdrop-filter: blur(12px);
}

.cmd-panel-header {
  padding: 8px 14px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.cmd-item {
  padding: 8px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.1s;
}

.cmd-item.active {
  background: rgba(102, 126, 234, 0.25);
}

.cmd-item:hover {
  background: rgba(102, 126, 234, 0.15);
}

.cmd-name {
  font-family: monospace;
  color: #a0b4f0;
  font-size: 13px;
}

.cmd-desc {
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}

.cmd-empty {
  padding: 12px;
  text-align: center;
  color: rgba(255, 255, 255, 0.25);
  font-size: 12px;
}

.input-container:focus-within {
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.1);
}

.input-container :deep(.el-textarea) {
  width: 100%;
}

.input-container :deep(.el-textarea__inner) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 4px 0;
  min-height: 24px;
  max-height: 200px;
  resize: none;
  line-height: 1.5;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85) !important;
}

.input-container :deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.35) !important;
}

.input-container :deep(.el-textarea__inner:focus) {
  border: none !important;
  box-shadow: none !important;
}

.send-btn {
  position: absolute !important;
  bottom: 8px;
  right: 8px;
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  --el-button-bg-color: rgba(255, 255, 255, 0.1);
  --el-button-border-color: transparent;
  --el-button-text-color: rgba(255, 255, 255, 0.4);
  --el-button-hover-bg-color: rgba(255, 255, 255, 0.15);
  --el-button-hover-border-color: transparent;
  --el-button-hover-text-color: rgba(255, 255, 255, 0.7);
}

.send-btn:not(.is-disabled) {
  --el-button-bg-color: rgba(102, 126, 234, 0.6);
  --el-button-border-color: transparent;
  --el-button-text-color: #ffffff;
  --el-button-hover-bg-color: rgba(102, 126, 234, 0.8);
  --el-button-hover-border-color: transparent;
  --el-button-hover-text-color: #ffffff;
  --el-button-active-bg-color: rgba(102, 126, 234, 0.9);
}

.send-btn.is-disabled,
.send-btn.is-loading {
  --el-button-bg-color: rgba(102, 126, 234, 0.6);
  --el-button-border-color: transparent;
  --el-button-text-color: #ffffff;
}

.custom-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 100%;
  height: 100%;
}

.custom-loading-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #ffffff;
  animation: loading-bounce 1.2s ease-in-out infinite;
}

.custom-loading-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.custom-loading-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loading-bounce {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.6);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>

<style>
.markdown-body {
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
}

.markdown-body p {
  margin: 0 0 8px;
}

.markdown-body p:last-child {
  margin-bottom: 0;
}

.markdown-body pre {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px 16px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-body code {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
}

.markdown-body :not(pre) > code {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  color: #a0b4f0;
}

.markdown-body pre code {
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
  padding: 0;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  color: #ffffff;
  margin: 12px 0 8px;
}

.markdown-body h1 { font-size: 1.4em; }
.markdown-body h2 { font-size: 1.2em; }
.markdown-body h3 { font-size: 1.1em; }

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body li {
  margin: 4px 0;
}

.markdown-body a {
  color: #a0b4f0;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
  color: #667eea;
}

.markdown-body blockquote {
  border-left: 3px solid rgba(102, 126, 234, 0.5);
  margin: 8px 0;
  padding: 4px 12px;
  color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.03);
  border-radius: 0 4px 4px 0;
}

.markdown-body table {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid rgba(255, 255, 255, 0.15);
  padding: 6px 12px;
  text-align: left;
}

.markdown-body th {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.markdown-body td {
  color: rgba(255, 255, 255, 0.75);
}

.markdown-body hr {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin: 12px 0;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 8px;
}

.markdown-body pre .hljs-keyword,
.markdown-body pre .hljs-selector-tag,
.markdown-body pre .hljs-built_in {
  color: #569cd6;
}

.markdown-body pre .hljs-string,
.markdown-body pre .hljs-attr {
  color: #ce9178;
}

.markdown-body pre .hljs-number,
.markdown-body pre .hljs-literal {
  color: #b5cea8;
}

.markdown-body pre .hljs-comment {
  color: #6a9955;
  font-style: italic;
}

.markdown-body pre .hljs-function,
.markdown-body pre .hljs-title {
  color: #dcdcaa;
}

.markdown-body pre .hljs-variable,
.markdown-body pre .hljs-name {
  color: #9cdcfe;
}
</style>
