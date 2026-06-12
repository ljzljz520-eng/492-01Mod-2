import request from '@/utils/request'

export const certificateApi = {
  save(data) {
    return request({
      url: '/certificate',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/certificate/${id}`,
      method: 'put',
      data
    })
  },
  audit(id, auditStatus, auditRemark) {
    return request({
      url: `/certificate/${id}/audit`,
      method: 'put',
      data: { auditStatus, auditRemark }
    })
  },
  getById(id) {
    return request({
      url: `/certificate/${id}`,
      method: 'get'
    })
  },
  page(params) {
    return request({
      url: '/certificate/page',
      method: 'get',
      params
    })
  },
  getByUserId(userId) {
    return request({
      url: `/certificate/user/${userId}`,
      method: 'get'
    })
  },
  getHistory(id, params) {
    return request({
      url: `/certificate/${id}/history`,
      method: 'get',
      params
    })
  },
  refreshStatus() {
    return request({
      url: '/certificate/refresh-status',
      method: 'post'
    })
  },
  checkCerts(userId, requiredCerts) {
    return request({
      url: '/certificate/check',
      method: 'post',
      params: { userId, requiredCerts }
    })
  }
}
