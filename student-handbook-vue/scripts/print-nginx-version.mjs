/**
 * 構建後執行，讀取 dist/version.json 並輸出 Nginx 需同步的版本號與配置片段。
 *
 * 用法：
 *   npm run build && npm run nginx-version
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const versionFile = path.resolve(__dirname, '../dist/version.json');
const snippetFile = path.resolve(__dirname, '../deploy/nginx-cache-bust.conf');

if (!fs.existsSync(versionFile)) {
  console.error('找不到 dist/version.json，請先執行 npm run build');
  process.exit(1);
}

const { version } = JSON.parse(fs.readFileSync(versionFile, 'utf-8'));
const snippet = fs.readFileSync(snippetFile, 'utf-8');

console.log('');
console.log('=== Student Handbook 發版 ===');
console.log(`version: ${version}`);
console.log('');
console.log('【1】Nginx server 級：更新 $app_version（prod + dev 各一處）');
console.log('');
console.log(`    set $app_version "${version}";`);
console.log('');
console.log('【2】Nginx location / 內、try_files 之前：用下方片段【替换】旧的版本跳转');
console.log('    （必须删除 if ($request_uri = /) 以及 if ($arg_v != $app_version) 旧逻辑）');
console.log('');
console.log(snippet);
console.log('【3】部署 dist/ → nginx -t && nginx -s reload');
console.log('');
