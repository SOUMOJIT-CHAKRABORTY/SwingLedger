@echo off
setlocal
cd /d "%~dp0server"
if not exist "target\app-0.0.1-SNAPSHOT.jar" (
  echo Building SwingLedger. Java 21+ and Maven 3.9+ must be installed.
  call mvn --batch-mode --no-transfer-progress package
  if errorlevel 1 (pause & exit /b 1)
)
echo.
echo Open http://127.0.0.1:8080 once SwingLedger starts.
echo Keep this window open. Press Ctrl+C to stop.
echo Your local records are saved in server\data.
echo.
java -jar target\app-0.0.1-SNAPSHOT.jar
pause
