<template>
  <div class="work-assignment-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">排班管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增排班
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="班次">
          <el-select 
            v-model="searchForm.workId" 
            placeholder="请选择班次" 
            clearable 
            filterable
            style="width: 200px"
            class="rounded-lg"
          >
            <el-option 
              v-for="work in workList" 
              :key="work.id" 
              :label="work.workName" 
              :value="work.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="候选人">
          <el-select 
            v-model="searchForm.userId" 
            placeholder="请选择候选人" 
            clearable 
            filterable
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.nickname || user.username" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班状态">
          <el-select 
            v-model="searchForm.assignStatus" 
            placeholder="请选择排班状态" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="已排班" value="assigned" />
            <el-option label="已到岗" value="checked_in" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button 
            @click="handleReset"
            class="rounded-lg"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="workName" label="班次名称" min-width="150" />
        <el-table-column prop="userName" label="候选人" width="120" />
        <el-table-column prop="assignStatus" label="排班状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getAssignStatusType(row.assignStatus)"
              class="rounded-full px-3 py-1"
            >
              {{ getAssignStatusText(row.assignStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certCheckPassed" label="证件检查" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.certCheckPassed === 1 ? 'success' : 'danger'"
              class="rounded-full px-3 py-1"
            >
              {{ row.certCheckPassed === 1 ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证件警告" min-width="200">
          <template #default="{ row }">
            <div v-if="row.certWarnings" class="text-orange-600 text-sm">
              <el-icon class="mr-1"><Warning /></el-icon>
              {{ row.certWarnings }}
            </div>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="证件检查详情" min-width="200">
          <template #default="{ row }">
            <el-button 
              type="info" 
              size="small" 
              link 
              @click="handleViewCertDetail(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.assignStatus === 'assigned'"
              type="success" 
              size="small" 
              link 
              @click="handleUpdateStatus(row, 'checked_in')"
              class="hover:text-green-600 transition-colors duration-200"
            >
              签到
            </el-button>
            <el-button 
              v-if="row.assignStatus === 'checked_in'"
              type="primary" 
              size="small" 
              link 
              @click="handleUpdateStatus(row, 'completed')"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              完成
            </el-button>
            <el-button 
              v-if="row.assignStatus !== 'cancelled' && row.assignStatus !== 'completed'"
              type="danger" 
              size="small" 
              link 
              @click="handleDelete(row)"
              class="hover:text-red-600 transition-colors duration-200"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="rounded-lg"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增排班对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="新增排班" 
      width="600px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef"
        label-width="100px"
        class="px-2"
      >
        <el-form-item label="选择班次" prop="workId">
          <el-select 
            v-model="formData.workId" 
            placeholder="请选择班次" 
            filterable
            class="w-full rounded-lg"
            @change="handleWorkChange"
          >
            <el-option 
              v-for="work in workList" 
              :key="work.id" 
              :label="work.workName" 
              :value="work.id" 
            >
              <span>{{ work.workName }}</span>
              <span v-if="work.requiredCerts" class="text-xs text-gray-500 ml-2">
                (要求: {{ formatRequiredCerts(work.requiredCerts) }})
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择候选人" prop="userId">
          <el-select 
            v-model="formData.userId" 
            placeholder="请选择候选人" 
            filterable
            class="w-full rounded-lg"
            @change="handleUserChange"
          >
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.nickname || user.username" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>

        <!-- 证件检查预览 -->
        <div v-if="certCheckResult" class="mb-4 p-4 bg-gray-50 rounded-lg">
          <h4 class="font-medium mb-3 flex items-center">
            <el-icon class="mr-2"><Document /></el-icon>
            证件检查预览
          </h4>
          <div 
            v-for="item in certCheckResult.detail" 
            :key="item.certType"
            class="flex items-center justify-between py-2 border-b border-gray-100 last:border-0"
          >
            <span class="text-sm">{{ item.certTypeName }}</span>
            <div class="flex items-center gap-2">
              <el-tag 
                :type="getCheckResultType(item.status)"
                size="small"
                class="rounded-full"
              >
                {{ getCheckResultText(item.status) }}
              </el-tag>
              <span class="text-xs text-gray-500">{{ item.message }}</span>
            </div>
          </div>
          <div v-if="certCheckResult.warnings && certCheckResult.warnings.length > 0" class="mt-3 p-3 bg-orange-50 rounded-lg">
            <div class="text-sm text-orange-600 font-medium mb-1 flex items-center">
              <el-icon class="mr-1"><Warning /></el-icon>
              证件警告
            </div>
            <div 
              v-for="(warning, idx) in certCheckResult.warnings" 
              :key="idx"
              class="text-xs text-orange-600"
            >
              {{ warning }}
            </div>
          </div>
          <div v-if="!certCheckResult.passed" class="mt-3 p-3 bg-red-50 rounded-lg">
            <div class="text-sm text-red-600 font-medium flex items-center">
              <el-icon class="mr-1"><CircleClose /></el-icon>
              证件检查未通过，无法排班
            </div>
          </div>
          <div v-else-if="certCheckResult.warnings && certCheckResult.warnings.length > 0" class="mt-3 p-3 bg-yellow-50 rounded-lg">
            <div class="text-sm text-yellow-700 font-medium flex items-center">
              <el-icon class="mr-1"><WarningFilled /></el-icon>
              证件检查通过，但存在即将过期的证件，请谨慎排班
            </div>
          </div>
          <div v-else class="mt-3 p-3 bg-green-50 rounded-lg">
            <div class="text-sm text-green-600 font-medium flex items-center">
              <el-icon class="mr-1"><CircleCheck /></el-icon>
              证件检查全部通过
            </div>
          </div>
        </div>

        <el-form-item label="备注">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button 
            @click="dialogVisible = false"
            class="rounded-lg"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            :loading="submitting"
            :disabled="certCheckResult && !certCheckResult.passed"
          >
            确定排班
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 证件检查详情对话框 -->
    <el-dialog 
      v-model="certDetailDialogVisible" 
      title="证件检查详情" 
      width="600px"
      class="rounded-lg"
    >
      <div v-if="currentCertDetail">
        <div class="mb-3">
          <span class="font-medium">班次：</span>{{ currentAssignment?.workName }}
        </div>
        <div class="mb-4">
          <span class="font-medium">候选人：</span>{{ currentAssignment?.userName }}
        </div>
        <div 
          v-for="item in currentCertDetail" 
          :key="item.certType"
          class="flex items-center justify-between py-3 px-4 mb-2 bg-gray-50 rounded-lg"
        >
          <span class="font-medium">{{ item.certTypeName }}</span>
          <div class="flex items-center gap-3">
            <el-tag 
              :type="getCheckResultType(item.status)"
              size="small"
              class="rounded-full"
            >
              {{ getCheckResultText(item.status) }}
            </el-tag>
            <span class="text-sm text-gray-600">{{ item.message }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Warning, 
  WarningFilled, 
  Document, 
  CircleCheck, 
  CircleClose 
} from '@element-plus/icons-vue'
import { workAssignmentApi } from '@/api/workAssignment'
import { workApi } from '@/api/work'
import { userApi } from '@/api/user'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const workList = ref([])
const userList = ref([])
const dialogVisible = ref(false)
const certDetailDialogVisible = ref(false)
const formRef = ref(null)
const certCheckResult = ref(null)
const currentAssignment = ref(null)
const currentCertDetail = ref(null)

const searchForm = reactive({
  workId: null,
  userId: null,
  assignStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  workId: null,
  userId: null,
  remark: ''
})

const formRules = {
  workId: [
    { required: true, message: '请选择班次', trigger: 'change' }
  ],
  userId: [
    { required: true, message: '请选择候选人', trigger: 'change' }
  ]
}

const loadWorks = async () => {
  try {
    const res = await workApi.page({ current: 1, size: 1000 })
    if (res.code === 200) {
      workList.value = res.data.records
    }
  } catch (error) {
    console.error(error)
  }
}

const loadUsers = async () => {
  try {
    const res = await userApi.page({ current: 1, size: 1000 })
    if (res.code === 200) {
      userList.value = res.data.records
    }
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await workAssignmentApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    workId: null,
    userId: null,
    assignStatus: ''
  })
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    workId: null,
    userId: null,
    remark: ''
  })
  certCheckResult.value = null
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleWorkChange = () => {
  certCheckResult.value = null
  if (formData.workId && formData.userId) {
    previewCertCheck()
  }
}

const handleUserChange = () => {
  certCheckResult.value = null
  if (formData.workId && formData.userId) {
    previewCertCheck()
  }
}

const previewCertCheck = async () => {
  try {
    const res = await workAssignmentApi.previewCertCheck(formData.workId, formData.userId)
    if (res.code === 200) {
      certCheckResult.value = res.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('证件检查失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!certCheckResult.value || !certCheckResult.value.passed) {
        ElMessage.error('证件检查未通过，无法排班')
        return
      }
      submitting.value = true
      try {
        const res = await workAssignmentApi.assign(formData)
        if (res.code === 200) {
          ElMessage.success('排班成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('排班失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleUpdateStatus = async (row, status) => {
  try {
    const statusText = {
      checked_in: '签到',
      completed: '完成'
    }
    await ElMessageBox.confirm(`确定要将该排班标记为${statusText[status]}吗？`, '提示', {
      type: 'warning'
    })
    const res = await workAssignmentApi.updateStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success('操作成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该排班吗？', '提示', {
      type: 'warning'
    })
    const res = await workAssignmentApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('取消失败')
    }
  }
}

const handleViewCertDetail = (row) => {
  currentAssignment.value = row
  if (row.certCheckDetail) {
    try {
      currentCertDetail.value = JSON.parse(row.certCheckDetail)
    } catch (e) {
      currentCertDetail.value = []
    }
  } else {
    currentCertDetail.value = []
  }
  certDetailDialogVisible.value = true
}

const formatRequiredCerts = (certs) => {
  if (!certs) return ''
  const typeMap = {
    electrician: '电工证',
    welder: '焊工证',
    height_work: '高处作业证',
    health: '健康证'
  }
  return certs.split(',').map(c => typeMap[c] || c).join('、')
}

const getAssignStatusType = (status) => {
  const types = {
    assigned: 'primary',
    checked_in: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
}

const getAssignStatusText = (status) => {
  const texts = {
    assigned: '已排班',
    checked_in: '已到岗',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const getCheckResultType = (status) => {
  const types = {
    valid: 'success',
    expiring: 'warning',
    missing: 'danger',
    expired: 'danger',
    rejected: 'danger',
    pending_audit: 'info'
  }
  return types[status] || 'info'
}

const getCheckResultText = (status) => {
  const texts = {
    valid: '有效',
    expiring: '即将过期',
    missing: '缺少',
    expired: '已过期',
    rejected: '已驳回',
    pending_audit: '待审核'
  }
  return texts[status] || status
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadWorks()
  loadUsers()
  loadData()
})
</script>

<style scoped>
.work-assignment-page {
  max-width: 1600px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
