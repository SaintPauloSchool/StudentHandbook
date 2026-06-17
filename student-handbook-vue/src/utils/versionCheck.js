/**
 * 版本檢查 & 緩存清除工具
 *
 * 原理：
 *  - 每次構建時 vite.config.js 會生成 /version.json（含構建時間戳）
 *  - App 啟動時用 fetch + no-store 從服務器拉取最新版本（繞過瀏覽器緩存）
 *  - 若服務器版本 ≠ localStorage 緩存的版本，說明有新部署
 *  - 立即將 URL 加上 ?_v=新版本號 後 replace（WeChat WebView 遇到新 URL 必須重新請求）
 *
 * 注意：首次部署此機制後需要用戶手動清一次緩存，之後全自動生效。
 */

const VERSION_KEY = '__app_cache_version';
const RELOAD_FLAG = '_v';

export async function checkAndClearCache() {
  try {
    // 1. 拉取服務器最新 version.json（強制不使用任何緩存）
    const res = await fetch('/version.json?' + Date.now(), {
      method: 'GET',
      cache: 'no-store',
      headers: {
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache'
      }
    });

    if (!res.ok) return; // 獲取失敗靜默退出

    const { version: serverVersion } = await res.json();
    if (!serverVersion) return;

    const urlParams = new URLSearchParams(window.location.search);
    const currentV = urlParams.get(RELOAD_FLAG);
    const storedVersion = localStorage.getItem(VERSION_KEY);

    // URL 已帶正確版本號且 localStorage 已同步 → 無需操作
    if (currentV === serverVersion && storedVersion === serverVersion) {
      return;
    }

    // 版本變更時清除 Service Worker 緩存（如果有的話）
    if (storedVersion !== null && storedVersion !== serverVersion) {
      if ('caches' in window) {
        const cacheNames = await caches.keys();
        await Promise.all(cacheNames.map(name => caches.delete(name)));
      }
    }

    localStorage.setItem(VERSION_KEY, serverVersion);

    // URL 缺少或版本號不對 → replace 強制微信重新請求 index.html
    if (currentV !== serverVersion) {
      urlParams.set(RELOAD_FLAG, serverVersion);
      const newUrl = window.location.pathname + '?' + urlParams.toString() + window.location.hash;

      console.log(`[版本更新] ${storedVersion || '(首次)'} → ${serverVersion}，強制重載...`);
      window.location.replace(newUrl);
      return true;
    }

  } catch (err) {
    // 獲取失敗（無網絡等情況）不影響正常使用
    console.warn('[versionCheck] 版本檢查失敗（靜默忽略）:', err.message);
  }
}
