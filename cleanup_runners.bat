@echo off
REM Simplifica todos los CasesRunners removiendo System.setProperty
REM Ya no es necesario con estrategia de seleccion aleatoria

setlocal enabledelayedexpansion

set "runnerDir=src\test\java\com\sara\automation\runners"

for /L %%i in (2,1,50) do (
    if %%i LSS 10 (
        set "paddedNum=0%%i"
    ) else (
        set "paddedNum=%%i"
    )
    
    set "file=!runnerDir!\CasesRunner!paddedNum!.java"
    
    echo !file!
)

echo Completed
