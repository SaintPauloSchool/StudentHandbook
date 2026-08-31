/**
 * 上傳附件的 Nginx 公開路徑（與服務器 location /profile/ → alias /usr/local/upload/ 一致）
 * 不走 /sp-api/profile，由 Nginx 直接讀磁盤。
 */
export const PROFILE_PUBLIC_PATH = '/profile'

/**
 * 將庫裡存的附件路徑統一為 /profile/... 相對路徑。
 * 兼容：/profile、/sp-api/profile、/sms-api/profile、完整 http URL。
 */
export function toPublicProfilePath(url) {
  if (!url || typeof url !== 'string') {
    return ''
  }

  let result = url.trim()

  if (result.startsWith('http://') || result.startsWith('https://')) {
    try {
      const parsed = new URL(result)
      const path = parsed.pathname || ''
      if (!path.includes('/profile/')) {
        return result
      }
      const suffix = path.replace(/^.*?\/profile\/?/, '')
      result = `${PROFILE_PUBLIC_PATH}/${suffix}`
    } catch (e) {
      return url
    }
  } else if (result.startsWith('/sp-api/profile/') || result.startsWith('/sp-api/profile//')) {
    result = result.replace(/^\/sp-api\/profile\/+/, `${PROFILE_PUBLIC_PATH}/`)
  } else if (result.startsWith('/sms-api/profile/') || result.startsWith('/sms-api/profile//')) {
    result = result.replace(/^\/sms-api\/profile\/+/, `${PROFILE_PUBLIC_PATH}/`)
  } else if (result.startsWith('/profile/') || result.startsWith('/profile//')) {
    result = result.replace(/^\/profile\/+/, `${PROFILE_PUBLIC_PATH}/`)
  } else if (result.startsWith('profile/')) {
    result = `${PROFILE_PUBLIC_PATH}/${result.substring('profile/'.length)}`
  } else if (!result.startsWith(PROFILE_PUBLIC_PATH)) {
    return result
  }

  return collapseDuplicateSlashes(result)
}

/**
 * 生成瀏覽器可訪問的附件 URL（默認帶當前站點 origin）。
 */
export function normalizeProfileUrl(url, options = {}) {
  const origin = options.origin || (typeof window !== 'undefined' ? window.location.origin : '')
  let path = toPublicProfilePath(url)

  if (!path) {
    return ''
  }

  // 非 /profile 路徑（外部鏈接等）原樣返回
  if (!path.startsWith(PROFILE_PUBLIC_PATH)) {
    return path.startsWith('http') ? path : (options.absolute && origin ? origin + path : path)
  }

  if (options.encode) {
    path = encodeUriPath(path)
  }

  if (options.absolute && origin) {
    return origin + path
  }

  return path
}

/** 路徑各段 encode，避免中文文件名在 img src 中加載失敗 */
function encodeUriPath(path) {
  return path
    .split('/')
    .map((segment) => {
      if (!segment) {
        return ''
      }
      try {
        return encodeURIComponent(decodeURIComponent(segment))
      } catch (e) {
        return encodeURIComponent(segment)
      }
    })
    .join('/')
}

function collapseDuplicateSlashes(value) {
  return value.replace(/([^:])\/\/+/g, '$1/')
}

/**
 * 構建後端安全下載地址（PDF/Word 等 Nginx 禁止直鏈的格式走此接口）。
 * @param {string} resourcePath 庫裡存的 /profile/... 路徑
 * @param {string} [downloadName] 展示用文件名
 */
export function buildProfileDownloadUrl(resourcePath, downloadName, options = {}) {
  const path = toPublicProfilePath(resourcePath)
  if (!path || !path.startsWith(PROFILE_PUBLIC_PATH)) {
    return ''
  }

  const origin = options.origin || (typeof window !== 'undefined' ? window.location.origin : '')
  const base = options.downloadApiPath || '/sp-api/common/download/resource'
  const params = new URLSearchParams()
  params.set('resource', path)
  if (downloadName) {
    params.set('name', downloadName)
  }

  const apiPath = base.startsWith('/') ? base : `/${base}`
  return `${origin}${apiPath}?${params.toString()}`
}
