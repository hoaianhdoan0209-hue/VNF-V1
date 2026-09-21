@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo [VNF] Debug build preparation...

if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME is not set.
  echo Install Android Studio or JDK 17, then set JAVA_HOME to a Java 17 JDK.
  pause
  exit /b 1
)

if "%ANDROID_SDK_ROOT%"=="" (
  if not "%ANDROID_HOME%"=="" set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
)
if "%ANDROID_SDK_ROOT%"=="" (
  echo [ERROR] ANDROID_SDK_ROOT or ANDROID_HOME is not set.
  echo In Android Studio, install Android SDK Platform 35, then set ANDROID_SDK_ROOT.
  pause
  exit /b 1
)

set "GRADLE_VERSION=8.14.1"
set "CACHE=%USERPROFILE%\.vnf-build"
set "GRADLE_HOME=%CACHE%\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%CACHE%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%CACHE%" mkdir "%CACHE%"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo [VNF] Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 goto :fail
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Force '%GRADLE_ZIP%' '%CACHE%'"
  if errorlevel 1 goto :fail
)

echo sdk.dir=%ANDROID_SDK_ROOT:=\%>local.properties

echo [VNF] Building debug APK...
call "%GRADLE_HOME%\bin\gradle.bat" :app:assembleDebug --stacktrace
if errorlevel 1 goto :fail

if not exist "app\build\outputs\apk\debug\app-debug.apk" goto :fail

echo.
echo ===============================================
echo BUILD SUCCESS
echo APK: %CD%\app\build\outputs\apk\debug\app-debug.apk
echo ===============================================
pause
exit /b 0

:fail
echo.
echo ===============================================
echo BUILD FAILED
echo Copy the full error shown above back to ChatGPT.
echo ===============================================
pause
exit /b 1
