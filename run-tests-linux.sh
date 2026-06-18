#!/bin/bash

# ============================================================================
# SCRIPT PARA EJECUTAR AUTOMATIZACIÓN SARA3 EN LINUX (HEADLESS)
# ============================================================================
# Uso: ./run-tests-linux.sh [runner_class]
# Ejemplo: ./run-tests-linux.sh "com.natura.automation.runners.CasesRunner15"
#
# Este script detecta automáticamente si está en Linux y activa modo headless
# ============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Detectar sistema operativo
OS_NAME=$(uname -s)
IS_LINUX=false

case "$OS_NAME" in
    Linux*)
        IS_LINUX=true
        echo -e "${GREEN}[SETUP] Sistema detectado: LINUX${NC}"
        ;;
    Darwin*)
        echo -e "${GREEN}[SETUP] Sistema detectado: macOS${NC}"
        ;;
    MINGW*|MSYS*|CYGWIN*)
        echo -e "${GREEN}[SETUP] Sistema detectado: WINDOWS${NC}"
        ;;
    *)
        echo -e "${YELLOW}[SETUP] Sistema desconocido: $OS_NAME${NC}"
        ;;
esac

# Parámetros
RUNNER_CLASS=${1:-"com.natura.automation.runners.CasesRunner15"}

echo -e "${BLUE}[SETUP] Preparando ambiente para ejecución...${NC}"
echo "[SETUP] Runner: $RUNNER_CLASS"
echo "[SETUP] Java version: $(java -version 2>&1 | head -1)"

# Opcional: Configurar Xvfb si lo necesitas (comentado por defecto)
# Si descomenta estas líneas, el script usará un display virtual
# export DISPLAY=:99
# echo "[SETUP] Iniciando servidor virtual X (Xvfb)..."
# Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
# XVFB_PID=$!
# sleep 2
# echo "[SETUP] Xvfb iniciado con PID: $XVFB_PID"

# Variables de entorno para mejor rendimiento
export GRADLE_OPTS="-Xmx2g -Xms512m"
export JAVA_TOOL_OPTIONS="-Xmx2g -Xms512m"

# Limpia y ejecuta los tests
echo -e "${BLUE}[TEST] Ejecutando tests...${NC}"

if [ "$IS_LINUX" = true ]; then
    echo "[CONFIG] ✓ Modo HEADLESS activado automáticamente"
    echo "[CONFIG] ✓ ChromeHeadlessConfig.java lo manejará"
fi

./gradlew test --tests "$RUNNER_CLASS" \
    --continue \
    --no-daemon

TEST_RESULT=$?

# Limpia Xvfb si lo iniciaste
# if [ ! -z "$XVFB_PID" ]; then
#     echo "[CLEANUP] Terminando Xvfb (PID: $XVFB_PID)..."
#     kill $XVFB_PID 2>/dev/null || true
#     sleep 1
# fi

# Resultado final
echo ""
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}[RESULT] ✓ TESTS EJECUTADOS EXITOSAMENTE${NC}"
    echo -e "${GREEN}[RESULT] Reporte disponible en: target/site/serenity/index.html${NC}"
else
    echo -e "${RED}[RESULT] ✗ FALLÓ LA EJECUCIÓN (Exit Code: $TEST_RESULT)${NC}"
fi

exit $TEST_RESULT
