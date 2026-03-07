@echo off
rem Ce script force la console en UTF-8 (pour que les accents s'affichent correctement)
chcp 65001 >nul

set JAR=supervision-server\target\supervision-server-1.0-SNAPSHOT-jar-with-dependencies.jar

if not exist "%JAR%" (
  echo Jar introuvable : %JAR%
  echo Compilez le projet avec : mvn clean package
  exit /b 1
)

java "-Dfile.encoding=UTF-8" -jar "%JAR%"
