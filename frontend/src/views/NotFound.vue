<template>
  <div class="not-found">
    <div>
      <h1>404</h1>
      <p class="muted">页面不存在，可能是链接过期了。</p>
      <el-button type="primary" @click="$router.push('/')">回到首页</el-button>
    </div>
  </div>
</template>