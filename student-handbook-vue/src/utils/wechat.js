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
 * 清理回跳地址中的 token/userType，避免 401 後把帶 token 的舊鏈接再次寫回。
 * 僅允許站內相對路徑。
 */
export function sanitizeRedirectUrl(rawUrl) {
    if (!rawUrl || typeof rawUrl !== 'string') {
        return '/'
    }
    let path = rawUrl.trim()
    try {
        if (path.startsWith('http://') || path.startsWith('https://')) {
            const parsed = new URL(path)
            path = parsed.pathname + parsed.search + parsed.hash
        }
    } catch (e) {
        return '/'
    }
    // 必須是站內相對路徑；拒絕 //host 協議相對 URL，避免開放重定向
    if (!path.startsWith('/') || path.startsWith('//')) {
        return '/'
    }
    const hashIndex = path.indexOf('#')
    const hash = hashIndex >= 0 ? path.substring(hashIndex) : ''
    const withoutHash = hashIndex >= 0 ? path.substring(0, hashIndex) : path
    const qIndex = withoutHash.indexOf('?')
    const pathname = qIndex >= 0 ? withoutHash.substring(0, qIndex) : withoutHash
    if (!pathname.startsWith('/') || pathname.startsWith('//') || pathname.includes('://')) {
        return '/'
    }
    const query = qIndex >= 0 ? withoutHash.substring(qIndex + 1) : ''
    const params = new URLSearchParams(query)
    params.delete('token')
    params.delete('userType')
    const search = params.toString()
    return pathname + (search ? `?${search}` : '') + hash
}

/** 清除本地緩存的當前學生信息（換帳號/登出時必須清，否則會顯示別人孩子） */
export function clearStudentSession() {
    localStorage.removeItem('currentStudentId')
    localStorage.removeItem('currentStudentName')
    localStorage.removeItem('currentStudentClassSection')
    localStorage.removeItem('currentStudentProfileNumber')
    localStorage.removeItem('studentSessionToken')
}

/** 清除登錄憑證與學生緩存 */
export function clearAuthSession() {
    localStorage.removeItem('token')
    localStorage.removeItem('token_expire')
    localStorage.removeItem('userType')
    clearStudentSession()
}

/** 當前學生緩存是否屬於當前 token（防止換帳號後仍顯示上一任孩子） */
export function isStudentSessionOwnedByCurrentToken() {
    const token = localStorage.getItem('token')
    const owner = localStorage.getItem('studentSessionToken')
    return !!token && !!owner && token === owner
}

/**
 * 讀取當前學生緩存。
 * token 不匹配或沒有綁定時一律清空並返回空，避免誤顯示別人孩子。
 */
export function getCurrentStudentSession() {
    if (!isStudentSessionOwnedByCurrentToken()) {
        clearStudentSession()
        return {
            studentId: '',
            studentName: '',
            classSection: '',
            studentProfileNumber: ''
        }
    }
    return {
        studentId: localStorage.getItem('currentStudentId') || '',
        studentName: localStorage.getItem('currentStudentName') || '',
        classSection: localStorage.getItem('currentStudentClassSection') || '',
        studentProfileNumber: localStorage.getItem('currentStudentProfileNumber') || ''
    }
}

/** 寫入當前學生，並綁定到當前 token */
export function setCurrentStudentSession({ studentId, studentName, classSection, studentProfileNumber }) {
    const token = localStorage.getItem('token')
    if (!token || !studentId) {
        clearStudentSession()
        return
    }
    localStorage.setItem('currentStudentId', studentId)
    localStorage.setItem('currentStudentName', studentName || '')
    localStorage.setItem('currentStudentClassSection', classSection || '')
    localStorage.setItem('currentStudentProfileNumber', studentProfileNumber || '')
    localStorage.setItem('studentSessionToken', token)
}

/**
 * 用後端學生列表校驗/補齊當前學生。
 * @param {Function} fetchStudents 返回 Promise，resolve 為 relations 數組
 * @returns {object|null} 選中的學生關係，無則 null
 */
export async function ensureCurrentStudent(fetchStudents) {
    const token = localStorage.getItem('token')
    if (!token) {
        clearStudentSession()
        return null
    }

    const relations = await fetchStudents()
    if (!Array.isArray(relations) || relations.length === 0) {
        clearStudentSession()
        return null
    }

    const saved = getCurrentStudentSession()
    const matched = saved.studentId
        ? relations.find((r) => r.studentId === saved.studentId)
        : null
    const selected = matched || relations[0]
    setCurrentStudentSession({
        studentId: selected.studentId,
        studentName: selected.studentName,
        classSection: selected.classSection || '',
        studentProfileNumber: selected.studentProfileNumber || ''
    })
    return selected
}

/**
 * 從 URL 參數保存 token。
 * @returns {boolean} URL 中是否帶有 token（已寫入 localStorage 並從地址欄移除）
 */
export function saveTokenFromUrl(urlParams = new URLSearchParams(window.location.search)) {
    const tokenFromUrl = urlParams.get('token')
    if (!tokenFromUrl) {
        return false
    }

    const existingToken = localStorage.getItem('token')
    const isNewLogin = !existingToken || existingToken !== tokenFromUrl

    // 換帳號時清掉上一任家長/學生緩存，避免頂欄顯示別人孩子
    if (isNewLogin) {
        clearStudentSession()
    }

    localStorage.setItem('token', tokenFromUrl)
    localStorage.setItem('token_expire', (Date.now() + 7 * 24 * 60 * 60 * 1000).toString())

    const urlUserType = urlParams.get('userType')
    if (urlUserType !== null && urlUserType !== '') {
        localStorage.setItem('userType', urlUserType)
    } else if (isNewLogin) {
        // 新登錄但 URL 未帶 userType 時，清掉上一任身份，避免短暫誤顯示家長頂欄
        localStorage.removeItem('userType')
    }

    // 寫入 localStorage 後立刻從地址欄移除 token，避免微信「複製鏈接」洩露登錄態
    window.history.replaceState({}, document.title, stripTokenFromUrlParams(urlParams))

    // 供調用方判斷是否應提示「登錄成功」（僅真正換帳號時）
    saveTokenFromUrl.lastWasNewLogin = isNewLogin
    return true
}

/**
 * 從當前地址欄移除 token/userType。
 * 不再把登錄態同步回 URL，防止家長互轉鏈接時冒用對方身份。
 */
export function stripAuthParamsFromUrl() {
    const params = new URLSearchParams(window.location.search)
    if (!params.has('token') && !params.has('userType')) {
        return
    }
    window.history.replaceState({}, document.title, stripTokenFromUrlParams(params))
}

/**
 * @deprecated 已改為移除 URL 中的 token，保留導出名避免舊引用報錯
 */
export function syncTokenToUrlForWeChatShare() {
    stripAuthParamsFromUrl()
}
