@echo off
echo hello %USERNAME%, you have %NUMBER_OF_PROCESSORS% processors.
set PLATFORMDIR=%cd%\platform\WINNT_x86-msvc
set LIBDIR=%PLATFORMDIR%\lib
set BERKELEYBIN=%PLATFORMDIR%\berkeley\bin
set APPDATA=%PLATFORMDIR%\AppData
set JAVA_HOME=%cd%\java
PATH=%BERKELEYBIN%;%LIBDIR%;%APPDATA%;%JAVA_HOME%;%JAVA_HOME%\lib\
set NLTK_DATA=%cd%\shared\nltk_data
echo appdata=%APPDATA%
echo *******************************
echo path=%PATH%
echo *******************************
echo nltk=%NLTK_DATA%
echo *******************************
REM  start /max %cd%\xulrunner-stub.exe
REM xulrunner-stub.exe
xulrunner\xulrunner.exe application.ini
