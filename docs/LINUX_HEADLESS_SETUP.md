# 🐧 CONFIGURACIÓN SARA3 - LINUX HEADLESS

## Resumen
Este documento describe cómo ejecutar la automatización SARA3 en un servidor Linux sin interfaz gráfica.

---

## 📋 REQUISITOS PREVIOS

### 1. **Java 8+ (REQUERIDO)**
```bash
# Instalar Java 8
sudo apt-get update
sudo apt-get install -y openjdk-8-jdk

# Instalar Java 11, 17, o 21 (opcional, para mejor rendimiento)
sudo apt-get install -y openjdk-11-jdk
sudo apt-get install -y openjdk-17-jdk
sudo apt-get install -y openjdk-21-jdk

# Verificar instalación
java -version
```

### 2. **Chrome/Chromium (REQUERIDO para headless)**
```bash
# Opción 1: Instalar Chromium (más ligero)
sudo apt-get install -y chromium-browser

# Opción 2: Instalar Google Chrome
wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
sudo sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
sudo apt-get update
sudo apt-get install -y google-chrome-stable

# Verificar instalación
chromium-browser --version
# o
google-chrome --version
```

### 3. **ChromeDriver (REQUERIDO)**
```bash
# Descargar ChromeDriver que coincida con tu versión de Chrome
# Verificar versión de Chrome:
chromium-browser --version

# Descargar desde: https://chromedriver.chromium.org/
# Ejemplo para Chrome 148:
wget https://edgedl.me/chromedriver/LATEST_RELEASE_148
# Descomprimir y mover a /usr/local/bin/
unzip chromedriver_linux64.zip
sudo mv chromedriver /usr/local/bin/
sudo chmod +x /usr/local/bin/chromedriver

# Verificar instalación
chromedriver --version
```

### 4. **Dependencias de sistema**
```bash
sudo apt-get install -y build-essential
sudo apt-get install -y curl wget git
sudo apt-get install -y xvfb libxss1  # Para display virtual (opcional)
sudo apt-get install -y fonts-liberation xdg-utils  # Para Chrome

# Para PowerShell (si necesitas generar reportes con scripts PS1)
sudo apt-get install -y powershell
```

---

## 🔧 CONFIGURACIÓN DEL PROYECTO

### 1. **Clonar/Descargar proyecto**
```bash
git clone https://github.com/sergio129/Sara3.git
cd Sara3
```

### 2. **Configurar permisos**
```bash
chmod +x gradlew
chmod +x run_tests.sh
chmod +x *.sh 2>/dev/null || true
```

### 3. **Configurar variables de entorno (opcional)**
```bash
# ~/.bashrc o ~/.bash_profile
export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export CHROME_BIN="/usr/bin/chromium-browser"  # o /usr/bin/google-chrome
export PATH="$JAVA_HOME/bin:$PATH"

# Aplicar cambios
source ~/.bashrc
```

---

## ▶️ EJECUCIÓN EN LINUX

### **Opción 1: Usar script run_tests.sh (RECOMENDADO)**
```bash
# Dar permisos de ejecución
chmod +x run_tests.sh

# Ejecutar script interactivo
./run_tests.sh

# Seleccionar opción del menú:
# 1. Numero personalizado de runners
# 2. 2 runners en paralelo
# 3. 4 runners en paralelo
# ... etc
```

### **Opción 2: Ejecutar tests directamente con Gradle**
```bash
# 1 test (debug)
./gradlew test --tests "com.sara.automation.runners.CasesRunner01"

# 2 tests en paralelo
sed -i 's/^maxParallelForks=.*/maxParallelForks=2/' gradle.properties
./gradlew test --parallel

# 4 tests en paralelo
sed -i 's/^maxParallelForks=.*/maxParallelForks=4/' gradle.properties
./gradlew test --parallel

# 50 tests en paralelo
sed -i 's/^maxParallelForks=.*/maxParallelForks=50/' gradle.properties
./gradlew test --parallel
```

### **Opción 3: Ejecutar en segundo plano (para CI/CD)**
```bash
# Ejecutar en background y guardar logs
nohup ./gradlew test --parallel > test_execution.log 2>&1 &

# Monitorear progreso
tail -f test_execution.log

# Obtener PID del proceso
ps aux | grep gradlew

# Matar proceso si es necesario
kill -9 <PID>
```

---

## 🌐 CONFIGURACIÓN HEADLESS

### **Variables de entorno para headless**
```bash
export DISPLAY=""                    # Sin display
export QT_QPA_PLATFORM="offscreen"   # Qt en modo offscreen
export CHROME_HEADLESS="--headless"  # Chrome headless

# Opciones Chrome headless (configuradas en serenity.properties):
# --headless              : Sin interfaz gráfica
# --no-sandbox            : Permitir ejecución en contenedores
# --disable-dev-shm-usage : Evitar problemas de memoria
# --disable-gpu           : Desactivar GPU (para VMs)
# --start-maximized       : Abrir maximizado
```

### **Alternativa: Display Virtual (xvfb)**
Si necesitas un display virtual:
```bash
# Instalar xvfb
sudo apt-get install -y xvfb

# Ejecutar tests con display virtual
xvfb-run -a ./gradlew test --parallel

# O con display específico:
Xvfb :99 -screen 0 1024x768x24 &
export DISPLAY=:99
./gradlew test --parallel
```

---

## 📊 GENERAR REPORTES EN LINUX

### **Después de ejecutar tests**
```bash
# Los reportes se generan automáticamente en:
# target/site/serenity/index.html      (Reporte HTML)
# target/reports/step_details_*.xlsx   (Excel - si PowerShell disponible)
# target/reports/step_details_*.csv    (CSV)
# target/reports/step_details_*.html   (HTML)

# Ver reporte HTML desde terminal:
# 1. Copiar URL desde terminal
# 2. Abrir en navegador: http://servidor:puerto/ruta/archivo.html

# O acceder por SCP:
scp usuario@servidor:/ruta/Sara3/target/site/serenity/index.html .
```

---

## 🚀 EJEMPLO: EJECUCIÓN EN CI/CD (GitLab CI / GitHub Actions)

### **GitLab CI (.gitlab-ci.yml)**
```yaml
test:
  image: openjdk:11-jdk
  services:
    - docker:dind
  before_script:
    - apt-get update
    - apt-get install -y chromium-browser chromium-chromedriver git
    - export CHROME_BIN=/usr/bin/chromium-browser
    - chmod +x gradlew
  script:
    - sed -i 's/^maxParallelForks=.*/maxParallelForks=4/' gradle.properties
    - ./gradlew test --parallel
  artifacts:
    paths:
      - target/site/serenity/
      - target/reports/
    reports:
      junit: build/test-results/test/**/*.xml
```

### **GitHub Actions (.github/workflows/test.yml)**
```yaml
name: SARA3 Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Install Chrome
        run: |
          sudo apt-get update
          sudo apt-get install -y chromium-browser chromium-chromedriver
      - name: Run tests
        run: |
          export CHROME_BIN=/usr/bin/chromium-browser
          chmod +x gradlew
          sed -i 's/^maxParallelForks=.*/maxParallelForks=4/' gradle.properties
          ./gradlew test --parallel
      - name: Upload reports
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: serenity-reports
          path: target/site/serenity/
```

---

## 🔍 SOLUCIÓN DE PROBLEMAS

### **Error: "chrome: not found"**
```bash
# Solución: Instalar Chrome/Chromium
sudo apt-get install -y chromium-browser
# o
sudo apt-get install -y google-chrome-stable
```

### **Error: "java: not found"**
```bash
# Solución: Instalar Java
sudo apt-get install -y openjdk-8-jdk
# Verificar JAVA_HOME
echo $JAVA_HOME
```

### **Error: "Permission denied" en gradlew**
```bash
# Solución: Dar permisos
chmod +x gradlew
chmod +x run_tests.sh
```

### **Error: "Timeout waiting for driver server to start"**
```bash
# Problema: ChromeDriver no coincide con Chrome
# Solución: Verificar versiones
chromium-browser --version
chromedriver --version

# Descargar versión correcta:
# https://chromedriver.chromium.org/
```

### **Error: "Failed to start headless shell"**
```bash
# Problema: Sandbox issues
# Solución: Ya configurado en serenity.properties (--no-sandbox)
# Verificar que está presente:
grep "no-sandbox" serenity.properties

# Si no está, agregar manualmente
```

### **Bajo rendimiento / Tests lentos**
```bash
# Verificar recursos disponibles
free -h          # Memoria
df -h            # Disco
top -b -n 1      # CPU

# Reducir paralelismo
sed -i 's/^maxParallelForks=.*/maxParallelForks=2/' gradle.properties
```

---

## 📈 MONITOREO Y DEBUGGING

### **Monitorear ejecución en tiempo real**
```bash
# Terminal 1: Ejecutar tests
./gradlew test --parallel

# Terminal 2: Monitorear procesos
watch -n 1 'ps aux | grep -E "(java|chrome|gradlew)"'

# Terminal 3: Monitorear recursos
watch -n 1 'free -h && echo "---" && df -h'
```

### **Ver logs detallados**
```bash
# Con más verbosidad
./gradlew test --parallel --info

# Con stack traces completos
./gradlew test --parallel --debug > test_debug.log 2>&1

# Filtrar logs de Chrome
./gradlew test --parallel 2>&1 | grep -i chrome
```

### **Generar reporte de performance**
```bash
# Durante/después de tests
ls -lh target/reports/
ls -lh target/site/serenity/

# Copiar reportes a máquina local
scp -r usuario@servidor:/ruta/Sara3/target/site/serenity/ ./reports/
```

---

## 📝 DIFERENCIAS WINDOWS vs LINUX

| Aspecto | Windows | Linux |
|--------|---------|-------|
| Script | `run_tests.bat` | `run_tests.sh` |
| Gradlew | `gradlew.bat` | `./gradlew` |
| Path Java | `C:\Program Files\...` | `/usr/lib/jvm/...` |
| Chrome path | Auto-detectado | `/usr/bin/chromium-browser` |
| Display | GUI | Headless (DISPLAY="") |
| Reportes | Excel abre con `start` | Copiar vía SCP |
| Shell | Batch/PowerShell | Bash |

---

## ✅ VERIFICACIÓN FINAL

```bash
# Checklist de verificación:
java -version                    # ✅ Java instalado
chromium-browser --version       # ✅ Chrome instalado
chromedriver --version           # ✅ ChromeDriver instalado
./gradlew -version               # ✅ Gradle OK
grep "headless" serenity.properties   # ✅ Configuración headless

# Ejecutar test simple
chmod +x ./gradlew
chmod +x ./run_tests.sh
./gradlew test --tests "com.sara.automation.runners.CasesRunner01"
```

---

## 🆘 SOPORTE

Si encuentras problemas:
1. Verifica que cumples los REQUISITOS PREVIOS
2. Revisa SOLUCIÓN DE PROBLEMAS arriba
3. Ejecuta con `--debug` para ver logs completos
4. Verifica que `serenity.properties` tiene `--headless` en `chrome.switches`




