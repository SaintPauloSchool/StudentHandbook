/**
 * 構建後執行，讀取 dist/version.json 並輸出 Nginx 版本號。
 *
 * 用法：
 *   npm run build && npm run nginx-version
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const versionFile = path.resolve(__dirname, '../dist/version.json');

if (!fs.existsSync(versionFile)) {
  console.error('找不到 dist/version.json，請先執行 npm run build');
  process.exit(1);
}

const { version } = JSON.parse(fs.readFileSync(versionFile, 'utf-8'));

console.log('');
console.log('=== Student Handbook 發版 ===');
console.log(`version: ${version}`);
console.log(`訪問路徑前綴: /${version}/`);
console.log('');
console.log('【1】Nginx：prod + dev 各更新一行');
console.log(`    set $app_version "${version}";`);
console.log('');
console.log('【2】Nginx：用 deploy/nginx-full-example.conf 替換配置');
console.log('    ★ 刪除所有舊的 ?_v= / need_v / fix_v 邏輯');
console.log('    ★ 改用路徑版本號 /' + version + '/  不再用 query');
console.log('');
console.log('【3】部署 dist/ → nginx -t && nginx -s reload');
console.log('【4】企微測試：打開後 URL 應為 /' + version + '/');
console.log('');
