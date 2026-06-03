@echo off
echo.
echo [信息] 打包Web工程，生成war/jar包文件。
echo.

%~d0
cd %~dp0

cd ..
echo [信息] 清理舊的構建文件...
call mvn clean

echo [信息] 安裝依賴...
call mvn install -DskipTests

echo [信息] 編譯和打包項目...
call mvn package -DskipTests

echo [信息] 構建完成!
pause