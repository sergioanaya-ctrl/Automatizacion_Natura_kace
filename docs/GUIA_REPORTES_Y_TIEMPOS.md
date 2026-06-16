# GUIA: LIMPEZA DE REPORTES, MEDICION DE TIEMPOS Y SOLUCION DE PARALELISMO

## 1. LIMPEZA AUTOMATICA DE REPORTES

### Antes de ejecutar tests (AUTOMATICO)
A partir de ahora, cuando ejecutas cualquier opcion del menu de `run_tests.bat`, los reportes se limpian automaticamente:

```
1. Ejecutar numero personalizado de runners     -> Limpia reportes
2. Ejecutar  2 runners en paralelo              -> Limpia reportes
3. Ejecutar  4 runners en paralelo              -> Limpia reportes
4. Ejecutar  8 runners en paralelo              -> Limpia reportes
5. Ejecutar 12 runners en paralelo              -> Limpia reportes
6. Ejecutar 50 runners en paralelo              -> Limpia reportes
7. Ejecutar  1 runner individual                -> Limpia reportes
```

### Opcion 10: Limpiar manualmente reportes
Si quieres limpiar los reportes sin ejecutar tests:
```
Selecciona opcion: 10
```

### Carpetas que se limpian
- `target/site/serenity/` - Reportes HTML de Serenity
- `target/test-results/` - Resultados XML de JUnit
- `build/reports/` - Reportes de Gradle

---

## 2. MEDICION DE TIEMPOS DE EJECUCION

### Generar Reporte de Tiempos (Opcion 9)

Despues de ejecutar los tests, genera un reporte detallado:

```
Selecciona opcion: 9
```

Esto genera:

#### A. Archivo CSV: `test_timings_report.csv`
Formato exportable con columnas:
- **Suite** - Nombre del suite de tests
- **Test Name** - Nombre del test
- **Class** - Clase Java del test
- **Duration (s)** - Tiempo en segundos
- **Status** - PASSED, FAILED, SKIPPED
- **Error Message** - Mensaje si fallo

#### B. Archivo XLSX: `test_timings_report.xlsx` (si tienes Excel)
Con 2 hojas:

**Hoja 1: Test Details**
- Todos los tests con sus tiempos
- Codificacion de colores por status
- Ordenados por duracion (mas lentos primero)

**Hoja 2: Summary**
- Estadisticas generales
- Total de tests
- Tests exitosos, fallidos, omitidos
- Tiempo total, promedio, maximo, minimo

#### C. Estadisticas en Consola
```
ESTADISTICAS DE EJECUCION:
========================================================
Total de tests: 50
Tests exitosos: 48
Tests fallidos: 2
Tests omitidos: 0

Tiempo total: 1250.45 segundos
Tiempo promedio: 25.01 segundos
Tiempo maximo: 45.23 segundos (Test mas lento)
Tiempo minimo: 15.67 segundos (Test mas rapido)

TOP 10 TESTS MAS LENTOS:
========================================================
CasesRunner01 - 45.23s - PASSED
CasesRunner02 - 42.15s - PASSED
...
```

### Usar el reporte

**Importar en Excel/Power BI:**
1. Abre `test_timings_report.xlsx` o `test_timings_report.csv`
2. Crea graficos de distribucion de tiempos
3. Identifica tests que se ejecutan lentamente

**Analizar outliers:**
```
Tiempo promedio: 25s
Test lento: 45s (80% mas lento que el promedio)
Investigar: Por que CasesRunner01 tarda tanto?
```

---

## 3. SOLUCION DE PARALELISMO INFINITO

### Cambios implementados

#### A. Limpieza de reportes ANTES de ejecutar
Evita que archivos viejos interfieran con la nueva ejecucion:
```bash
rmdir /s /q target\site\serenity\
rmdir /s /q target\test-results\
rmdir /s /q build\reports\
```

#### B. Flag `--parallel` agregado
Ahora Gradle ejecuta con:
```bash
.\gradlew.bat test --parallel --max-workers=50
```

#### C. Timeout de 10 minutos por test
En `build.gradle`:
```gradle
timeout = 600000  // 10 minutos en milisegundos
```

Si un test no termina en 10 minutos, Gradle lo mata automaticamente.

#### D. Timeouts en serenity.properties
```properties
webdriver.timeouts.implicitlywait = 5000    # 5 segundos espera implicita
serenity.timeout = 10000                    # 10 segundos timeout de Serenity
webdriver.close.driver = true               # Cerrar driver al final
webdriver.quit.driver = true                # Eliminar driver al final
```

#### E. Configuracion mejorada en gradle.properties
```properties
org.gradle.parallel.workers=8
org.gradle.workers.max=50
org.gradle.internal.worker.socket.timeout=120000  # 2 minutos timeout de socket
```

---

## 4. MONITOREO DE PROCESOS EN TIEMPO REAL

Si aun sospechas de procesos colgados, puedes ejecutar el monitor:

```bash
powershell -ExecutionPolicy Bypass -File script/monitor_java_processes.ps1
```

**Caracteristicas:**
- Detecta procesos Java colgados (sin actividad por 10 minutos)
- Los mata automaticamente
- Muestra consumo de CPU y memoria
- Intervalo de verificacion: 30 segundos (configurable)

**Ejemplo:**
```
[+] Nuevo proceso detectado: PID=1234, CPU=15%, Memoria=512MB
[+] Proceso activo: PID=1234, CPU=25%, Memoria=620MB
[~] Proceso inactivo: PID=1234, Tiempo=5.2min (limite: 10 min)
[-] PROCESO COLGADO DETECTADO: PID=1234, Tiempo inactivo=10.5min
    Matando proceso...
    [OK] Proceso matado
```

---

## 5. MEJORES PRACTICAS PARA EJECUCION EN PARALELO

### Para 50 tests en paralelo

1. **Ejecutar en horario fuera de pico:**
   - Evita que otros programas usen CPU/RAM
   - Garantiza recursos suficientes para 50 navegadores

2. **Cerrar programas pesados:**
   - Office, Visual Studio, IDE's
   - Chrome, Firefox, Edge manualmente abiertos
   - Sistemas de sincronizacion (Dropbox, OneDrive)

3. **Verificar recursos antes:**
   - RAM disponible: Minimo 8GB (50 * 1GB por fork)
   - Procesadores: Minimo 8 nucleos
   - Espacio disco: Minimo 10GB

4. **Usar opcion menor si falla:**
   ```
   Falla con 50? -> Intenta con 12
   Falla con 12? -> Intenta con 8
   Falla con 8?  -> Intenta con 4
   ```

5. **Monitorear la primera ejecucion:**
   - Abre script/monitor_java_processes.ps1 en otra ventana
   - Verifica que no haya procesos colgados

---

## 6. EJEMPLO DE FLUJO COMPLETO

```
1. run_tests.bat (START_TESTS.bat)
   ↓
2. Selecciona opcion: 6 (50 runners en paralelo)
   ↓
   [Limpia reportes automaticamente]
   [Ejecuta gradle test con 50 JVM paralelas]
   [Cada JVM abre 1 navegador Chrome]
   [Timeout de 10 minutos por test]
   ↓
3. (Esperar 20-40 minutos segun capacidad)
   ↓
4. Selecciona opcion: 9 (Generar reporte de tiempos)
   ↓
   [Lee XML de resultados]
   [Genera test_timings_report.csv]
   [Genera test_timings_report.xlsx]
   [Abre automaticamente el reporte]
   ↓
5. Analizar:
   - Tiempo total: 1250s (20.8 minutos)
   - Test promedio: 25s
   - Tests mas lentos: CasesRunner01, CasesRunner15
   - Tests fallidos: 2
   ↓
6. Optimizar:
   - Investigar por que CasesRunner01 es lento
   - Reducir timeouts innecesarios
   - Verificar pasos redundantes
```

---

## 7. SOLUCION DE PROBLEMAS

### Error: "La ejecucion tardo demasiado"
```
Solucion:
1. Reduce numero de runners (prueba con 4 en lugar de 50)
2. Verifica conexion a internet
3. Verifica que la aplicacion bajo prueba responde
4. Aumenta RAM en gradle.properties: org.gradle.jvmargs=-Xmx4096m
```

### Error: "No hay memoria"
```
Solucion:
1. Reduce maxParallelForks en gradle.properties
2. Cierra otros programas
3. Limpia reportes viejos: selecciona opcion 10
4. Reinicia la computadora
```

### Algunos tests pasan, otros fallan irregularmente
```
Solucion:
1. Ejecuta los tests que fallan individualmente (opcion 7)
2. Verifica que el servidor este disponible
3. Aumenta timeouts en serenity.properties
4. Verifica pasos que dependan de otros tests
```

### El monitor detecta procesos colgados frecuentemente
```
Solucion:
1. Reduce numero de runners en paralelo
2. Verifica pasos que usen WebDriverWait innecesariamente
3. Revisa logs para ver donde se cuelga
4. Aumenta timeout de serenity.timeout en properties
```

---

## 8. ARCHIVOS NUEVOS/MODIFICADOS

### Nuevos archivos:
- `script/generate_timing_report.ps1` - Genera reportes de tiempos
- `script/monitor_java_processes.ps1` - Monitorea procesos colgados

### Modificados:
- `run_tests.bat` - Agregadas opciones 9 y 10, limpieza automatica
- `build.gradle` - Agregado timeout de 10 minutos
- `gradle.properties` - Agregadas configuraciones de timeout

---

## 9. NOTAS IMPORTANTES

1. **Los reportes se limpian ANTES de ejecutar**, no despues
   - Asegura que los resultados nuevos no se mezclen con viejos

2. **El timeout de 10 minutos es POR TEST**, no total
   - 50 tests * 10 min = 500 minutos maximo (pero normalmente 20-30 minutos)

3. **CSV es exportable a cualquier herramienta**
   - Excel, Power BI, Google Sheets, Tableau

4. **XLSX requiere Excel instalado**
   - Si no tienes Excel, solo se genera CSV

5. **El monitor debe correrse EN OTRA VENTANA POWERSHELL**
   - Abre otra terminal mientras corre run_tests.bat

---

Preguntas? Revisa los logs en:
- `gradle-test-output.txt` - Salida de Gradle
- `test_output.txt` - Salida de tests
- `build/test-results/test/*.xml` - Resultados detallados

¡Listo! Ahora tienes reportes limpios, medicion de tiempos y proteccion contra cuelgues infinitos. 🚀



