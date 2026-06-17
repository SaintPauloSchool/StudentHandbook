/**
 * 版本檢查：拉取 /version.json，確保 URL 帶 ?_v= 以繞過微信 WebView 頁面緩存。
 * 首次部署後，仍握有舊 index.html 的用戶需清一次微信緩存。
 */

export const VERSION_KEY = '__app_cache_version';
export const RELOAD_FLAG = '_v';

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
      return;
    }

    localStorage.setItem(VERSION_KEY, serverVersion);

    if (currentV !== serverVersion) {
      urlParams.set(RELOAD_FLAG, serverVersion);
      const newUrl = window.location.pathname + '?' + urlParams.toString() + window.location.hash;
      window.location.replace(newUrl);
      return true;
    }
  } catch (err) {
    console.warn('[versionCheck] 版本檢查失敗:', err.message);
  }
}
