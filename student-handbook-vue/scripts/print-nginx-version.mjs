/**
 * 構建後執行，輸出帶實際版本號的 Nginx 配置片段。
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

const { version } = JSON.parse(fs.readFileSync(versionFile, 'utf-8'));
const template = fs.readFileSync(templateFile, 'utf-8');
const config = template.replaceAll('REPLACE_VERSION', version);

console.log('');
console.log('=== Student Handbook 發版 ===');
console.log(`version: ${version}`);
console.log(`訪問路徑: /${version}/`);
console.log('');
console.log('【1】用下方完整配置替換 sp-api 中兩個 server 塊');
console.log('【2】部署 dist/');
console.log('【3】sudo nginx -t && sudo systemctl reload nginx');
console.log('');
console.log(config);
