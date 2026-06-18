/**
 * 構建後執行，輸出帶實際版本號的完整 Nginx 配置。
 *
 * 用法：
 *   npm run build && npm run nginx-version
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const versionFile = path.resolve(__dirname, '../dist/version.json');
const templateFile = path.resolve(__dirname, '../deploy/nginx-full-example.conf');

if (!fs.existsSync(versionFile)) {
  console.error('找不到 dist/version.json，請先執行 npm run build');
  process.exit(1);
}

if (!fs.existsSync(templateFile)) {
  console.error('找不到 deploy/nginx-full-example.conf');
  process.exit(1);
}

const { version } = JSON.parse(fs.readFileSync(versionFile, 'utf-8'));
const config = fs.readFileSync(templateFile, 'utf-8').replaceAll('REPLACE_VERSION', version);

console.log('');
console.log('=== Student Handbook 發版 ===');
console.log(`version: ${version}`);
console.log(`微信訪問後 URL 應為: /${version}/ （只有一個版本號）`);
console.log(`舊版書籤 /舊版本/ 刷新後應 302 到 /${version}/，不應出現 /${version}/舊版本/`);
console.log('');
console.log('【重要】必須同時完成以下三步，缺一都會出問題：');
console.log('  1. 用下方配置【整份替換】 sp-api 中的兩個 server');
console.log('  2. 部署【本次 build 的】dist/ 到服務器');
console.log('  3. sudo nginx -t && sudo systemctl reload nginx');
console.log('');
console.log('【驗證】發新版後在舊 URL 按刷新，應跳到 /' + version + '/ 而非雙層版本路徑');
console.log('【防死循環】以下場景均應 ≤3 次跳轉後穩定，不應 ERR_TOO_MANY_REDIRECTS：');
console.log('  / → /' + version + '/ → 200');
console.log('  /舊版本/ → /' + version + '/ → 200');
console.log('  /' + version + '/舊版本/ → /' + version + '/ → 200');
console.log('  /' + version + '/' + version + '/login → /' + version + '/login → 200');
console.log('');
console.log(config);
