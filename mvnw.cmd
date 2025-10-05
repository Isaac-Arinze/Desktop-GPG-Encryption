@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for Windows
@REM ----------------------------------------------------------------------------
@ECHO OFF

setlocal

set ERROR_CODE=0

set MVNW_REPOURL=https://repo.maven.apache.org/maven2
set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

IF NOT EXIST "%WRAPPER_JAR%" (
  powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'" || (
    ECHO Failed to download Maven Wrapper JAR
    set ERROR_CODE=1
    GOTO :error
  )
)

@REM Find project base dir by looking for .mvn directory
set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn" (
  set EXEC_DIR=%CD%
  set WDIR=%EXEC_DIR%
  :findBaseDir
  IF EXIST "%WDIR%\.mvn" (
    set MAVEN_PROJECTBASEDIR=%WDIR%
    GOTO baseDirFound
  )
  cd ..
  IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
  set WDIR=%CD%
  GOTO findBaseDir
  :baseDirNotFound
  set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
  cd "%EXEC_DIR%"
)
:baseDirFound

set JAVA_EXE=java
"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
endlocal & set EXIT_CODE=%ERROR_CODE%
cmd /C exit /B %EXIT_CODE%

