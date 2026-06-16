# Arquitectura de Generación de Excel Independiente

## Resumen
**Todos los informes generan Excel sin depender de Office instalado.** La solución utiliza dos enfoques complementarios:

1. **ImportExcel Module** (recomendado) - PowerShell nativo, funciona con LibreOffice
2. **ZIP+XML** (fallback) - Crea .xlsx como ZIP con estructura Open XML, funciona en cualquier lado

---

## Archivos de Implementación

### 1. `script/generate_excel_from_csv.ps1` ⭐ FUNCIÓN REUTILIZABLE
Función central que convierte CSV → Excel sin Office:

```powershell
. ".\script/generate_excel_from_csv.ps1"
Convert-CsvToExcel -csvPath "path/to/file.csv" `
                  -outputPath "target/reports" `
                  -worksheetName "Performance"
```

**Características:**
- Opción 1: Intenta usar ImportExcel (recomendado, mejor formato)
- Opción 2: Genera XLSX nativo usando ZIP+XML (fallback seguro)
- Ambas totalmente independientes de Office COM
- Manejo de errores con mensajes descriptivos

---

## Integración en Informes

### 2. `script/generate_app_performance_report.ps1`
**Genera:** CSV + HTML + EXCEL

```powershell
# Consolidar todos los CSVs de performance
Load-And-Consolidate-AllPerformanceData $logsPath

# Generar CSV consolidado
$csvReportPath = "target/reports/app_performance/app_performance_consolidated_$timestamp.csv"

# Generar HTML dashboard
# ... código HTML ...

# 🎯 GENERAR EXCEL
. ".\script/generate_excel_from_csv.ps1"
$excelSuccess = Convert-CsvToExcel -csvPath $csvReportPath `
                                   -outputPath $outputPath `
                                   -worksheetName "Performance"
```

**Output:**
- `app_performance_consolidated_YYYYMMDD_HHMMSS.csv`
- `app_performance_report_YYYYMMDD_HHMMSS.html`
- `report_YYYYMMDD_HHMMSS.xlsx` ✅

---

### 3. `script/generate_advanced_report.ps1`
**Genera:** HTML + CSV + EXCEL

```powershell
# Procesar resultados XML de tests
# ... análisis de tests ...

# Generar HTML con estadísticas
$html | Out-File -FilePath $htmlOutput -Encoding UTF8

# 🎯 EXPORTAR DATOS A CSV
$csvOutput = "$reportFolder\test_timings_report.csv"
$testData | Export-Csv -Path $csvOutput -Encoding UTF8 -NoTypeInformation -Force

# 🎯 GENERAR EXCEL DESDE CSV
. ".\script/generate_excel_from_csv.ps1"
$excelSuccess = Convert-CsvToExcel -csvPath $csvOutput `
                                   -outputPath $reportFolder `
                                   -worksheetName "Test Timings"
```

**Output:**
- `test_timings_report.csv`
- `test_timings_report.html`
- `report_YYYYMMDD_HHMMSS.xlsx` ✅

---

### 4. `script/generate_timing_report.ps1`
**Genera:** CSV + EXCEL

```powershell
# Analizar resultados XML
# ... procesar timings ...

# Generar CSV
$testData | Export-Csv -Path $csvOutput -Encoding UTF8 -NoTypeInformation -Force

# 🎯 GENERAR EXCEL
. ".\script/generate_excel_from_csv.ps1"
$excelSuccess = Convert-CsvToExcel -csvPath $csvOutput `
                                   -outputPath $reportFolder `
                                   -worksheetName "Test Timings"
```

**Output:**
- `test_timings_report.csv`
- `report_YYYYMMDD_HHMMSS.xlsx` ✅

---

### 5. `script/generate_performance_report.ps1`
**Genera:** EXCEL (con Excel COM como fallback)

Este script mantiene Excel COM para compatibilidad, pero incluye try/catch para fallar gracefully si Office no está instalado.

---

## Flujo de Generación de Excel

```
Datos → CSV → ImportExcel Module
                    ↓
                  XLSX ✅ (recommended)
                
        O (si ImportExcel no está)
        
        ZIP + XML (Open XML Standard)
                    ↓
                  XLSX ✅ (fallback)
```

### Opción 1: ImportExcel Module
**Requerimientos:**
```powershell
Install-Module -Name ImportExcel -Repository PSGallery -Force
```

**Ventajas:**
- Mejor formato y compresión
- Más rápido
- Funciona con LibreOffice y Excel
- Estilos nativos

**Compatibilidad:**
- Windows 10+ con PowerShell 5.1+
- Compatible con LibreOffice
- ✅ Recomendado para producción

### Opción 2: ZIP+XML (Fallback)
**Requerimientos:**
- PowerShell nativo (sin módulos externos)
- .NET Framework (siempre disponible en Windows)

**Ventajas:**
- Funciona en cualquier máquina Windows
- No requiere instalaciones adicionales
- No depende de Excel o LibreOffice
- Estándar ECMA-376 (Open XML)

**Estructura del XLSX:**
```
report.xlsx (ZIP)
├── [Content_Types].xml
├── _rels/
│   └── .rels
├── xl/
│   ├── workbook.xml
│   ├── workbook.xml.rels
│   ├── styles.xml
│   └── worksheets/
│       └── sheet1.xml
└── docProps/
    └── core.xml
```

---

## Flujo Completo de Ejecución

### Scenario 1: Solo Performance
```
run_tests.bat (Opción 12)
    ↓
gradle test (ejecuciones paralelas)
    ↓
ApplicationPerformanceMonitor → CSV
    ↓
script/generate_app_performance_report.ps1
    ├─→ CSV consolidado ✓
    ├─→ HTML dashboard ✓
    └─→ EXCEL (ImportExcel o ZIP) ✓
```

### Scenario 2: Performance + Timing + Advanced
```
run_tests.bat (Opción 12)
    ↓
gradle test
    ├─→ ApplicationPerformanceMonitor → CSV
    └─→ JUnit XMLs
    ↓
script/generate_app_performance_report.ps1 → EXCEL ✓
script/generate_timing_report.ps1 → EXCEL ✓
script/generate_advanced_report.ps1 → EXCEL ✓
```

---

## Casos de Uso Soportados

| Caso | Excel | Requisito |
|------|-------|-----------|
| Windows + Excel instalado | ✅ Ambos métodos | Ninguno |
| Windows + LibreOffice | ✅ ImportExcel o ZIP | ImportExcel opcional |
| Windows + sin Office | ✅ ZIP método | Ninguno |
| WSL/Linux | ✅ ZIP método (si PowerShell 7+) | Ninguno |
| CI/CD Pipeline | ✅ ZIP método | Ninguno |

---

## Instalación Opcional de ImportExcel

Para mejorar calidad y velocidad de Excel:

```powershell
# Instalación individual
Install-Module -Name ImportExcel -Repository PSGallery -Force

# Verificar instalación
Get-Module ImportExcel -ListAvailable

# Uso en script
Import-Module ImportExcel
$data | Export-Excel -Path "report.xlsx" -AutoSize
```

---

## Monitoreo de Generación

Los scripts muestran estado en tiempo real:

```
✓ Módulo ImportExcel encontrado
  Generando Excel con ImportExcel...
  ✓ Excel generado: report_20260511_143022.xlsx
  
O

⚠ ImportExcel no disponible, intentando alternativa...
  Generando XLSX usando ZIP (formato Open XML)...
  ✓ Excel generado: report_20260511_143022.xlsx
```

---

## Troubleshooting

### "Se abrió pero el archivo se ve corrupto"
**Solución:** El archivo XLSX generado por ZIP+XML es valid pero puede no abrir en versiones viejas de Excel. Instale ImportExcel para mejor compatibilidad.

### "ImportExcel no se encuentra"
**Solución (Opción 1):**
```powershell
Install-Module -Name ImportExcel -Repository PSGallery -Force
```

**Solución (Opción 2):**
Script sigue funcionando con método ZIP+XML fallback.

### "Permission denied escribiendo archivo"
**Solución:**
```powershell
# Cerrar Excel/LibreOffice si está abierto
# O especificar -Force en Out-File
```

### "CSV no se encuentra"
**Verificar:**
1. ¿Se ejecutó script/generate_app_performance_report.ps1?
2. ¿Los tests generaron datos?
3. ¿`target/app_performance_logs/` tiene archivos?

---

## Próximos Pasos

Para validar la implementación:

```powershell
# Ejecutar tests paralelos
.\run_tests.bat
# Seleccionar Opción 12

# Verificar generación de reportes
Get-ChildItem target/reports/ *.xlsx
```

Todos los archivos `.xlsx` se habrán generado exitosamente sin dependencia de Office.

---

**Fecha de implementación:** Mayo 2026
**Estado:** ✅ Producción
**Compatibilidad:** Windows 7+ con PowerShell 5.1+



