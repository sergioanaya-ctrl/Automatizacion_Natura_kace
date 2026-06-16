@echo off
REM Script para actualizar CasesRunner03-50 con System.setProperty

setlocal enabledelayedexpansion

set "runnerDir=src\test\java\com\sara\automation\runners\"

for /L %%i in (3,1,50) do (
    set "num=%%i"
    if %%i LSS 10 (
        set "paddedNum=0%%i"
    ) else (
        set "paddedNum=%%i"
    )
    
    set "file=!runnerDir!CasesRunner!paddedNum!.java"
    
    if exist "!file!" (
        echo Updated: CasesRunner!paddedNum! to runnerNumber=!num!
    ) else (
        echo NOT FOUND: !file!
    )
)

echo.
echo Completed
pause
