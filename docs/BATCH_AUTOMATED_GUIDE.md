# 🤖 GUÍA - BATCH AUTOMATIZADO 8-PARALELO

## Descripción
Script `batch_test_8p.sh` ejecuta automáticamente 8 tests en paralelo y genera reportes CSV, optimizado para tareas programadas (cron, scheduler).

---

## ⚙️ CONFIGURACIÓN RÁPIDA

### 1. **Dar permisos de ejecución**
```bash
chmod +x batch_test_8p.sh
```

### 2. **Ejecutar manualmente (test)**
```bash
./batch_test_8p.sh
```

### 3. **Programar con cron (tareas automáticas)**

#### **Editar crontab**
```bash
crontab -e
```

#### **Ejemplos de programación**

**Diariamente a las 2 AM:**
```bash
0 2 * * * cd /ruta/Sara3 && ./batch_test_8p.sh
```

**Cada día laboral (Lunes-Viernes) a las 3 AM:**
```bash
0 3 * * 1-5 cd /ruta/Sara3 && ./batch_test_8p.sh
```

**Cada 6 horas:**
```bash
0 */6 * * * cd /ruta/Sara3 && ./batch_test_8p.sh
```

**Cada semana (domingo a las 4 AM):**
```bash
0 4 * * 0 cd /ruta/Sara3 && ./batch_test_8p.sh
```

**Cada primera del mes a las 1 AM:**
```bash
0 1 1 * * cd /ruta/Sara3 && ./batch_test_8p.sh
```

---

## 📊 ESTRUCTURA DE SALIDA

```
Sara3/
├── logs/
│   ├── batch_test_20260519_120000.log    ← Logs de ejecución
│   ├── batch_test_20260519_180000.log
│   └── ...
└── target/
    └── reports/
        ├── step_details_*.csv             ← Reportes CSV
        ├── step_details_*.html            ← Reportes HTML
        ├── test_results_20260519_120000.csv
        └── execution_summary_20260519_120000.txt
```

---

## 📝 QUÉ GENERA EL SCRIPT

| Archivo | Descripción |
|---------|-------------|
| `batch_test_YYYYMMDD_HHMMSS.log` | Log completo de ejecución |
| `step_details_*.csv` | Detalles paso a paso en CSV |
| `step_details_*.html` | Detalles paso a paso en HTML |
| `test_results_*.csv` | Resumen de resultados |
| `execution_summary_*.txt` | Resumen ejecutivo legible |
| `target/site/serenity/index.html` | Reporte HTML Serenity (completo) |

---

## 🔔 NOTIFICACIONES OPCIONALES

### **Por Email**
Configura antes de ejecutar:
```bash
export NOTIFICATION_EMAIL="tu@email.com"
./batch_test_8p.sh
```

O en crontab:
```bash
0 2 * * * cd /ruta/Sara3 && NOTIFICATION_EMAIL="tu@email.com" ./batch_test_8p.sh
```

### **Por Webhook (Slack, Discord, etc.)**
```bash
export WEBHOOK_URL="https://hooks.slack.com/services/..."
./batch_test_8p.sh
```

O en crontab:
```bash
0 2 * * * cd /ruta/Sara3 && WEBHOOK_URL="https://..." ./batch_test_8p.sh
```

---

## 🔍 MONITOREAR EJECUCIONES

### **Ver último log**
```bash
tail -f logs/batch_test_*.log | tail -1
```

### **Ver resumen**
```bash
cat target/reports/execution_summary_*.txt | tail -1
```

### **Listar todos los reportes generados**
```bash
ls -lh target/reports/
```

### **Contar ejecuciones del mes**
```bash
ls logs/batch_test_*.log | wc -l
```

---

## 🐧 INSTALACIÓN EN LINUX (CI/CD)

### **GitLab CI**
```yaml
batch_tests:
  stage: test
  script:
    - chmod +x batch_test_8p.sh
    - ./batch_test_8p.sh
  artifacts:
    paths:
      - target/reports/
      - logs/
    reports:
      junit: build/test-results/test/**/*.xml
  schedule:
    - cron: "0 2 * * *"  # Diariamente a las 2 AM
```

### **GitHub Actions**
```yaml
name: Batch Tests 8P

on:
  schedule:
    - cron: "0 2 * * *"  # Diariamente a las 2 AM

jobs:
  batch_tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Install Chrome
        run: sudo apt-get install -y chromium-browser chromium-chromedriver
      - name: Run batch tests
        run: |
          chmod +x batch_test_8p.sh
          ./batch_test_8p.sh
      - name: Upload reports
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: batch-reports
          path: target/reports/
```

---

## 📋 CARACTERÍSTICAS DEL SCRIPT

✅ **Automático**
- Sin menú interactivo
- Se ejecuta directamente

✅ **Robusto**
- Validación de entorno (Java, Chrome, ChromeDriver)
- Manejo de errores
- Logging completo

✅ **Reportes CSV**
- Reportes en CSV para facilitar análisis
- También genera HTML para visualización
- Reporte ejecutivo legible

✅ **Limpieza automática**
- Elimina logs de más de 7 días
- Mantiene directorio limpio

✅ **Idempotente**
- Se puede ejecutar múltiples veces sin problemas
- Maneja fallos de forma elegante

✅ **Notificaciones**
- Soporte para email (opcional)
- Soporte para webhooks (Slack, Discord, etc.)

✅ **Escalable**
- Apto para cron jobs
- Compatible con CI/CD
- Genera reportes consistentes

---

## 🚀 FLUJO COMPLETO

```
1. Script inicia
   ↓
2. Valida entorno (Java, Chrome, ChromeDriver)
   ↓
3. Configura variables headless
   ↓
4. Limpia builds anteriores
   ↓
5. Compila proyecto
   ↓
6. Configura 8 runners en paralelo
   ↓
7. Ejecuta tests
   ↓
8. Genera reportes CSV
   ↓
9. Genera resumen ejecutivo
   ↓
10. Valida reportes creados
   ↓
11. Envía notificaciones (si está configurado)
   ↓
12. Limpia logs antiguos
   ↓
13. Finaliza con éxito
```

---

## 🆘 TROUBLESHOOTING

### **Error: "Java no encontrado"**
```bash
# Solución: Instalar Java
sudo apt-get install openjdk-8-jdk

# O especificar en cron:
0 2 * * * export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 && cd /ruta/Sara3 && ./batch_test_8p.sh
```

### **Error: "Chrome no encontrado"**
```bash
# Solución: Instalar Chrome
sudo apt-get install chromium-browser

# Verificar ruta:
which chromium-browser
```

### **Error: "Permission denied"**
```bash
# Solución: Dar permisos
chmod +x batch_test_8p.sh
chmod +x gradlew
```

### **Tests no se ejecutan en cron**
```bash
# Problema: Paths relativos en cron
# Solución: Usar rutas absolutas en cron

# INCORRECTO:
0 2 * * * ./batch_test_8p.sh

# CORRECTO:
0 2 * * * /home/usuario/Sara3/batch_test_8p.sh
```

### **No se generan reportes**
```bash
# Verificar logs:
tail -f logs/batch_test_*.log

# Ver último log completo:
cat logs/batch_test_*.log | tail -100
```

---

## 📈 MONITOREO EN PRODUCCIÓN

### **Ver estado de cron jobs**
```bash
grep CRON /var/log/syslog | tail -20
```

### **Ver logs del sistema**
```bash
journalctl -u cron -n 20
```

### **Crear alerta si falla**
```bash
# Script wrapper con notificación de error
#!/bin/bash
if ! /home/usuario/Sara3/batch_test_8p.sh; then
    echo "❌ SARA3 Batch Test FALLÓ" | mail -s "ALERT: Sara3 Tests Failed" admin@empresa.com
fi
```

---

## 💡 CASOS DE USO

### 1. **Testing nocturno**
```bash
0 23 * * * /ruta/Sara3/batch_test_8p.sh
```

### 2. **Validación diaria de cambios**
```bash
0 9 * * 1-5 /ruta/Sara3/batch_test_8p.sh  # Lunes-Viernes a las 9 AM
```

### 3. **Monitoreo de disponibilidad**
```bash
*/30 * * * * /ruta/Sara3/batch_test_8p.sh  # Cada 30 minutos
```

### 4. **Reporte semanal**
```bash
0 0 * * 0 /ruta/Sara3/batch_test_8p.sh  # Cada domingo a medianoche
```

### 5. **Testing en container**
```bash
docker run -d \
  -v /ruta/Sara3:/app \
  openjdk:8 \
  bash -c "cd /app && chmod +x batch_test_8p.sh && ./batch_test_8p.sh"
```

---

## 📊 ANÁLISIS DE REPORTES

### **Script para resumir reportes CSV**
```bash
#!/bin/bash
# summarize_reports.sh

echo "RESUMEN DE EJECUCIONES BATCH (últimos 7 días)"
echo "=============================================="

for log in logs/batch_test_*.log; do
    if [ -f "$log" ]; then
        timestamp=$(basename "$log" | sed 's/batch_test_//;s/.log//')
        passed=$(grep -c "SUCCESS" "$log" || echo 0)
        failed=$(grep -c "ERROR\|FAILURE" "$log" || echo 0)
        
        echo "$(date -d "$timestamp" '+%Y-%m-%d %H:%M'): Passed=$passed, Failed=$failed"
    fi
done
```

---

## 🔐 SEGURIDAD

### **Asegurar logs con permisos**
```bash
chmod 600 logs/batch_test_*.log
chown usuario:usuario logs/
```

### **Usar credenciales seguras en cron**
```bash
# NO hacer esto (expone credenciales):
0 2 * * * cd /ruta && EMAIL_PASSWORD=123456 ./batch_test_8p.sh

# Hacer esto (usar .env o variables del sistema):
0 2 * * * source /home/usuario/.sara3env && cd /ruta && ./batch_test_8p.sh
```

---

## ✅ CHECKLIST

```
□ Script creado y con permisos: chmod +x batch_test_8p.sh
□ Prueba manual exitosa: ./batch_test_8p.sh
□ Java instalado: java -version
□ Chrome instalado: chromium-browser --version
□ ChromeDriver instalado: chromedriver --version
□ Crontab configurado: crontab -l | grep batch_test
□ Logs generándose: ls logs/batch_test_*.log
□ Reportes CSV creándose: ls target/reports/step_details_*.csv
□ Email configurado (opcional): NOTIFICATION_EMAIL=...
□ Webhook configurado (opcional): WEBHOOK_URL=...
```

---

## 📞 SOPORTE

Para más detalles, ver:
- `LINUX_HEADLESS_SETUP.md` - Configuración completa
- `IMPLEMENTATION_SUMMARY_LINUX.md` - Resumen de implementación
- Logs en `logs/` - Registro detallado de ejecuciones




