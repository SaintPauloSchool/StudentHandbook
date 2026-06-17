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
console.log(`微信訪問後 URL 應為: /${version}/`);
console.log('');
console.log('【重要】必須同時完成以下三步，缺一都會出問題：');
console.log('  1. 用下方配置【整份替換】 sp-api 中的兩個 server');
console.log('  2. 部署【本次 build 的】dist/ 到服務器');
console.log('  3. sudo nginx -t && sudo systemctl reload nginx');
console.log('');
console.log('【若仍死循環】先刪除 location / 裡 if/return/rewrite 三行，只留 try_files 止血');
console.log('');
console.log(config);
