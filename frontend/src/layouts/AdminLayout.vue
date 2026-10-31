<template>
  <el-container class="admin">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">{{ site.config.siteName }} · 后台</div>
      <el-menu :default-active="activeMenu" router class="admin-menu">
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon><span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/docs">
          <el-icon><Notebook /></el-icon><span>知识库</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon><span>分类</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon><span>标签</span>
        </el-menu-item>
        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon><span>站点设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="spacer" />
        <el-button text @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon>&nbsp;前台
        </el-button>
        <el-dropdown @command="onCommand">
          <span class="user-chip">
            {{ user.user?.nickname || user.user?.username || 'admin' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px" @closed="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="86px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password
                    autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password
                    autocomplete="new-password" placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password
                    autocomplete="new-password" @keyup.enter="submitPwd" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { useSiteStore } from '@/stores/site'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const site = useSiteStore()

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/articles')) return '/admin/articles'
  return route.path
})

/* ---------- 修改密码 ---------- */
const pwdVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需在 6-64 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

function resetPwdForm() {
  pwdFormRef.value?.clearValidate()
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
}

async function submitPwd() {
  await pwdFormRef.value.validate()
  pwdSaving.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码已修改，请用新密码重新登录')
    pwdVisible.value = false
    user.logout()
    router.replace('/login')
  } finally {
    pwdSaving.value = false
  }
}

/* ---------- 下拉菜单 ---------- */
function onCommand(cmd) {
  if (cmd === 'password') {
    pwdVisible.value = true
  } else if (cmd === 'logout') {
    user.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>