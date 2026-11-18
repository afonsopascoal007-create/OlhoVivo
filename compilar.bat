@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -cp "lib\*" -d out @fontes.txt
if errorlevel 1 (
  echo.
  echo ERRO na compilacao. Verifique se a pasta lib contem os 2 jars (driver MariaDB/MySQL e iText).
  pause
  exit /b 1
)
del fontes.txt
echo Compilado com sucesso. Execute executar.bat
pause
