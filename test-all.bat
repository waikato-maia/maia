@echo off

@rem A valid JDK is required. Use one of the following to set:
@rem PowerShell: Set-Item -Path Env:JAVA_HOME -Value "X:\Path\To\JDK"
@rem CMD: set JAVA_HOME=X:\Path\To\JDK

echo JAVA_HOME=%JAVA_HOME%

call :TestRepo maia-build
call :TestRepo maia-build-moa
call :TestRepo maia-arff
call :TestRepo maia-configure
call :TestRepo maia-dataset
call :TestRepo maia-json-configuration
call :TestRepo maia-learner
call :TestRepo maia-learner-factory
call :TestRepo maia-moa-dataset-nodes
call :TestRepo maia-moa-datasets
call :TestRepo maia-moa-learners
call :TestRepo maia-primitive
call :TestRepo maia-standard-dataset-nodes
call :TestRepo maia-standard-datasets
call :TestRepo maia-standard-learner-nodes
call :TestRepo maia-standard-learners
call :TestRepo maia-standard-nodes
call :TestRepo maia-standard-types
call :TestRepo maia-topology
call :TestRepo maia-topology-json
call :TestRepo maia-util
cd ../maia
goto :eof

@rem cds into the argument repo and runs the tests
:TestRepo
echo Testing %~1...
cd ../%~1
call ./gradlew.bat clean build test
if %ERRORLEVEL% neq 0 (
    echo Error testing %~1
    @rem Exit using (goto) 2>nul trick as in https://stackoverflow.com/questions/3227796/exit-batch-script-from-inside-a-function
    cd ../maia
    (goto) 2>nul
    exit /B %ERRORLEVEL%
)
