import request from '@/utils/request'

export function getChildList(params) {
  return request({
    url: '/child/list',
    method: 'get',
    params
  })
}

export function getChildById(id) {
  return request({
    url: `/child/${id}`,
    method: 'get'
  })
}

export function createChild(data) {
  return request({
    url: '/child',
    method: 'post',
    data
  })
}

export function updateChild(id, data) {
  return request({
    url: `/child/${id}`,
    method: 'put',
    data
  })
}

export function deleteChild(id) {
  return request({
    url: `/child/${id}`,
    method: 'delete'
  })
}

export function bindChild(data) {
  return request({
    url: '/child/bind',
    method: 'post',
    data
  })
}

export function getMyChildren() {
  return request({
    url: '/child/my-children',
    method: 'get'
  })
}

export function getChildrenByClassId(classId) {
  return request({
    url: `/child/class/${classId}`,
    method: 'get'
  })
}
