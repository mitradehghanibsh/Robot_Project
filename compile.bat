@echo off
"C:\Program Files\Eclipse Adoptium\jdk-17.0.12.7-hotspot\bin\javac.exe" -cp "lib\*" -d . src\*.java
if %errorlevel% neq 0 (
    echo BUILD FAILED
    pause
    exit /b
)
echo SUCCESSFUL
pause