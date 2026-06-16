# 📊 Consolidación de Reportes Paso a Paso - Múltiples Tests

## Cómo Funciona la Consolidación

El script `script/generate_step_details_excel_report_CLEAN.ps1` consolida automáticamente los datos de **todos los tests ejecutados** en un solo archivo Excel. Aquí explicamos cómo:

---

## 🔄 Flujo de Consolidación

### Cuando ejecutas **2 tests** en paralelo:

```
Ejecución de 2 Tests en Paralelo
│
├─ Test 1 (CasesRunner01)
│  └─ Genera: target\site\serenity\CasesRunner01.json
│     └─ Contiene: 41 pasos
│
└─ Test 2 (CasesRunner02)
   └─ Genera: target\site\serenity\CasesRunner02.json
      └─ Contiene: 41 pasos

CONSOLIDACIÓN (Script):
│
├─ Lee: CasesRunner01.json → Extrae 41 pasos
├─ Lee: CasesRunner02.json → Extrae 41 pasos
└─ Consolida en: step_details_YYYYMMDD_HHMMSS.xlsx
   │
   ├─ Hoja 1: RESUMEN
   │  ├─ Total Tests: 2
   │  ├─ Total Pasos: 82 (41+41)
   │  ├─ Pasos Lentos: X
   │  └─ Tiempo Total: X minutos
   │
   ├─ Hoja 2: TODOS LOS PASOS (82 filas)
   │  ├─ Test | Descripcion | Accion | Elemento | Tiempo_ms
   │  ├─ CasesRunner01 | Step 1 | Click | Button | 150
   │  ├─ CasesRunner01 | Step 2 | Escribe | Input | 200
   │  ├─ ... (41 pasos de Test 1)
   │  ├─ CasesRunner02 | Step 1 | Click | Button | 160
   │  ├─ CasesRunner02 | Step 2 | Escribe | Input | 210
   │  └─ ... (41 pasos de Test 2)
   │
   ├─ Hoja 3: PASOS LENTOS (>5s)
   │  └─ Consolidado de ambos tests
   │
   └─ Hoja 4: ESTADÍSTICAS POR TEST (2 filas)
      ├─ CasesRunner01 | 41 pasos | 3 lentos | 2,5 min
      └─ CasesRunner02 | 41 pasos | 4 lentos | 2,7 min
```

---

### Cuando ejecutas **50 tests** en paralelo:

```
Ejecución de 50 Tests en Paralelo
│
├─ CasesRunner01.json → 41 pasos
├─ CasesRunner02.json → 41 pasos
├─ CasesRunner03.json → 41 pasos
├─ ...
└─ CasesRunner50.json → 41 pasos

CONSOLIDACIÓN:
│
├─ Hoja 1: RESUMEN
│  ├─ Total Tests: 50
│  ├─ Total Pasos: 2,050 (50 × 41)
│  ├─ Pasos Lentos: ~100-150 (estimado)
│  └─ Tiempo Total: ~125 minutos (50 tests × 2,5 min)
│
├─ Hoja 2: TODOS LOS PASOS (2,050 filas)
│  └─ Filtrable por:
│     ├─ Test (CasesRunner01 a CasesRunner50)
│     ├─ Acción (Click, Escribe, Ejecuta, etc.)
│     ├─ Tiempo
│     └─ Estado (Passed/Failed)
│
├─ Hoja 3: PASOS LENTOS (>5s)
│  └─ Consolidado de todos los 50 tests
│     (Ordenado por tiempo descendente)
│
└─ Hoja 4: ESTADÍSTICAS POR TEST (50 filas)
   ├─ CasesRunner01 | 41 pasos | 2 lentos | 2,4 min
   ├─ CasesRunner02 | 41 pasos | 3 lentos | 2,6 min
   ├─ CasesRunner03 | 41 pasos | 2 lentos | 2,5 min
   ├─ ...
   └─ CasesRunner50 | 41 pasos | 4 lentos | 2,7 min
```

---

## 📋 Estructura de Datos por Hoja

### Hoja 1: RESUMEN
```
Metrica                          Valor
Total Tests Ejecutados           50
Total de Pasos                   2,050
Pasos Lentos (>5s)              127
Tiempo Total Promedio            2,5 min
Test Mas Lento                   CasesRunner15 (3,2 min)
Test Mas Rapido                  CasesRunner08 (2,1 min)
Pasos Totales Lentos             6.2%
Tiempo Promedio por Paso         73 ms
```

### Hoja 2: TODOS LOS PASOS (2,050 filas)
```
Test          | Descripcion              | Accion         | Elemento      | Tiempo_ms | Estado
CasesRunner01 | Enter '123456' into text | Escribe        | Input Email   | 245       | PASSED
CasesRunner01 | Click on Confirm button  | Click          | Button        | 156       | PASSED
CasesRunner01 | Wait for page load       | Ejecuta        | Page Load     | 5,234     | PASSED
CasesRunner02 | Enter '789012' into text | Escribe        | Input Email   | 267       | PASSED
...
CasesRunner50 | Click on Confirm button  | Click          | Button        | 178       | PASSED
```

### Hoja 3: PASOS LENTOS (>5s SOLAMENTE)
```
Test          | Descripcion              | Accion   | Tiempo_ms | % del Total
CasesRunner22 | Upload file              | Ejecuta  | 9,123     | 10.0% ⭐ TOP
CasesRunner03 | Wait for page load       | Ejecuta  | 7,456     | 8.2%
CasesRunner15 | Navigate to dashboard    | Navega   | 6,234     | 6.8%
CasesRunner42 | Page load complete       | Ejecuta  | 5,890     | 6.4%
... (resto de pasos >5s, ordenados descendente)
```
✅ Solo aparecen PASOS REALES con duración > 5 segundos
✅ NO hay espacios en blanco
✅ Ordenados de mayor a menor tiempo

### Hoja 4: ESTADÍSTICAS POR TEST
```
Test          | Total Pasos | Pasos Lentos | Tiempo Total | % Pasos Lentos
CasesRunner01 | 41          | 2            | 2,456 ms     | 4.9%
CasesRunner02 | 41          | 3            | 2,654 ms     | 7.3%
CasesRunner03 | 41          | 4            | 2,789 ms     | 9.8%
...
CasesRunner50 | 41          | 2            | 2,501 ms     | 4.9%
```

---

## 🔑 Características de Consolidación

### ✅ El script automáticamente:

1. **Lee todos los JSON** de `target\site\serenity\`
   - Cada test genera un archivo JSON
   - El script itera sobre todos ellos

2. **Extrae los pasos de cada test**
   - Test name
   - Descripción del paso
   - Tipo de acción (Click, Escribe, etc.)
   - Elemento HTML
   - Valor escrito
   - Tiempo en ms
   - Estado (PASSED/FAILED)

3. **Consolida en un array único**
   ```powershell
   $allSteps = @()  # Array que acumula todos los pasos
   
   foreach ($jsonFile in $jsonFiles) {
       $steps = Extract-TestSteps -steps $content.testSteps -testName $testName
       $allSteps += $steps  # Se agregan los 41 pasos del test actual
   }
   
   # Resultado: 50 tests × 41 pasos = 2,050 filas en Excel
   ```

4. **Genera estadísticas por test**
   ```powershell
   $testStats += @{
       Test = $testName
       TotalPasos = 41
       PasosLentos = 3
       TiempoTotal = "2,5 min"
   }
   # Resultado: 50 filas en la hoja 4 (una por cada test)
   ```

5. **Crea las 4 hojas de Excel**
   - Hoja 1: Consolidado total
   - Hoja 2: Todos los pasos (filtrable)
   - Hoja 3: Pasos lentos
   - Hoja 4: Estadísticas por test

---

## 💾 Tamaños de Archivo Esperados

| Tests | Pasos Totales | Tamaño Excel | Tiempo Generación |
|-------|--------------|--------------|-------------------|
| 1     | 41           | ~50 KB       | <1 seg           |
| 2     | 82           | ~80 KB       | 1 seg            |
| 5     | 205          | ~180 KB      | 2 seg            |
| 10    | 410          | ~350 KB      | 3 seg            |
| 20    | 820          | ~650 KB      | 5 seg            |
| 50    | 2,050        | ~1.5 MB      | 8 seg            |

---

## 🎯 Casos de Uso Comunes

### Caso 1: Encontrar el paso más lento de 50 tests
```
1. Abre step_details_YYYYMMDD_HHMMSS.xlsx
2. Ve a Hoja 3: "Pasos Lentos"
3. Los pasos están ordenados por tiempo (más lentos primero)
4. Identificas que "Navigate to dashboard" (Test 15) toma 7.4 seg
```

### Caso 2: Comparar performance entre tests
```
1. Abre Hoja 4: "Estadísticas por Test"
2. Ordena por "Tiempo Total" (descendente)
3. Ves que Test 22 toma 3.2 min vs Test 8 que toma 2.1 min
4. Diferencia: 1.1 minuto (52% más lento)
```

### Caso 3: Analizar distribución de acciones
```
1. Abre Hoja 2: "Todos los Pasos" (2,050 filas)
2. Filtra por "Accion" = "Escribe"
3. Ves que hay X escrituras de 500 ms promedio
4. Identifica si hay escrituras anormalmente lentas
```

---

## 📊 Ventajas de esta Consolidación

✅ **Todo en un archivo** - No necesitas 50 archivos separados  
✅ **Fácil comparación** - Compara tests lado a lado  
✅ **Análisis agregado** - Ve tendencias en 50 tests  
✅ **Identificar cuellos de botella** - Encuentra pasos problemáticos  
✅ **Filtrable en Excel** - AutoFilter en todas las hojas  
✅ **Generación automática** - Se crea después de cada ejecución  

---

## 🚀 Próximas Mejoras (Futuro)

- Gráficos de tendencias en Excel
- Comparación período a período
- Alertas si un paso excede threshold
- Exportación a CSV para análisis externo
- Dashboard interactivo con Power BI




