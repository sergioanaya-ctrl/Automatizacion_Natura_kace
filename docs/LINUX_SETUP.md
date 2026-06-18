# ==========================================
# CONFIGURACIÓN LINUX - AUTOMATIZACIÓN SARA3
# ==========================================
# Este archivo explica cómo ejecutar la automatización en Linux sin interfaz gráfica

## 1. REQUISITOS DEL SISTEMA
## ==========================

# En Ubuntu/Debian:
# sudo apt-get update
# sudo apt-get install -y \
#   openjdk-21-jdk \
#   google-chrome-stable \
#   xvfb \
#   libxrender1 \
#   libx11-6 \
#   curl \
#   wget \
#   git

# En CentOS/RHEL:
# sudo yum install -y \
#   java-21-openjdk \
#   google-chrome-stable \
#   xorg-x11-server-Xvfb \
#   libXrender \
#   libX11 \
#   curl \
#   wget \
#   git


## 2. OPCIÓN A: EJECUCIÓN CON XVFB (Virtual Display)
## ====================================================

# Instala Xvfb (X Virtual Framebuffer):
# sudo apt-get install xvfb

# Ejecuta el test con Xvfb:
# export DISPLAY=:99
# Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
# ./gradlew.bat clean test --tests "com.natura.automation.runners.CasesRunner15"

# O usa un script wrapper:
# #!/bin/bash
# export DISPLAY=:99
# Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
# XVFB_PID=$!
# ./gradlew clean test --tests "com.natura.automation.runners.CasesRunner15"
# kill $XVFB_PID


## 3. OPCIÓN B: HEADLESS MODE (Recomendado)
## ==========================================

# No requiere servidor gráfico. Chrome corre sin interfaz visual.
# La clase ChromeHeadlessConfig.java lo hace automáticamente en Linux.

# Ejecuta simplemente:
# ./gradlew clean test --tests "com.natura.automation.runners.CasesRunner15"

# Ventajas:
# - No requiere Xvfb
# - Más rápido
# - Menor consumo de memoria
# - Funciona en contenedores Docker


## 4. OPCIÓN C: DOCKER (Más simple para CI/CD)
## ===============================================

# Crea un Dockerfile:
# ---
# FROM openjdk:21-jdk
# 
# RUN apt-get update && apt-get install -y \
#     google-chrome-stable \
#     wget \
#     curl \
#     git \
#     gradle \
#     && rm -rf /var/lib/apt/lists/*
# 
# WORKDIR /automation
# COPY . .
# 
# CMD ["./gradlew", "clean", "test"]
# ---

# Ejecuta:
# docker build -t sara-automation .
# docker run --rm sara-automation


## 5. CONFIGURACIÓN RECOMENDADA PARA LINUX
## =========================================

# En tu serenity.properties (en Linux):
# chrome.capabilities=--headless --disable-gpu --no-sandbox --disable-dev-shm-usage

# Variables de entorno (~/.bashrc o en CI/CD):
# export CHROME_DRIVER_PATH=/usr/bin/chromedriver  (si no está en PATH)
# export DISPLAY=:99                                (solo si usas Xvfb)


## 6. SCRIPT DE EJECUCIÓN RECOMENDADO (run-tests-linux.sh)
## =========================================================
# #!/bin/bash
# set -e
# 
# echo "[SETUP] Preparando ambiente Linux..."
# export DISPLAY=:99
# 
# # Opcional: Inicia Xvfb si lo necesitas
# # Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
# # XVFB_PID=$!
# # sleep 2
# 
# echo "[TEST] Ejecutando tests en Linux..."
# ./gradlew clean test --tests "com.natura.automation.runners.CasesRunner15"
# TEST_RESULT=$?
# 
# # Mata Xvfb si lo iniciaste
# # if [ ! -z "$XVFB_PID" ]; then
# #     kill $XVFB_PID 2>/dev/null || true
# # fi
# 
# exit $TEST_RESULT


## 7. VERIFICACIÓN EN LINUX
## ==========================

# Verifica que Chrome esté instalado:
# google-chrome --version
# 
# Verifica WebDriver:
# which chromedriver
# 
# Prueba conexión básica:
# java -version
# ./gradlew --version


## 8. TROUBLESHOOTING LINUX
## =========================

# Error: "Failed to start Chrome"
# → Instala: sudo apt-get install libglib2.0-0 libx11-6 libxrender1

# Error: "Permission denied" en gradlew
# → chmod +x ./gradlew

# Error: "No display"
# → Usa headless mode o instala Xvfb

# Error: "Chrome crashed"
# → Añade: --disable-dev-shm-usage --no-sandbox

# Error: "Port already in use"
# → ./gradlew clean test (limpia antes de ejecutar)


## 9. VARIABLES DE ENTORNO RECOMENDADAS
## ======================================

# JAVA_OPTS="-Xmx2g -Xms512m"
# GRADLE_OPTS="-Xmx2g"
# HEADLESS_MODE=true
# CHROME_ARGS="--headless --disable-gpu --no-sandbox"



