@echo off
set libdir=\platform\WINNT_x86-msvc\lib
set currdir=%cd%
set PATH = %PATH%;%currdir%\%libdir%
start /max %currdir%\xulrunner-stub.exe
