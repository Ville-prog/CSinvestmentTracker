@echo off
setlocal enabledelayedexpansion

set MAVEN_VERSION=3.9.6
set DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd
set TMP_ZIP=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin.zip

if not exist "%MVN_CMD%" (
    echo Downloading Maven %MAVEN_VERSION%...
    if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"
    curl -fsSL -o "%TMP_ZIP%" "%DISTRIBUTION_URL%"
    powershell -Command "Expand-Archive -Path '%TMP_ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
    del "%TMP_ZIP%"
)

"%MVN_CMD%" %*
