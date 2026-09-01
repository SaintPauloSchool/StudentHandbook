/** 是否在企業微信 WebView 內（UA 含 wxwork） */
export function isWeComEnv() {
    return /wxwork/i.test(navigator.userAgent)
}

/** 是否在微信 / 企業微信 WebView 內 */
export function isWeChatEnv() {
    return /MicroMessenger/i.test(navigator.userAgent) || isWeComEnv()
}

/**
 * OAuth 渠道：企微職工 wecom，微信家長 wechat。
 * 後端依 state 前綴決定兌換 auth/getuserinfo 或 school/getuserinfo。
 */
export function getOAuthChannel() {
    return isWeComEnv() ? 'wecom' : 'wechat'
}

/**
 * 構建 OAuth state。
 * 例：wecom / wechat / wecom_campus_notice_123 / wechat_campus_notice_root
 */
export function buildOAuthState(redirectToCampus) {
    const channel = getOAuthChannel()
    if (redirectToCampus) {
        return `${channel}_campus_notice_${redirectToCampus}`
    }
    return channel
}

/** state 是否含校園通知跳轉意圖 */
export function isCampusNoticeState(state) {
    return !!state && state.includes('campus_notice_')
}

/**
 * 從 state 解析校園通知 ID。
 * 兼容舊格式 campus_notice_xxx 與新格式 wecom_campus_notice_xxx。
 */
export function parseCampusNoticeId(state) {
    if (!state) {
        return null
    }
    const marker = 'campus_notice_'
    const idx = state.indexOf(marker)
    if (idx < 0) {
        return null
    }
    return state.substring(idx + marker.length) || null
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
 * @returns {boolean} 是否為「新登錄」（URL 中的 token 與本地不同或本地尚無 token）
 */
export function saveTokenFromUrl(urlParams = new URLSearchParams(window.location.search)) {
    const tokenFromUrl = urlParams.get('token')
    if (!tokenFromUrl) {
        return false
    }

    const existingToken = localStorage.getItem('token')
    const isNewLogin = !existingToken || existingToken !== tokenFromUrl

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
