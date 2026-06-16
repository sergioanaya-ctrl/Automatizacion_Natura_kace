# 📈 Comparación: 1 Test vs 50 Tests Consolidados

## Reporte ACTUAL (1 Test Ejecutado)

```
Archivo: step_details_20260511_105536.xlsx
Tamaño: 11.7 KB
Ubicación: target\reports\

┌─────────────────────────────────────────────┐
│ Hoja 1: RESUMEN                             │
├─────────────────────────────────────────────┤
│ Total Tests:              1                 │
│ Total Pasos:              41                │
│ Pasos Lentos (>5s):       17                │
│ Duración Total:           2,5 minutos       │
│ Test Analizado:           Usuario 25        │
│                           AMAZONAS-LETICIA  │
│                           AUTOS-MANTENIMIENTO
│ Porcentaje Pasos Lentos:  41.5%             │
└─────────────────────────────────────────────┘

Hoja 2: TODOS LOS PASOS (41 filas)
├─ Usuario 25 - AMAZONAS - LETICIA - AUTOS - MANTENIMIENTO PREVENTIVO
│  ├─ Paso 1: Abre página (45 ms)
│  ├─ Paso 2: Completa email (125 ms)
│  ├─ Paso 3: Espera carga (5,234 ms) ⚠️ LENTO
│  ├─ Paso 4: Click en botón (156 ms)
│  └─ ... 37 pasos más
└─ Total: 41 pasos

Hoja 3: PASOS LENTOS >5s (17 filas)
├─ Paso 3: "Wait for navigation"        5,234 ms
├─ Paso 7: "Upload document"            6,456 ms
├─ Paso 12: "Process request"           7,123 ms
├─ Paso 18: "Page load complete"        5,890 ms
└─ ... 13 pasos más

Hoja 4: ESTADÍSTICAS POR TEST (1 fila)
├─ Test: Usuario 25 - AMAZONAS...
├─ Total Pasos: 41
├─ Pasos Lentos: 17
├─ Tiempo Total: 2,5 min
└─ % Lentos: 41.5%
```

---

## 📊 Proyección: Con 50 Tests Ejecutados en Paralelo

```
Archivo: step_details_20260512_084500.xlsx
Tamaño ESTIMADO: 1.5 MB
Ubicación: target\reports\

┌─────────────────────────────────────────────┐
│ Hoja 1: RESUMEN                             │
├─────────────────────────────────────────────┤
│ Total Tests:              50                │
│ Total Pasos:              2,050 (50×41)     │
│ Pasos Lentos (>5s):       ~850 (41%)        │
│ Duración Total Promedio:  2,5 min por test  │
│ Duración Total Paralelo:  ~3 minutos        │
│ Test Más Lento:           CasesRunner15     │
│                           (2,9 min)         │
│ Test Más Rápido:          CasesRunner08     │
│                           (2,1 min)         │
│ Porcentaje Pasos Lentos:  41.5% (promedio)  │
└─────────────────────────────────────────────┘

Hoja 2: TODOS LOS PASOS (2,050 filas)
├─ CasesRunner01 - Usuario 1
│  └─ 41 pasos
├─ CasesRunner02 - Usuario 2
│  └─ 41 pasos
├─ CasesRunner03 - Usuario 3
│  └─ 41 pasos
├─ ... CasesRunner04 a CasesRunner50
│  └─ 41 pasos cada uno
└─ Total: 2,050 pasos

   Muestra de datos:
   ┌─────────────┬──────────────────────┬─────────────┬────────────┬──────────────┐
   │ Test        │ Descripción          │ Accion      │ Elemento   │ Tiempo (ms)  │
   ├─────────────┼──────────────────────┼─────────────┼────────────┼──────────────┤
   │ CasesRunner01 │ Abre página        │ Abre        │ URL        │ 45           │
   │ CasesRunner01 │ Completa email     │ Escribe     │ Input      │ 125          │
   │ CasesRunner01 │ Espera carga       │ Ejecuta     │ Page Load  │ 5,234 ⚠️     │
   │ CasesRunner01 │ Click en botón     │ Click       │ Button     │ 156          │
   │ ...          │ ...                │ ...         │ ...        │ ...          │
   │ CasesRunner02 │ Abre página        │ Abre        │ URL        │ 52           │
   │ CasesRunner02 │ Completa email     │ Escribe     │ Input      │ 132          │
   │ ...          │ ...                │ ...         │ ...        │ ...          │
   │ CasesRunner50 │ Click en botón     │ Click       │ Button     │ 168          │
   └─────────────┴──────────────────────┴─────────────┴────────────┴──────────────┘

Hoja 3: PASOS LENTOS >5s (850 filas - TODOS LOS PASOS CON >5 SEGUNDOS)
├─ CasesRunner22 - "Upload attachment"     8,234 ms ⭐ TOP 1 MÁS LENTO
├─ CasesRunner08 - "Process request"       7,123 ms ⭐ TOP 2
├─ CasesRunner15 - "Upload document"        6,456 ms ⭐ TOP 3
├─ CasesRunner42 - "Page load complete"    5,890 ms
├─ CasesRunner03 - "Wait for navigation"    5,234 ms
├─ CasesRunner01 - "Wait for validation"   5,567 ms
├─ CasesRunner35 - "Submit form"           5,456 ms
├─ ... (843 filas más de pasos >5s, ordenados por tiempo descendente)
└─ TOTAL: 850 filas (sin espacios en blanco)

   ℹ️ NOTA: Solo se muestran pasos con duración > 5 segundos
   ℹ️ Están ordenados de mayor a menor tiempo
   ℹ️ No hay filas vacías: cada fila es un paso real >5s

Hoja 4: ESTADÍSTICAS POR TEST (50 filas)
├─ CasesRunner01 | 41 pasos | 16 lentos | 2,4 min | 39.0%
├─ CasesRunner02 | 41 pasos | 17 lentos | 2,6 min | 41.5%
├─ CasesRunner03 | 41 pasos | 18 lentos | 2,5 min | 43.9%
├─ CasesRunner04 | 41 pasos | 15 lentos | 2,3 min | 36.6%
├─ CasesRunner05 | 41 pasos | 19 lentos | 2,7 min | 46.3%
├─ CasesRunner06 | 41 pasos | 16 lentos | 2,4 min | 39.0%
├─ CasesRunner07 | 41 pasos | 17 lentos | 2,5 min | 41.5%
├─ CasesRunner08 | 41 pasos | 14 lentos | 2,1 min | 34.1% ⭐ MÁS RÁPIDO
├─ CasesRunner09 | 41 pasos | 17 lentos | 2,5 min | 41.5%
├─ CasesRunner10 | 41 pasos | 18 lentos | 2,6 min | 43.9%
├─ ...
├─ CasesRunner15 | 41 pasos | 20 lentos | 2,9 min | 48.8% ⭐ MÁS LENTO
├─ ...
├─ CasesRunner50 | 41 pasos | 17 lentos | 2,5 min | 41.5%
└─ PROMEDIO:     | 41 pasos | 17 lentos | 2,5 min | 41.5%
```

---

## 🔍 Análisis Comparativo

| Métrica | 1 Test | 50 Tests | Incremento |
|---------|--------|----------|-----------|
| **Total Pasos** | 41 | 2,050 | 50x |
| **Pasos Lentos** | 17 | ~850 | 50x |
| **Archivo Excel** | 11.7 KB | ~1.5 MB | 128x |
| **Filas (Hoja 2)** | 41 | 2,050 | 50x |
| **Filas (Hoja 3)** | 17 | ~850 | 50x |
| **Filas (Hoja 4)** | 1 | 50 | 50x |
| **Tiempo Generación** | <1 seg | ~8 seg | 8x |
| **Filtros Posibles** | 1 test | 50 tests | 50x |

---

## 💡 Cómo Analizar 50 Tests en el Excel

### Caso 1: Encontrar el Test Más Problemático
```
1. Abre step_details_YYYYMMDD_HHMMSS.xlsx
2. Ve a Hoja 4: "Estadísticas por Test"
3. Ordena columna "Tiempo Total" (Z a A)
4. CasesRunner15 está arriba → Toma 2,9 min (22% más que el promedio)
5. Abre Hoja 3 (Pasos Lentos) y filtra por "CasesRunner15"
6. Ves que: "Upload document" toma 6,456 ms (8.2 seg del total)
7. CONCLUSIÓN: El bottleneck es la carga de documento en Test 15
```

### Caso 2: Comparar Distribución de Acciones
```
1. Abre Hoja 2: "Todos los Pasos"
2. Filtra por Acción = "Escribe"
3. Ves: 50 tests × ~5 escrituras = ~250 "Escribe"
4. Tiempo promedio de escritura: 125 ms
5. Detectas que CasesRunner22 tiene escrituras de 456 ms (3x más lento)
6. CONCLUSIÓN: Hay un problema con los inputs en Test 22
```

### Caso 3: Identificar Pasos Problemáticos Globales
```
1. Abre Hoja 3: "Pasos Lentos" (850 filas)
2. Ordena por "Tiempo (ms)" (descendente)
3. Top 5 pasos más lentos:
   - Upload document: 8,234 ms (Test 22)
   - Page load: 7,456 ms (Test 3)
   - Submit form: 7,123 ms (Test 8)
   - Wait for validation: 6,890 ms (Test 15)
   - Process request: 6,456 ms (Test 15)
4. CONCLUSIÓN: "Upload document" y "Page load" necesitan optimización
```

### Caso 4: Verificar Consistencia
```
1. Abre Hoja 4: "Estadísticas por Test"
2. Analiza columna "% Pasos Lentos"
3. Rango: 34.1% (Test 8) a 48.8% (Test 15)
4. Desviación estándar: ~4.2%
5. CONCLUSIÓN: Consistencia aceptable, pero Test 15 es outlier
```

---

## 🎯 Ventajas de Consolidación con 50 Tests

✅ **Una única fuente de verdad** - Todo en un archivo  
✅ **Identificar outliers** - Detectar tests anormalmente lentos  
✅ **Análisis de tendencias** - Ver patrones en 50 ejecuciones  
✅ **Filtrado avanzado** - Excel AutoFilter en todas las hojas  
✅ **Fácil comparación** - Compara Test 1 vs Test 50 lado a lado  
✅ **Generación automática** - Se crea al finalizar la ejecución  
✅ **Reporte histórico** - Guarda timestamp en el nombre del archivo  

---

## 📅 Historiales de Reportes

```
target/reports/
├─ step_details_20260511_102716.xlsx (1 test)
├─ step_details_20260511_104714.xlsx (1 test)
├─ step_details_20260511_105536.xlsx (1 test - ACTUAL)
├─ step_details_20260512_084500.xlsx (50 tests - futuro)
└─ step_details_20260512_153015.xlsx (50 tests - futuro)
```

Cada archivo es **independiente** y contiene la consolidación de ESA ejecución específica.

---

## 🚀 Próximos Pasos

Cuando ejecutes 50 tests:

```powershell
# En run_tests.bat, seleccionar opción 6:
# "Ejecutar 50 runners en paralelo"

# Esto automáticamente:
# 1. Ejecuta 50 tests en paralelo (~3 min)
# 2. Genera step_details_YYYYMMDD_HHMMSS.xlsx
# 3. Consolida 2,050 pasos en 4 hojas
# 4. Permite análisis completo de toda la ejecución
```

¡Listo para analizar 50 tests consolidados en un solo archivo! 🎉



