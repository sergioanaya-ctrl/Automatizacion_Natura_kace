# 🎯 GUÍA RÁPIDA - Consolidación de Reportes Paso a Paso

## ¿Qué es la Consolidación?

Cuando ejecutas **múltiples tests** (2, 5, 10, 20, 50), el script automáticamente:
1. Lee todos los JSON de cada test ejecutado
2. Extrae los 41 pasos de cada test
3. Los agrupa en **UN único archivo Excel**
4. Genera 4 hojas con análisis consolidado

---

## 📊 Escala según Cantidad de Tests

```
┌──────────┬─────────────┬────────────────┬─────────────────┬──────────────────┐
│ Tests    │ Total Pasos │ Tamaño Excel   │ Filas Hoja 2    │ Filas Hoja 4     │
├──────────┼─────────────┼────────────────┼─────────────────┼──────────────────┤
│ 1        │ 41          │ ~12 KB         │ 41              │ 1 (Resumen)      │
│ 2        │ 82          │ ~22 KB         │ 82              │ 2 (Tests)        │
│ 5        │ 205         │ ~55 KB         │ 205             │ 5 (Tests)        │
│ 10       │ 410         │ ~110 KB        │ 410             │ 10 (Tests)       │
│ 20       │ 820         │ ~220 KB        │ 820             │ 20 (Tests)       │
│ 50       │ 2,050       │ ~550 KB        │ 2,050           │ 50 (Tests)       │
└──────────┴─────────────┴────────────────┴─────────────────┴──────────────────┘
```

---

## 📁 Las 4 Hojas Explicadas

### Hoja 1: RESUMEN (Consolidado Total)
```
Metrica                    | Valor
---------------------------|--------
Total Tests                | 50
Total de Pasos             | 2,050
Pasos Lentos (>5s)         | ~850
Duración Promedio per Test | 2,5 min
Test Más Lento             | CasesRunner15 (2,9 min)
Test Más Rápido            | CasesRunner08 (2,1 min)
```

### Hoja 2: TODOS LOS PASOS (Todos los pasos, filtrable)
```
Test          | Descripcion                | Accion    | Tiempo (ms)
--------------|----------------------------|-----------|-------------
CasesRunner01 | Abre página                | Abre      | 45
CasesRunner01 | Completa email             | Escribe   | 125
CasesRunner01 | Espera carga               | Ejecuta   | 5,234 ⚠️
CasesRunner02 | Abre página                | Abre      | 52
CasesRunner02 | Completa email             | Escribe   | 132
...
CasesRunner50 | Click en botón             | Click     | 168

✨ PUEDES FILTRAR POR:
   • Test (CasesRunner01-50)
   • Acción (Click, Escribe, etc.)
   • Tiempo (para encontrar lentos)
```

### Hoja 3: PASOS LENTOS >5s (Ordenado por tiempo)
```
Test          | Descripcion                | Tiempo (ms) | % del Total
--------------|----------------------------|-------------|----------
CasesRunner22 | Upload document            | 8,234 ms    | 10.0% ⚠️ TOP 1
CasesRunner03 | Wait for navigation        | 7,456 ms    | 9.1%
CasesRunner08 | Process request            | 7,123 ms    | 8.7%
CasesRunner42 | Page load complete         | 5,890 ms    | 7.2%
...
```

### Hoja 4: ESTADÍSTICAS POR TEST (Una fila por test)
```
Test          | Total Pasos | Pasos Lentos | Tiempo Total | % Lentos
--------------|-------------|--------------|--------------|----------
CasesRunner01 | 41          | 16           | 2,4 min      | 39.0%
CasesRunner02 | 41          | 17           | 2,6 min      | 41.5%
CasesRunner03 | 41          | 18           | 2,5 min      | 43.9%
...
CasesRunner15 | 41          | 20           | 2,9 min      | 48.8% ⚠️ MÁS LENTO
...
CasesRunner50 | 41          | 17           | 2,5 min      | 41.5%
PROMEDIO      | 41          | 17           | 2,5 min      | 41.5%
```

---

## 💻 Cómo Ejecutar y Obtener el Reporte

### Opción 1: Ejecutar 50 Tests (Consolidación Completa)
```
1. Abre run_tests.bat
2. Selecciona opción: 6 (50 runners en paralelo)
3. Espera ~3 minutos
4. El archivo se genera automáticamente:
   → target/reports/step_details_YYYYMMDD_HHMMSS.xlsx
```

### Opción 2: Ejecutar 20 Tests
```
1. Abre run_tests.bat
2. Selecciona opción: 4 o 5 (según número de runners)
3. Espera ~2 minutos
4. Resultado: ~820 pasos consolidados en Excel
```

### Opción 3: Generar Manual
```
1. Abre PowerShell en carpeta raíz
2. Ejecuta: powershell -ExecutionPolicy Bypass -File "script/generate_step_details_excel_report_CLEAN.ps1"
3. Se genera consolidando todos los JSON disponibles en target\site\serenity\
```

---

## 🔍 Análisis: Preguntas que Puedes Responder

### Pregunta: "¿Cuál test fue el más lento?"
```
Respuesta en Hoja 4:
1. Ordena columna "Tiempo Total" de mayor a menor
2. El primero es tu test más lento
Ejemplo: CasesRunner15 = 2,9 min (48.8% pasos lentos)
```

### Pregunta: "¿Qué paso es el cuello de botella?"
```
Respuesta en Hoja 3 (Pasos Lentos):
1. Primera fila es el paso más lento globalmente
2. Muestra qué test lo contiene
3. Muestra el % del tiempo total
Ejemplo: "Upload document" (Test 22) = 8,234 ms (10.0% del tiempo)
```

### Pregunta: "¿Es consistente la performance entre tests?"
```
Respuesta en Hoja 4:
1. Mira la columna "Tiempo Total"
2. Calcula: Max - Min
3. Si rango es pequeño (±15%) → Consistente ✅
4. Si rango es grande (>30%) → Problemas puntuales ⚠️
Ejemplo: 2,1 min a 2,9 min = Rango 38%, hay variabilidad
```

### Pregunta: "¿Qué acciones son más lentas?"
```
Respuesta en Hoja 2:
1. Filtra por Acción (Click, Escribe, Ejecuta, etc.)
2. Calcula tiempo promedio por tipo
3. Identifica cuál es más lenta
Ejemplo: "Ejecuta" promedia 1,200 ms vs "Click" 156 ms
```

---

## 📈 Métricas Clave por Test

**Con 50 Tests, esperas ver:**

| Métrica | Valor Típico |
|---------|--------------|
| Pasos por test | 41 |
| Pasos lentos por test | 15-20 (35-50%) |
| Duración por test | 2,1-2,9 min |
| Tiempo promedio por paso | 70-80 ms |
| Pasos >5s por test | 2-4 |
| Paso más lento | 8-10 seg (carga, upload) |

---

## 🚀 Flujo Automático

```
Tú ejecutas: run_tests.bat → opción 6
                ↓
            [50 tests ejecutan en paralelo]
                ↓
            [Generan 50 JSON en target\site\serenity\]
                ↓
            [Script lee los 50 JSON]
                ↓
            [Consolida 2,050 pasos]
                ↓
            [Genera Excel con 4 hojas]
                ↓
    [Automáticamente en target/reports/]
                ↓
        step_details_20260512_100530.xlsx
        (listo para abrir y analizar)
```

---

## 💾 Ubicación de Archivos

```
Raíz Proyecto (E:\Proyectos\Reto_Siigo\Siigo_Front\Sara3\)
│
├─ target/
│  ├─ site/serenity/          ← JSON de cada test (entrada)
│  │  ├─ CasesRunner01.json
│  │  ├─ CasesRunner02.json
│  │  └─ ... hasta CasesRunner50.json
│  │
│  └─ reports/                ← ARCHIVOS GENERADOS (salida)
│     └─ step_details_YYYYMMDD_HHMMSS.xlsx ← TU REPORTE
│
└─ script/generate_step_details_excel_report_CLEAN.ps1 ← Script que lo hace
```

---

## ⚡ Consejos para Análisis

✅ **Abre con Excel** - Mejor que Google Sheets para filtros complejos  
✅ **Usa AutoFilter** - Haz clic en encabezado de hoja para filtrar  
✅ **Ordena por columnas** - Encuentra top/bottom fácilmente  
✅ **Copia a otro archivo** - Si necesitas comparar múltiples ejecuciones  
✅ **Usa formato condicional** - Destaca celdas >5000 ms en rojo  

---

## 📚 Documentación Complementaria

Archivos creados para referencia:
- `CONSOLIDACION_REPORTES_MULTIPLES.md` - Explicación técnica detallada
- `EJEMPLO_1_vs_50_TESTS.md` - Comparación visual 1 test vs 50 tests
- `GUIA_RAPIDA_CONSOLIDACION.md` - Este archivo

¡Listo para consolidar y analizar! 🎯



