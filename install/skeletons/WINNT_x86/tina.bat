@echo off
REM echo hello %USERNAME%, you have %NUMBER_OF_PROCESSORS% processors.
set JAVA_HOME=%cd%\java
PATH=%JAVA_HOME%;%JAVA_HOME%\lib\
set NLTK_DATA=%cd%\shared\nltk_data
REM echo *******************************
REM echo PAT=%PATH%
REM echo NLTK_DATA=%NLTK_DATA%
REM echo *******************************
REM  start /max %cd%\xulrunner-stub.exe
REM xulrunner-stub.exe
echo Loading TinaSoft, please wait..
xulrunner\xulrunner.exe application.ini
