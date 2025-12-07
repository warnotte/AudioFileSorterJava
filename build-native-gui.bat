@echo off
REM Build native executable for GUI with GraalVM native-image
REM Usage: build-native-gui.bat
REM
REM Prerequisites:
REM   - GraalVM JDK installed (GRAALVM_HOME set or in PATH)
REM   - Visual Studio Build Tools (MSVC compiler)
REM   - Run from "x64 Native Tools Command Prompt" or after running vcvars64.bat

setlocal

set JAR_NAME=AudioFilesSorter-GUI-0.2.0.jar
set TARGET_DIR=audiosorter-gui\target
set NATIVE_CONFIG=%TARGET_DIR%\native-config

REM Use GRAALVM_HOME if native-image not in PATH
if exist "%GRAALVM_HOME%\bin\native-image.cmd" (
    set NATIVE_IMAGE=%GRAALVM_HOME%\bin\native-image.cmd
) else (
    set NATIVE_IMAGE=native-image
)

echo.
echo === Step 1/4: Building JAR ===
echo.
call mvn clean package -pl audiosorter-gui -am -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed
    exit /b 1
)

echo.
echo === Step 2/4: Running agent to collect native-image metadata ===
echo Please interact with the GUI (scan a folder, change theme, etc.) then close it.
echo.
if exist "%NATIVE_CONFIG%" rmdir /s /q "%NATIVE_CONFIG%"

REM Run with agent - user needs to interact with GUI
java -agentlib:native-image-agent=config-output-dir=%NATIVE_CONFIG% ^
     -jar %TARGET_DIR%\%JAR_NAME%
if errorlevel 1 (
    echo ERROR: Agent tracing failed
    exit /b 1
)

echo.
echo === Step 3/4: Adding JavaFX native configurations ===
echo.

REM Create resource-config for JavaFX FXML and CSS
echo { >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo   "resources": { >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo     "includes": [ >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": "views/.*\\.fxml"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": "styles/.*\\.css"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": ".*\\.properties"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": ".*\\.ftl"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": "com/sun/javafx/.*"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": "com/sun/glass/.*"}, >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo       {"pattern": "com/sun/prism/.*"} >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo     ] >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo   } >> "%NATIVE_CONFIG%\resource-config-extra.json"
echo } >> "%NATIVE_CONFIG%\resource-config-extra.json"

echo.
echo === Step 4/4: Compiling native executable ===
echo This may take several minutes and use ~8 GB RAM...
echo.

%NATIVE_IMAGE% ^
    -jar %TARGET_DIR%\%JAR_NAME% ^
    -H:ConfigurationFileDirectories=%NATIVE_CONFIG% ^
    -H:Name=%TARGET_DIR%\AudioFilesSorter-GUI-native ^
    -H:+ReportExceptionStackTraces ^
    --no-fallback ^
    --enable-url-protocols=https,http ^
    -Djava.awt.headless=false

if errorlevel 1 (
    echo.
    echo ERROR: Native compilation failed
    exit /b 1
)

echo.
echo === SUCCESS ===
echo Native executable created: %TARGET_DIR%\AudioFilesSorter-GUI-native.exe
echo.

endlocal
