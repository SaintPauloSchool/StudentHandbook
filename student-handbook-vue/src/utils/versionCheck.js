/**
 * 版本檢查：以 /version.json 為唯一版本來源，確保 URL 帶正確 ?_v=
 */

export const VERSION_KEY = '__app_cache_version';
export const RELOAD_FLAG = '_v';
const LOOP_COUNT_KEY = '__v_redirect_count';

export async function checkAndClearCache() {
  try {
    const res = await fetch('/version.json?t=' + Date.now(), { cache: 'no-store' });
    if (!res.ok) return;

    const { version: serverVersion } = await res.json();
    if (!serverVersion) return;

    const urlParams = new URLSearchParams(window.location.search);
    const currentV = urlParams.get(RELOAD_FLAG);

    // URL 已正確 → 同步 localStorage 即可，不再跳轉
    if (currentV === serverVersion) {
      localStorage.setItem(VERSION_KEY, serverVersion);
      sessionStorage.removeItem(LOOP_COUNT_KEY);
      return;
    }

    // 防止死循環：同一會話最多跳轉 1 次
    const redirectCount = parseInt(sessionStorage.getItem(LOOP_COUNT_KEY) || '0', 10);
    if (redirectCount >= 1) {
      console.warn('[versionCheck] 已跳轉過，跳過（請確認 Nginx 已刪除 fix_v 舊邏輯）');
      localStorage.setItem(VERSION_KEY, serverVersion);
      return;
    }

    localStorage.setItem(VERSION_KEY, serverVersion);
    sessionStorage.setItem(LOOP_COUNT_KEY, String(redirectCount + 1));

    urlParams.set(RELOAD_FLAG, serverVersion);
    const newUrl = window.location.pathname + '?' + urlParams.toString() + window.location.hash;
    window.location.replace(newUrl);
    return true;
  } catch (err) {
    console.warn('[versionCheck] 版本檢查失敗:', err.message);
  }
}
