# 🐧 LINUX DEPLOYMENT - GUÍA RÁPIDA

## ✅ Checklist Pre-Requisitos Linux

```bash
# 1. Instala dependencias (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install -y \
  openjdk-21-jdk \
  google-chrome-stable \
  git \
  curl

# 2. Verifica instalaciones
java -version                    # Debe mostrar Java 21
google-chrome --version          # Debe mostrar Chrome
which chromedriver               # ChromeDriver (si no está, se descarga automáticamente)

# 3. Clona y prepara el proyecto
git clone https://github.com/sergio129/Sara3.git
cd Sara3
chmod +x run-tests-linux.sh      # Hace el script ejecutable
```

---

## 🚀 Ejecución en Linux (3 opciones)

### **OPCIÓN 1: MÁS SIMPLE - Headless automático** ⭐ RECOMENDADO
```bash
# El proyecto ya está configurado con --headless en serenity.properties
# Simplemente ejecuta:
./run-tests-linux.sh

# O especifica un runner diferente:
./run-tests-linux.sh "com.natura.automation.runners.CasesRunner15"

# Para todas las pruebas:
./run-tests-linux.sh "com.natura.automation.runners.*"
```

**Ventajas:**
- ✓ No requiere servidor gráfico virtual (Xvfb)
- ✓ Más rápido (menos overhead)
- ✓ Funciona en contenedores Docker
- ✓ Consumo de memoria reducido

---

### **OPCIÓN 2: CON SERVIDOR GRÁFICO VIRTUAL (Si necesitas UI visual)**
```bash
# Instala Xvfb
sudo apt-get install -y xvfb

# Inicia Xvfb (en background)
export DISPLAY=:99
Xvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &
XVFB_PID=$!

# Ejecuta los tests
./gradlew clean test --tests "com.natura.automation.runners.CasesRunner15"

# Limpia
kill $XVFB_PID
```

**Ventajas:**
- ✓ Ve la ejecución en video/screenshots
- ✓ Debug visual más fácil

**Desventajas:**
- ✗ Más lento
- ✗ Requiere más recursos

---

### **OPCIÓN 3: EN DOCKER** (Mejor para CI/CD)
```bash
# Usa esta imagen (ya tiene todo instalado):
docker run --rm \
  -v $(pwd):/automation \
  -w /automation \
  openjdk:21-jdk \
  bash -c "apt-get update && apt-get install -y google-chrome-stable && ./gradlew clean test"
```

---

## 📊 Comparación: Windows vs Linux

| Característica | Windows | Linux |
|---|---|---|
| Ejecución | `run-tests-windows.bat` | `./run-tests-linux.sh` |
| Headless | ✓ Sí (--headless en properties) | ✓ Sí (automático) |
| Server Gráfico | ✓ GUI nativa | ✗ Requiere Xvfb (opcional) |
| Velocidad | Normal | Más rápido |
| Memoria | Normal | Menos |
| Producción | OK | ⭐ Recomendado |

---

## ⚙️ Configuración Actual (Ya Hecha)

✅ **serenity.properties**:
```properties
chrome.switches =--headless;--start-maximized;--remote-allow-origins=*;--disable-dev-shm-usage;--no-sandbox;--disable-gpu
```

Las opciones `--disable-dev-shm-usage` y `--no-sandbox` son CRÍTICAS para Linux.

---

## 🔍 Verificación Post-Ejecución

Después de ejecutar los tests, verifica:

```bash
# Ver reporte HTML
open target/site/serenity/index.html

# O en Linux:
firefox target/site/serenity/index.html &

# Ver logs
cat target/serenity.log

# Verificar archivos generados
ls -la target/serenity/
```

---

## 🐛 Troubleshooting Linux

| Error | Solución |
|---|---|
| `Chrome crashed` | Instala: `sudo apt-get install libglib2.0-0 libx11-6` |
| `No display` | Ya está configurado headless, no necesitas display |
| `Permission denied` | `chmod +x run-tests-linux.sh` |
| `Port already in use` | Ejecuta: `./gradlew clean` primero |
| `Out of memory` | Aumenta: `export GRADLE_OPTS="-Xmx4g"` |
| `Chrome not found` | `sudo apt-get install google-chrome-stable` |

---

## 📝 Variables de Entorno (Opcional)

```bash
# En ~/.bashrc o antes de ejecutar:
export GRADLE_OPTS="-Xmx2g -Xms512m"          # Memory para Gradle
export JAVA_TOOL_OPTIONS="-Xmx2g -Xms512m"   # Memory para Java
export CHROME_DRIVER_PATH=/usr/bin/chromedriver  # Path a ChromeDriver (si no está en PATH)
export HEADLESS_MODE=true                      # Modo headless explícito
```

---

## 🔗 CI/CD Integration

### GitHub Actions Example:
```yaml
name: Sara3 Automation - Linux

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Install Chrome
        run: |
          sudo apt-get update
          sudo apt-get install -y google-chrome-stable
      - name: Run Tests
        run: |
          chmod +x run-tests-linux.sh
          ./run-tests-linux.sh
      - name: Upload Reports
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: serenity-reports
          path: target/site/serenity/
```

---

## ✨ Resumen

**Para ejecutar en Linux:**

1. **Instala**: Java 21 + Chrome
2. **Ejecuta**: `./run-tests-linux.sh`
3. **Espera**: Los tests se ejecutan en modo headless
4. **Verifica**: Abre `target/site/serenity/index.html`

¡Eso es! Linux ahora funciona igual que Windows.




