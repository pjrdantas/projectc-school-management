@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo   LOCAL K8S PIPELINE - BLINDADO
echo ==========================================

set APP_NAME=projeto-piloto
set IMAGE=%APP_NAME%:local

echo.
echo [0/7] CHECK CONTEXT KUBERNETES
kubectl config current-context

echo.
echo [1/7] CLEAN CLUSTER (REMOVE APP ONLY)

kubectl delete deployment %APP_NAME% --ignore-not-found=true
kubectl delete replicaset -l app=%APP_NAME% --ignore-not-found=true
kubectl delete pods -l app=%APP_NAME% --ignore-not-found=true

echo.
echo [2/7] MAVEN BUILD
call mvn clean package -DskipTests

IF %ERRORLEVEL% NEQ 0 (
    echo ERRO MAVEN
    exit /b 1
)

echo.
echo [3/7] DOCKER BUILD (LOCAL IMAGE)
docker build -t %IMAGE% .

IF %ERRORLEVEL% NEQ 0 (
    echo ERRO DOCKER BUILD
    exit /b 1
)

echo.
echo [4/7] VERIFY IMAGE EXISTS
docker images | findstr %APP_NAME%

echo.
echo [5/7] APPLY KUBERNETES CLEAN
kubectl apply -f k8s/deployment.yaml

IF %ERRORLEVEL% NEQ 0 (
    echo ERRO KUBECTL APPLY
    exit /b 1
)

echo.
echo [6/7] WAIT POD READY
kubectl wait --for=condition=Ready pods -l app=%APP_NAME% --timeout=180s

echo.
echo [7/7] STATUS FINAL
kubectl get pods -l app=%APP_NAME%

echo.
echo ==========================================
echo DEPLOY FINALIZADO COM SUCESSO
echo IMAGE: %IMAGE%
echo ==========================================

pause
endlocal