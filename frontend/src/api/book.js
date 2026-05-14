import request from '@/utils/request'

export function getBookList(params) {
  return request({
    url: '/book/list',
    method: 'get',
    params
  })
}

export function getBookById(id) {
  return request({
    url: `/book/${id}`,
    method: 'get'
  })
}

export function createBook(data) {
  return request({
    url: '/book',
    method: 'post',
    data
  })
}

export function updateBook(id, data) {
  return request({
    url: `/book/${id}`,
    method: 'put',
    data
  })
}

export function deleteBook(id) {
  return request({
    url: `/book/${id}`,
    method: 'delete'
  })
}

export function getBooksByCategory(category, subCategory, params) {
  return request({
    url: '/book/category',
    method: 'get',
    params: { category, subCategory, ...params }
  })
}

export function getCategories() {
  return request({
    url: '/book/categories',
    method: 'get'
  })
}

export function uploadBooks(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/book/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 新增接口
export function getBooksByAgeRange(ageRange) {
  return request({
    url: `/book/age-range/${encodeURIComponent(ageRange)}`,
    method: 'get'
  })
}

export function getBooksByKnowledgeType(knowledgeType) {
  return request({
    url: `/book/knowledge-type/${encodeURIComponent(knowledgeType)}`,
    method: 'get'
  })
}

export function getBooksByArtStyle(artStyle) {
  return request({
    url: `/book/art-style/${encodeURIComponent(artStyle)}`,
    method: 'get'
  })
}

export function filterBooks(params) {
  return request({
    url: '/book/filter',
    method: 'get',
    params
  })
}

export function getAgeRanges() {
  return request({
    url: '/book/age-ranges',
    method: 'get'
  })
}

export function getKnowledgeTypes() {
  return request({
    url: '/book/knowledge-types',
    method: 'get'
  })
}

export function getArtStyles() {
  return request({
    url: '/book/art-styles',
    method: 'get'
  })
}

export function getBookStats() {
  return request({
    url: '/book/stats',
    method: 'get'
  })
}

export function getHotBooks(limit = 10) {
  return request({
    url: '/book/hot',
    method: 'get',
    params: { limit }
  })
}

export function getBookPages(bookId) {
  return request({
    url: `/book-pages/${bookId}`,
    method: 'get'
  })
}
