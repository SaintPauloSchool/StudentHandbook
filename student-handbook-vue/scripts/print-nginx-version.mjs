/**
 * 構建後執行，讀取 dist/version.json 並輸出 Nginx 需同步的版本號。
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
console.log('=== Student Handbook 發版版本號 ===');
console.log(`version: ${version}`);
console.log('');
console.log('請將以下兩處 Nginx 配置中的 $app_version 改為同一值，然後 reload nginx：');
console.log('');
console.log(`    set $app_version "${version}";`);
console.log('');
