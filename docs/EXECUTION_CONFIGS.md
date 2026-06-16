# EXECUTION CONFIGURATIONS - Sara3 Performance Testing

> Diferentes configuraciones según necesidad de carga paralela

---

## 📋 Configuraciones Predefinidas

### **Configuration 1: Minimal Test (2 Machines × 2 Tests = 4 Parallel)**
```batch
REM Minimal parallel testing - fast, small sample
gradle test -Dcucumber.filter.tags="@batch1 or @batch2" -DmaxParallelForks=2
.\script/generate_app_performance_report.ps1
echo "✓ Minimal test completed - check target\reports\app_performance_report_*.xlsx"
```

**Cuándo usar:** Pruebas rápidas de validación, verificar que la instrumentación funciona

**Tiempo estimado:** 5-8 minutos por máquina
**Total paralelos:** 4 tests
**Reportes esperados:** 4 CSVs en target/app_performance_logs/

---

### **Configuration 2: Small Load (3 Machines × 3 Tests = 9 Parallel)**
```batch
REM Small load test - quick validation
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3" -DmaxParallelForks=3
.\script/generate_app_performance_report.ps1
echo "✓ Small load test completed - check target\reports\app_performance_report_*.xlsx"
```

**Cuándo usar:** Pruebas iniciales, validar comportamiento bajo carga ligera

**Tiempo estimado:** 8-12 minutos por máquina
**Total paralelos:** 9 tests
**Reportes esperados:** 9 CSVs con métricas de app

---

### **Configuration 3: Medium Load (5 Machines × 4 Tests = 20 Parallel)**
```batch
REM Medium load test - balanced coverage
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3 or @batch4" -DmaxParallelForks=4
.\script/generate_app_performance_report.ps1
echo "✓ Medium load test completed - check target\reports\app_performance_report_*.xlsx"
```

**Cuándo usar:** Pruebas regulares, validación de baseline de performance

**Tiempo estimado:** 10-15 minutos por máquina
**Total paralelos:** 20 tests
**Reportes esperados:** 20 CSVs + Excel dashboard

---

### **Configuration 4: Large Load (8 Machines × 4 Tests = 32 Parallel)**
```batch
REM Large load test - significant stress
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3 or @batch4" -DmaxParallelForks=4
.\script/generate_app_performance_report.ps1
echo "✓ Large load test completed - check target\reports\app_performance_report_*.xlsx"
```

**Cuándo usar:** Testing de carga significativa, validar scalability

**Tiempo estimado:** 10-15 minutos por máquina (x 8 máquinas)
**Total paralelos:** 32 tests
**Reportes esperados:** 32 CSVs + Excel analysis

---

### **Configuration 5: Heavy Load (10 Machines × 5 Tests = 50 Parallel)**
```batch
REM Heavy load test - maximum stress
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3 or @batch4 or @batch5" -DmaxParallelForks=5
.\script/generate_app_performance_report.ps1
echo "✓ Heavy load test completed - check target\reports\app_performance_report_*.xlsx"
```

**Cuándo usar:** Pruebas finales, validar app bajo carga máxima

**Tiempo estimado:** 12-18 minutos por máquina (x 10 máquinas)
**Total paralelos:** 50 tests
**Reportes esperados:** 50 CSVs + comprehensive Excel report

---

## 🔧 Personalización

### Para cualquier configuración custom: **N máquinas × M tests**

**Formula:**
```batch
# Ejecutar M tests en paralelo
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or ... or @batchM" -DmaxParallelForks=M

# Luego: generar reporte
.\script/generate_app_performance_report.ps1
```

**Ejemplos:**
```batch
# 6 tests en paralelo
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3 or @batch4 or @batch5 or @batch6" -DmaxParallelForks=6

# 7 tests en paralelo
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or @batch3 or @batch4 or @batch5 or @batch6 or @batch7" -DmaxParallelForks=7

# 12 tests en paralelo (si tienes 50 scenarios)
gradle test -Dcucumber.filter.tags="@batch1 or @batch2 or ... or @batch12" -DmaxParallelForks=12
```

---

## 📊 Interpretación de Reportes

Después de ejecutar cualquier configuración:

```powershell
# Abrir reporte
start target\reports\app_performance_report_*.xlsx
```

**Revisar en orden:**

1. **Hoja 1: Summary** → ¿App responde bien? (Status: ✓ OK o ⚠ WARNING)
2. **Hoja 2: Network Timing** → ¿Endpoints son rápidos? (Degradation % bajo 80%?)
3. **Hoja 3: Web Vitals** → ¿Frontend degrada sin mucho? (< 30% degradation?)
4. **Hoja 4: Bottleneck** → ¿Cuál es el componente más lento?
5. **Hoja 5: Load Degradation Curve** → ¿Scalability >= 50%?

---

## 🚀 Workflow Recomendado (Semana)

**Lunes - Validación:**
```batch
Config 1 (4 paralelos) - Quick sanity check
```

**Martes-Miércoles - Baseline:**
```batch
Config 3 (20 paralelos) - Capture baseline performance
Config 4 (32 paralelos) - Validate consistency
```

**Jueves-Viernes - Final:**
```batch
Config 5 (50 paralelos) - Maximum stress test
```

---

## 📈 Key Metrics to Track Across Runs

Comparar entre configuraciones:

| Config | Paralelos | Avg Response | Scalability Index | Error Rate | Status |
|--------|-----------|--------------|-------------------|-----------|--------|
| Config 1 | 4 | 1.5s | 100% | 0% | Baseline |
| Config 2 | 9 | 1.8s | 83% | 0% | ✓ Good |
| Config 3 | 20 | 2.1s | 71% | 0% | ✓ Good |
| Config 4 | 32 | 2.5s | 60% | 0% | ✓ OK |
| Config 5 | 50 | 2.8s | 54% | 0% | ✓ Acceptable |

**Objetivo:** Mantener Scalability >= 50% incluso en Config 5

---

## ⚠️ If Performance Degrades

**Síntomas a monitorer:**

1. **Degradation > 100%** (API tarda el doble o más)
   - Problema: Backend bajo carga, DB connection pool agotado
   - Acción: Optimizar queries, aumentar connection pool

2. **Scalability Index < 50%**
   - Problema: App no escala bien
   - Acción: Horizontal scaling, async processing, caching

3. **Error Rate > 0.5%**
   - Problema: App falla bajo carga
   - Acción: Investigar 502/503/504 errors, aumentar resources

4. **Web Vitals degradan > 40%**
   - Problema: Frontend rendering lento bajo carga
   - Acción: Optimize JavaScript, lazy load, virtualize lists

---

## 📞 Quick Reference Commands

```powershell
# Run specific config
.\execute_config.ps1 -config "medium"  # Ejecutar config Medium (20 paralelos)

# Generate report
.\script/generate_app_performance_report.ps1 -appPerfLogsPath "target/app_performance_logs"

# Compare two runs
.\compare_performance_runs.ps1 -run1 "app_performance_report_20260511_143022.xlsx" -run2 "app_performance_report_20260512_153022.xlsx"

# Clean old reports
.\clean_reports.bat
```



