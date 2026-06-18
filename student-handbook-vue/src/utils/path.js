const VERSION_PREFIX_RE = /^\/\d{14}(?=\/|$)/

export function getVersionPrefix() {
    const m = window.location.pathname.match(VERSION_PREFIX_RE)
    return m ? m[0] : ''
}

export function detectVersionBase() {
    const prefix = getVersionPrefix()
    return prefix ? `${prefix}/` : '/'
}

/** 剝掉路徑中的版本前綴，供 Vue Router 使用（可重複剝離歷史髒數據） */
export function stripVersionPrefix(pathname) {
    let path = pathname || '/'
    let prev
    do {
        prev = path
        const m = path.match(/^\/\d{14}(\/.*)?$/)
        if (m) {
            path = m[1] || '/'
        }
    } while (path !== prev)
    return path.startsWith('/') ? path : `/${path}`
}

/** sessionStorage 中的 redirect_url 轉為 Router 路徑 */
export function normalizeRedirectUrl(url) {
    if (!url) return '/'

    const hashIndex = url.indexOf('#')
    const queryIndex = url.indexOf('?')
    const end = Math.min(
        hashIndex === -1 ? url.length : hashIndex,
        queryIndex === -1 ? url.length : queryIndex
    )
    const pathname = stripVersionPrefix(url.slice(0, end) || '/')
    const rest = url.slice(end)
    return `${pathname}${rest}`
}

export function versionedPath(path) {
    const prefix = getVersionPrefix()
    const normalized = path.startsWith('/') ? path : `/${path}`
    return prefix ? `${prefix}${normalized}` : normalized
}

/** 若地址欄版本號重疊，返回應使用的 Router 內部路徑；否則返回 null */
export function getDoubledVersionFix() {
    if (typeof window === 'undefined') return null

    const internalPath = stripVersionPrefix(window.location.pathname)
    const canonical = versionedPath(internalPath)
    if (canonical === window.location.pathname) {
        return null
    }
    return internalPath
}

/** 將瀏覽器地址欄矯正為「單層版本前綴 + 路由路徑」（僅改地址欄，不觸發路由） */
export function sanitizeBrowserUrl() {
    if (typeof window === 'undefined') return false

    const canonical = versionedPath(stripVersionPrefix(window.location.pathname))
    if (canonical === window.location.pathname) {
        return false
    }

    window.history.replaceState(
        {},
        document.title,
        canonical + window.location.search + window.location.hash
    )
    return true
}
