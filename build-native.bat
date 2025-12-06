@echo off
REM Build native executable with GraalVM native-image
REM Usage: build-native.bat <chemin-dossier-musique-test>
REM
REM Prerequisites:
REM   - GraalVM JDK installed and in PATH
REM   - Visual Studio Build Tools (MSVC compiler)
REM   - Run from "x64 Native Tools Command Prompt" or after running vcvars64.bat

setlocal

if "%~1"=="" (
    echo Usage: build-native.bat ^<chemin-dossier-musique^>
    echo.
    echo Example: build-native.bat D:\Music\TestFolder
    exit /b 1
)

set MUSIC_DIR=%~1
set JAR_NAME=AudioFilesSorter-0.2.0.jar
set TARGET_DIR=audiosorter-cli\target
set NATIVE_CONFIG=%TARGET_DIR%\native-config

echo.
echo === Step 1/4: Building JAR ===
echo.
call mvn clean package -pl audiosorter-cli -am -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed
    exit /b 1
)

echo.
echo === Step 2/4: Running agent to collect native-image metadata ===
echo Using test directory: %MUSIC_DIR%
echo.
if exist "%NATIVE_CONFIG%" rmdir /s /q "%NATIVE_CONFIG%"
java -agentlib:native-image-agent=config-output-dir=%NATIVE_CONFIG% ^
     -jar %TARGET_DIR%\%JAR_NAME% scan "%MUSIC_DIR%"
if errorlevel 1 (
    echo ERROR: Agent tracing failed
    exit /b 1
)

echo.
echo === Step 3/3: Compiling native executable ===
echo This may take several minutes and use ~8 GB RAM...
echo.
native-image -jar %TARGET_DIR%\%JAR_NAME% -H:ConfigurationFileDirectories=%NATIVE_CONFIG% -H:Name=%TARGET_DIR%\AudioFilesSorter-native --no-fallback

endlocal
