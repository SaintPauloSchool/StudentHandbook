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
const deployOutDir = path.resolve(__dirname, '../deploy-out');

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

// 打包 deploy-out/{version}/ 便於上傳服務器
const targetDir = path.join(deployOutDir, version);
fs.rmSync(targetDir, { recursive: true, force: true });
fs.mkdirSync(targetDir, { recursive: true });
copyDir(path.resolve(__dirname, '../dist'), targetDir);

fs.writeFileSync(
  path.join(deployOutDir, 'nginx-student-handbook.conf'),
  config,
  'utf-8'
);

console.log('');
console.log('=== Student Handbook 發版（方案 A-2 多版本目錄）===');
console.log(`version: ${version}`);
console.log(`資源 URL 示例: /${version}/assets/index-xxxxx.js`);
console.log(`微信訪問後 URL: /${version}/`);
console.log('');
console.log('【必須完成以下四步，缺一都會出問題】');
console.log('  1. 將 deploy-out/' + version + '/ 上傳到服務器：');
console.log('       prod → /usr/share/nginx/student-handbook/prod/' + version + '/');
console.log('       dev  → /usr/share/nginx/student-handbook/dev/' + version + '/');
console.log('  2. 用 deploy-out/nginx-student-handbook.conf 替換 Nginx 中兩個 server 的 handbook 配置');
console.log('  3. 更新 sp-api application-prod.yml：');
console.log('       sp.frontend.version: "' + version + '"');
console.log('     （測試環境 application-test.yml 同步更新）');
console.log('  4. sudo nginx -t && sudo systemctl reload nginx');
console.log('     重啟 sp-api 使 frontend.version 生效');
console.log('');
console.log('【驗證】');
console.log('  curl -sI http://tals-wcapp.esp.edu.mo/ | findstr Location');
console.log('  → 應為 /' + version + '/');
console.log('  cat dist/index.html | findstr assets');
console.log('  → 應含 /' + version + '/assets/');
console.log('');
console.log('【舊版本清理】可保留最近 2～3 個版本目錄便於回滾，其餘可刪除');
console.log('');
console.log(config);

function copyDir(src, dest) {
  fs.mkdirSync(dest, { recursive: true });
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyDir(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}
