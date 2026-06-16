#!/bin/bash
# ============================================================
# SARA3 - LINUX SETUP AUTOMATION
# Instala y configura automáticamente todas las dependencias
# ============================================================

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}"
echo "=========================================================="
echo "  SARA3 - Linux Headless Automatic Setup"
echo "=========================================================="
echo -e "${NC}"

# ============================================================
# FUNCIONES
# ============================================================

check_sudo() {
    if [ "$EUID" -ne 0 ]; then 
        echo -e "${YELLOW}Este script requiere permisos de sudo${NC}"
        echo "Ejecuta: sudo bash setup_linux.sh"
        exit 1
    fi
}

install_java() {
    echo -e "${BLUE}[1/5] Instalando Java...${NC}"
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1)
        echo -e "${GREEN}✓ Java ya instalado: $JAVA_VERSION${NC}"
        return
    fi
    
    apt-get update -qq
    
    # Intentar instalar Java 8 (compatibilidad con código legacy)
    if ! apt-get install -y openjdk-8-jdk 2>/dev/null; then
        # Fallback a Java 11
        echo -e "${YELLOW}Java 8 no disponible, usando Java 11${NC}"
        apt-get install -y openjdk-11-jdk
    fi
    
    echo -e "${GREEN}✓ Java instalado${NC}"
}

install_chrome() {
    echo -e "${BLUE}[2/5] Instalando Chrome/Chromium...${NC}"
    
    if command -v chromium-browser &> /dev/null || command -v google-chrome &> /dev/null; then
        CHROME_VERSION=$(chromium-browser --version 2>/dev/null || google-chrome --version 2>/dev/null || echo "Desconocida")
        echo -e "${GREEN}✓ Chrome ya instalado: $CHROME_VERSION${NC}"
        return
    fi
    
    apt-get update -qq
    
    # Intentar instalar Chromium (más ligero)
    if apt-get install -y chromium-browser 2>/dev/null; then
        echo -e "${GREEN}✓ Chromium instalado${NC}"
    else
        # Fallback a Google Chrome
        echo -e "${YELLOW}Chromium no disponible, instalando Google Chrome${NC}"
        wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
        sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
        apt-get update -qq
        apt-get install -y google-chrome-stable
        echo -e "${GREEN}✓ Google Chrome instalado${NC}"
    fi
}

install_chromedriver() {
    echo -e "${BLUE}[3/5] Instalando ChromeDriver...${NC}"
    
    if command -v chromedriver &> /dev/null; then
        CHROMEDRIVER_VERSION=$(chromedriver --version | cut -d' ' -f2)
        echo -e "${GREEN}✓ ChromeDriver ya instalado: $CHROMEDRIVER_VERSION${NC}"
        return
    fi
    
    # Obtener versión de Chrome
    if command -v chromium-browser &> /dev/null; then
        CHROME_VERSION=$(chromium-browser --version | awk '{print $NF}' | cut -d'.' -f1)
        CHROME_BIN="chromium-browser"
    elif command -v google-chrome &> /dev/null; then
        CHROME_VERSION=$(google-chrome --version | awk '{print $NF}' | cut -d'.' -f1)
        CHROME_BIN="google-chrome"
    else
        echo -e "${RED}✗ Chrome no está instalado${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Versión de Chrome: $CHROME_VERSION${NC}"
    
    # Descargar ChromeDriver correspondiente
    CHROMEDRIVER_URL="https://chromedriver.chromium.org/download"
    
    # Usar la API de chromedriver (simplificado)
    echo "Descargando ChromeDriver para Chrome $CHROME_VERSION..."
    
    if [ ! -f "chromedriver_linux64.zip" ]; then
        # Usar versión 148 como ejemplo (ajustar según necesidad)
        wget -q "https://edgedl.me/chromedriver/LATEST_RELEASE_$CHROME_VERSION" -O latest_version.txt 2>/dev/null || true
        
        if [ -f "latest_version.txt" ]; then
            LATEST_VERSION=$(cat latest_version.txt)
            wget "https://edgedl.me/chromedriver/$LATEST_VERSION/chromedriver_linux64.zip" -O chromedriver_linux64.zip 2>/dev/null || {
                echo -e "${YELLOW}No se pudo descargar automáticamente${NC}"
                echo "Descarga manualmente desde: https://chromedriver.chromium.org/download"
                echo "Coloca el archivo chromedriver en /usr/local/bin/"
                return 1
            }
        fi
    fi
    
    if [ -f "chromedriver_linux64.zip" ]; then
        unzip -o chromedriver_linux64.zip -d /tmp/
        mv /tmp/chromedriver /usr/local/bin/
        chmod +x /usr/local/bin/chromedriver
        rm -f chromedriver_linux64.zip
        echo -e "${GREEN}✓ ChromeDriver instalado${NC}"
    fi
}

install_dependencies() {
    echo -e "${BLUE}[4/5] Instalando dependencias del sistema...${NC}"
    
    apt-get update -qq
    apt-get install -y \
        build-essential \
        curl \
        wget \
        git \
        xvfb \
        libxss1 \
        fonts-liberation \
        xdg-utils \
        unzip \
        2>/dev/null || true
    
    # Instalar PowerShell (opcional, para reportes)
    if ! command -v pwsh &> /dev/null; then
        apt-get install -y powershell 2>/dev/null || {
            echo -e "${YELLOW}PowerShell no disponible (opcional para reportes)${NC}"
        }
    fi
    
    echo -e "${GREEN}✓ Dependencias del sistema instaladas${NC}"
}

configure_project() {
    echo -e "${BLUE}[5/5] Configurando proyecto Sara3...${NC}"
    
    # Dar permisos de ejecución
    chmod +x gradlew 2>/dev/null || true
    chmod +x run_tests.sh 2>/dev/null || true
    chmod +x *.sh 2>/dev/null || true
    
    # Configurar variables de entorno
    if [ -f "$HOME/.bashrc" ]; then
        if ! grep -q "CHROME_BIN" "$HOME/.bashrc"; then
            cat >> "$HOME/.bashrc" << 'EOF'

# SARA3 Environment Variables
export DISPLAY=""
export QT_QPA_PLATFORM="offscreen"
export CHROME_BIN=$(which chromium-browser || which google-chrome)
EOF
            echo -e "${GREEN}✓ Variables de entorno configuradas en ~/.bashrc${NC}"
        fi
    fi
    
    # Verificar compilación
    echo "Verificando compilación del proyecto..."
    if [ -f "./gradlew" ]; then
        ./gradlew compileTestJava -q || {
            echo -e "${RED}✗ Error durante compilación${NC}"
            return 1
        }
        echo -e "${GREEN}✓ Proyecto compilado correctamente${NC}"
    fi
}

verify_installation() {
    echo ""
    echo -e "${BLUE}========================================================${NC}"
    echo -e "${BLUE}  VERIFICACIÓN DE INSTALACIÓN${NC}"
    echo -e "${BLUE}========================================================${NC}"
    echo ""
    
    # Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1)
        echo -e "${GREEN}✓ Java:${NC} $JAVA_VERSION"
    else
        echo -e "${RED}✗ Java: NO INSTALADO${NC}"
    fi
    
    # Chrome
    if command -v chromium-browser &> /dev/null; then
        CHROME_VERSION=$(chromium-browser --version)
        echo -e "${GREEN}✓ Chrome:${NC} $CHROME_VERSION"
    elif command -v google-chrome &> /dev/null; then
        CHROME_VERSION=$(google-chrome --version)
        echo -e "${GREEN}✓ Chrome:${NC} $CHROME_VERSION"
    else
        echo -e "${RED}✗ Chrome: NO INSTALADO${NC}"
    fi
    
    # ChromeDriver
    if command -v chromedriver &> /dev/null; then
        CHROMEDRIVER_VERSION=$(chromedriver --version | cut -d' ' -f1-3)
        echo -e "${GREEN}✓ ChromeDriver:${NC} $CHROMEDRIVER_VERSION"
    else
        echo -e "${RED}✗ ChromeDriver: NO INSTALADO${NC}"
    fi
    
    # Gradle
    if [ -f "./gradlew" ]; then
        GRADLE_VERSION=$(./gradlew -version 2>&1 | grep "Gradle" | head -1)
        echo -e "${GREEN}✓ Gradle:${NC} $GRADLE_VERSION"
    fi
    
    # Serenity Config
    if grep -q "headless" serenity.properties 2>/dev/null; then
        echo -e "${GREEN}✓ Serenity:${NC} Configurado para headless"
    else
        echo -e "${YELLOW}⚠ Serenity:${NC} No hay configuración headless"
    fi
    
    echo ""
}

run_test() {
    echo ""
    echo -e "${YELLOW}¿Deseas ejecutar un test de prueba? (s/n)${NC}"
    read -p "Opcion: " run_test_choice
    
    if [ "$run_test_choice" = "s" ] || [ "$run_test_choice" = "S" ]; then
        echo -e "${BLUE}Ejecutando test de prueba...${NC}"
        if ./gradlew test --tests "com.sara.automation.runners.CasesRunner01" -q; then
            echo -e "${GREEN}✓ Test ejecutado exitosamente${NC}"
        else
            echo -e "${RED}✗ Hubo un error en la ejecución del test${NC}"
        fi
    fi
}

# ============================================================
# PROGRAMA PRINCIPAL
# ============================================================

main() {
    check_sudo
    
    install_java
    install_chrome
    install_chromedriver
    install_dependencies
    configure_project
    verify_installation
    run_test
    
    echo ""
    echo -e "${GREEN}========================================================${NC}"
    echo -e "${GREEN}  ✓ CONFIGURACIÓN COMPLETADA${NC}"
    echo -e "${GREEN}========================================================${NC}"
    echo ""
    echo "Para ejecutar tests:"
    echo -e "${BLUE}  ./run_tests.sh${NC}"
    echo ""
    echo "O ejecutar tests directamente:"
    echo -e "${BLUE}  ./gradlew test --parallel${NC}"
    echo ""
    echo "Para más información:"
    echo -e "${BLUE}  cat LINUX_HEADLESS_SETUP.md${NC}"
    echo ""
}

# Ejecutar
main
