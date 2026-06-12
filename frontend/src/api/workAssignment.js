import request from '@/utils/request'

export const workAssignmentApi = {
  assign(data) {
    return request({
      url: '/work-assignment',
      method: 'post',
      data
    })
  },
  previewCertCheck(workId, userId) {
    return request({
      url: '/work-assignment/preview-cert-check',
      method: 'get',
      params: { workId, userId }
    })
  },
  getById(id) {
    return request({
      url: `/work-assignment/${id}`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/work-assignment/page',
      method: 'get',
      params
    })
  },
  updateStatus(id, assignStatus) {
    return request({
      url: `/work-assignment/${id}/status`,
      method: 'put',
      data: { assignStatus }
    })
  },
  delete(id) {
    return request({
      url: `/work-assignment/${id}`,
      method: 'delete'
    })
  }
}
