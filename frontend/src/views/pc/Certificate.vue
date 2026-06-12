<template>
  <div class="certificate-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">证件管理</span>
          <div class="flex gap-3">
            <el-button 
              @click="handleRefreshStatus"
              class="rounded-lg"
            >
              <el-icon><Refresh /></el-icon>
              刷新证件状态
            </el-button>
            <el-button 
              type="primary" 
              @click="handleAdd"
              class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <el-icon><Plus /></el-icon>
              上传证件
            </el-button>
          </div>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
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
        <el-form-item label="证件类型">
          <el-select 
            v-model="searchForm.certType" 
            placeholder="请选择证件类型" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="电工证" value="electrician" />
            <el-option label="焊工证" value="welder" />
            <el-option label="高处作业证" value="height_work" />
            <el-option label="健康证" value="health" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件状态">
          <el-select 
            v-model="searchForm.certStatus" 
            placeholder="请选择证件状态" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="有效" value="valid" />
            <el-option label="即将过期" value="expiring" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select 
            v-model="searchForm.auditStatus" 
            placeholder="请选择审核状态" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
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
        <el-table-column prop="userName" label="持证人" width="120" />
        <el-table-column prop="certType" label="证件类型" width="120">
          <template #default="{ row }">
            {{ getCertTypeName(row.certType) }}
          </template>
        </el-table-column>
        <el-table-column prop="certName" label="证件名称" min-width="150" />
        <el-table-column prop="certNo" label="证件编号" width="150" />
        <el-table-column label="证件照片" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.fileUrl" 
              :src="row.fileUrl" 
              :preview-src-list="[row.fileUrl]"
              fit="cover"
              class="w-12 h-12 rounded cursor-pointer"
            />
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="120" />
        <el-table-column prop="certStatus" label="证件状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getCertStatusType(row.certStatus)"
              class="rounded-full px-3 py-1"
            >
              {{ getCertStatusText(row.certStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getAuditStatusType(row.auditStatus)"
              class="rounded-full px-3 py-1"
            >
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditorName" label="审核人" width="100" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="handleEdit(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              更新证件
            </el-button>
            <el-button 
              v-if="row.auditStatus === 'pending'"
              type="success" 
              size="small" 
              link 
              @click="handleAudit(row)"
              class="hover:text-green-600 transition-colors duration-200"
            >
              审核
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              link 
              @click="handleViewHistory(row)"
              class="hover:text-gray-600 transition-colors duration-200"
            >
              历史记录
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

    <!-- 上传/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
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
        <el-form-item label="持证人" prop="userId">
          <el-select 
            v-model="formData.userId" 
            placeholder="请选择持证人" 
            filterable
            class="w-full rounded-lg"
            :disabled="isEdit"
          >
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.nickname || user.username" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="证件类型" prop="certType">
          <el-select 
            v-model="formData.certType" 
            placeholder="请选择证件类型"
            class="w-full rounded-lg"
            :disabled="isEdit"
          >
            <el-option label="电工证" value="electrician" />
            <el-option label="焊工证" value="welder" />
            <el-option label="高处作业证" value="height_work" />
            <el-option label="健康证" value="health" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件名称" prop="certName">
          <el-input 
            v-model="formData.certName" 
            placeholder="请输入证件名称"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="证件编号" prop="certNo">
          <el-input 
            v-model="formData.certNo" 
            placeholder="请输入证件编号"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="证件照片" prop="fileId">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept="image/*"
          >
            <div v-if="formData.fileUrl" class="relative">
              <img :src="formData.fileUrl" class="w-32 h-32 object-cover rounded-lg" />
              <div class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity rounded-lg">
                <span class="text-white">点击更换</span>
              </div>
            </div>
            <el-button v-else type="primary" class="rounded-lg">
              <el-icon><Upload /></el-icon>
              上传照片
            </el-button>
          </el-upload>
          <div class="text-xs text-gray-500 mt-1">支持 jpg、jpeg、png、gif 格式</div>
        </el-form-item>
        <el-form-item label="到期日期" prop="expireDate">
          <el-date-picker
            v-model="formData.expireDate"
            type="date"
            placeholder="请选择到期日期"
            class="w-full rounded-lg"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
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
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog 
      v-model="auditDialogVisible" 
      title="审核证件" 
      width="500px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <div class="mb-4">
        <div class="text-sm text-gray-600 mb-2">
          <span class="font-medium">持证人：</span>{{ currentAuditCert?.userName }}
        </div>
        <div class="text-sm text-gray-600 mb-2">
          <span class="font-medium">证件类型：</span>{{ getCertTypeName(currentAuditCert?.certType) }}
        </div>
        <div class="text-sm text-gray-600 mb-2">
          <span class="font-medium">证件编号：</span>{{ currentAuditCert?.certNo }}
        </div>
        <div class="text-sm text-gray-600 mb-4">
          <span class="font-medium">证件照片：</span>
        </div>
        <el-image 
          v-if="currentAuditCert?.fileUrl" 
          :src="currentAuditCert.fileUrl" 
          :preview-src-list="[currentAuditCert.fileUrl]"
          fit="contain"
          class="w-full max-h-60 rounded-lg"
        />
      </div>
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="80px">
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio value="approved">通过</el-radio>
            <el-radio value="rejected">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input 
            v-model="auditForm.auditRemark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入审核备注"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="auditDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleAuditSubmit"
            class="rounded-lg"
            :loading="auditing"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog 
      v-model="historyDialogVisible" 
      title="证件变更历史" 
      width="800px"
      class="rounded-lg"
    >
      <el-table 
        :data="historyData" 
        v-loading="historyLoading" 
        border
        class="rounded-lg overflow-hidden"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="certType" label="证件类型" width="120">
          <template #default="{ row }">
            {{ getCertTypeName(row.certType) }}
          </template>
        </el-table-column>
        <el-table-column label="变更前" min-width="200">
          <template #default="{ row }">
            <div class="text-xs">
              <div v-if="row.oldCertNo">编号：{{ row.oldCertNo }}</div>
              <div v-if="row.oldExpireDate">到期：{{ row.oldExpireDate }}</div>
              <div v-if="row.oldAuditorName">审核人：{{ row.oldAuditorName }}</div>
              <div v-if="row.oldFileUrl">
                <el-image 
                  :src="row.oldFileUrl" 
                  :preview-src-list="[row.oldFileUrl]"
                  fit="cover"
                  class="w-12 h-12 rounded mt-1"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="变更后" min-width="200">
          <template #default="{ row }">
            <div class="text-xs">
              <div v-if="row.newCertNo">编号：{{ row.newCertNo }}</div>
              <div v-if="row.newExpireDate">到期：{{ row.newExpireDate }}</div>
              <div v-if="row.newFileUrl">
                <el-image 
                  :src="row.newFileUrl" 
                  :preview-src-list="[row.newFileUrl]"
                  fit="cover"
                  class="w-12 h-12 rounded mt-1"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Upload } from '@element-plus/icons-vue'
import { certificateApi } from '@/api/certificate'
import { userApi } from '@/api/user'
import { fileApi } from '@/api/file'

const loading = ref(false)
const submitting = ref(false)
const auditing = ref(false)
const historyLoading = ref(false)
const tableData = ref([])
const userList = ref([])
const historyData = ref([])
const dialogVisible = ref(false)
const auditDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const dialogTitle = ref('上传证件')
const isEdit = ref(false)
const formRef = ref(null)
const auditFormRef = ref(null)
const currentAuditCert = ref(null)
const currentCertId = ref(null)

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL || '/api'}/file/upload`
const uploadHeaders = {}

const searchForm = reactive({
  userId: null,
  certType: '',
  certStatus: '',
  auditStatus: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  userId: null,
  certType: '',
  certName: '',
  certNo: '',
  fileId: null,
  fileUrl: '',
  expireDate: ''
})

const formRules = {
  userId: [
    { required: true, message: '请选择持证人', trigger: 'change' }
  ],
  certType: [
    { required: true, message: '请选择证件类型', trigger: 'change' }
  ],
  certName: [
    { required: true, message: '请输入证件名称', trigger: 'blur' }
  ],
  certNo: [
    { required: true, message: '请输入证件编号', trigger: 'blur' }
  ],
  expireDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' }
  ]
}

const auditForm = reactive({
  auditStatus: 'approved',
  auditRemark: ''
})

const auditRules = {
  auditStatus: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ]
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
    const res = await certificateApi.page({
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
    userId: null,
    certType: '',
    certStatus: '',
    auditStatus: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '上传证件'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    userId: null,
    certType: '',
    certName: '',
    certNo: '',
    fileId: null,
    fileUrl: '',
    expireDate: ''
  })
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleEdit = (row) => {
  dialogTitle.value = '更新证件（将保留历史记录）'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    userId: row.userId,
    certType: row.certType,
    certName: row.certName,
    certNo: row.certNo,
    fileId: row.fileId,
    fileUrl: row.fileUrl,
    expireDate: row.expireDate
  })
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const beforeUpload = (file) => {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB！')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    formData.fileId = response.data.id
    formData.fileUrl = response.data.filePath
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await certificateApi.update(formData.id, formData)
        } else {
          res = await certificateApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功，历史记录已保留' : '上传成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '上传失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleAudit = (row) => {
  currentAuditCert.value = row
  auditForm.auditStatus = 'approved'
  auditForm.auditRemark = ''
  auditDialogVisible.value = true
}

const handleAuditSubmit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      auditing.value = true
      try {
        const res = await certificateApi.audit(
          currentAuditCert.value.id,
          auditForm.auditStatus,
          auditForm.auditRemark
        )
        if (res.code === 200) {
          ElMessage.success('审核成功')
          auditDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('审核失败')
      } finally {
        auditing.value = false
      }
    }
  })
}

const handleViewHistory = async (row) => {
  currentCertId.value = row.id
  historyDialogVisible.value = true
  historyLoading.value = true
  try {
    const res = await certificateApi.getHistory(row.id, { current: 1, size: 100 })
    if (res.code === 200) {
      historyData.value = res.data.records
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载历史记录失败')
  } finally {
    historyLoading.value = false
  }
}

const handleRefreshStatus = async () => {
  try {
    await ElMessageBox.confirm('确定要刷新所有证件状态吗？', '提示', {
      type: 'warning'
    })
    const res = await certificateApi.refreshStatus()
    if (res.code === 200) {
      ElMessage.success('刷新成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('刷新失败')
    }
  }
}

const getCertTypeName = (type) => {
  const types = {
    electrician: '电工证',
    welder: '焊工证',
    height_work: '高处作业证',
    health: '健康证'
  }
  return types[type] || type
}

const getCertStatusType = (status) => {
  const types = {
    valid: 'success',
    expiring: 'warning',
    expired: 'danger'
  }
  return types[status] || 'info'
}

const getCertStatusText = (status) => {
  const texts = {
    valid: '有效',
    expiring: '即将过期',
    expired: '已过期'
  }
  return texts[status] || status
}

const getAuditStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getAuditStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
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
  loadUsers()
  loadData()
})
</script>

<style scoped>
.certificate-page {
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

.upload-demo {
  display: inline-block;
}
</style>
