<template>
  <div>
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题"
        clearable
        style="width: 220px"
        @keyup.enter="reload"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="reload">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
      </el-select>
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px" @change="reload">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button @click="reload">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="$router.push('/admin/articles/new')">
        <el-icon><Plus /></el-icon>&nbsp;写文章
      </el-button>
    </div>

    <el-table :data="rows" v-loading="loading" class="admin-table" stripe>
      <el-table-column label="标题" min-width="260">
        <template #default="{ row }">
          <div class="title-cell">
            <el-tag v-if="row.isTop" size="small" type="danger" effect="plain">置顶</el-tag>
            <span>{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="浏览" width="90" />
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ formatDate(row.publishTime || row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/admin/articles/${row.id}/edit`)">编辑</el-button>
          <el-button link type="primary" @click="toggle(row)">
            {{ row.status === 1 ? '撤回' : '发布' }}
          </el-button>
          <el-popconfirm title="确认删除这篇文章？" width="220" @confirm="remove(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminChangeStatus,
  adminDeleteArticle,
  adminFetchArticles
} from '@/api/article'
import { adminFetchCategories } from '@/api/taxonomy'
import { formatDate } from '@/utils/format'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined,
  categoryId: undefined
})

async function load() {
  loading.value = true
  try {
    const data = await adminFetchArticles({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      status: query.status,
      categoryId: query.categoryId
    })
    rows.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  load()
}

function onPage(p) {
  query.page = p
  load()
}

async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  await adminChangeStatus(row.id, next)
  ElMessage.success(next === 1 ? '已发布' : '已撤回为草稿')
  load()
}

async function remove(row) {
  await adminDeleteArticle(row.id)
  ElMessage.success('已删除')
  if (rows.value.length === 1 && query.page > 1) query.page -= 1
  load()
}

onMounted(async () => {
  load()
  categories.value = (await adminFetchCategories()) || []
})
</script>