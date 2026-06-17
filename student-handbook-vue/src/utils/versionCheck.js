/**
 * 版本檢查：以 /version.json 為準，確保 URL 帶正確 ?_v=。
 * Nginx 僅在 _v 缺失時補版本號；_v 過期時由此處更新，避免與 Nginx 互相 302 死循環。
 */

export const VERSION_KEY = '__app_cache_version';
export const RELOAD_FLAG = '_v';
const LOOP_GUARD_KEY = '__v_redirect_ts';

export async function checkAndClearCache() {
  try {
    const res = await fetch('/version.json?t=' + Date.now(), { cache: 'no-store' });
    if (!res.ok) return;

    const { version: serverVersion } = await res.json();
    if (!serverVersion) return;

    const urlParams = new URLSearchParams(window.location.search);
    const currentV = urlParams.get(RELOAD_FLAG);
    const storedVersion = localStorage.getItem(VERSION_KEY);

    if (currentV === serverVersion && storedVersion === serverVersion) {
      sessionStorage.removeItem(LOOP_GUARD_KEY);
      return;
    }

    localStorage.setItem(VERSION_KEY, serverVersion);

    if (currentV !== serverVersion) {
      const lastRedirect = sessionStorage.getItem(LOOP_GUARD_KEY);
      if (lastRedirect && Date.now() - parseInt(lastRedirect, 10) < 5000) {
        console.warn('[versionCheck] 检测到重定向循环，已停止（请检查 Nginx $app_version 是否与 version.json 一致）');
        return;
      }

      sessionStorage.setItem(LOOP_GUARD_KEY, Date.now().toString());
      urlParams.set(RELOAD_FLAG, serverVersion);
      const newUrl = window.location.pathname + '?' + urlParams.toString() + window.location.hash;
      window.location.replace(newUrl);
      return true;
    }
  } catch (err) {
    console.warn('[versionCheck] 版本檢查失敗:', err.message);
  }
}
