/** 是否在微信 / 企業微信 WebView 內 */
export function isWeChatEnv() {
    return /MicroMessenger/i.test(navigator.userAgent)
}

function stripTokenFromUrlParams(urlParams) {
    urlParams.delete('token')
    urlParams.delete('userType')
    return window.location.pathname +
        (urlParams.toString() ? '?' + urlParams.toString() : '') +
        window.location.hash
}

/**
 * 從 URL 參數保存 token。
 * 微信內：保留 URL 中的 token，方便用戶用微信自帶「複製鏈接」到瀏覽器。
 * 普通瀏覽器：保存後立即從地址欄清除 token。
 */
export function saveTokenFromUrl(urlParams = new URLSearchParams(window.location.search)) {
    const tokenFromUrl = urlParams.get('token')
    if (!tokenFromUrl) {
        return false
    }

    const isNewLogin = localStorage.getItem('token') !== tokenFromUrl

    localStorage.setItem('token', tokenFromUrl)
    localStorage.setItem('token_expire', (Date.now() + 7 * 24 * 60 * 60 * 1000).toString())

    const urlUserType = urlParams.get('userType')
    if (urlUserType !== null) {
        localStorage.setItem('userType', urlUserType)
    }

    if (!isWeChatEnv()) {
        window.history.replaceState({}, document.title, stripTokenFromUrlParams(urlParams))
    }
    return isNewLogin
}

/**
 * 微信內路由跳轉後，把 token 同步回 URL，使「複製鏈接」自帶登錄信息。
 */
export function syncTokenToUrlForWeChatShare(routePath) {
    if (!isWeChatEnv() || routePath === '/login') {
        return
    }

    const token = localStorage.getItem('token')
    if (!token) {
        return
    }

    const params = new URLSearchParams(window.location.search)
    if (params.get('token') === token) {
        return
    }

    params.set('token', token)
    const userType = localStorage.getItem('userType')
    if (userType !== null && userType !== '') {
        params.set('userType', userType)
    }

    const newUrl = window.location.pathname +
        (params.toString() ? '?' + params.toString() : '') +
        window.location.hash
    window.history.replaceState({}, document.title, newUrl)
}
