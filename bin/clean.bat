@echo off
echo.
echo [信息] 清理Web工程。
echo.

%~d0
cd %~dp0

cd ..
echo [信息] 清理Maven構建目錄...
call mvn clean

echo [信息] 清理前端Node模塊和構建目錄...
cd student-handbook-vue
if exist node_modules (
  echo [信息] 刪除node_modules目錄...
  rmdir /s /q node_modules
)

if exist dist (
  echo [信息] 刪除dist目錄...
  rmdir /s /q dist
)

cd ..
echo [信息] 清理完成!
pause