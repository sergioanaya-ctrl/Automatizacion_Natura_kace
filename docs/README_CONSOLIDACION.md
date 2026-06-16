# 📊 Consolidación de Reportes Multi-Máquina

## 🎯 Propósito

Este sistema permite **consolidar reportes CSV de múltiples máquinas** en un único reporte unificado. Ideal para testing distribuido donde varios equipos ejecutan tests en paralelo.

## 📁 Estructura

```
Sara3/
├── consolidate_reports.bat          # Script ejecutable independiente
├── script/consolidate_reports.ps1          # Script PowerShell de consolidación
└── reports_consolidation/           # Carpeta para CSV de entrada/salida
    ├── step_details_20260511_*.csv  # Archivos CSV de Máquina 1
    ├── step_details_20260511_*.csv  # Archivos CSV de Máquina 2
    └── consolidated_report_*.csv    # Reportes consolidados generados
```

## 🚀 Cómo Usar

### Paso 1: Ejecutar tests en cada máquina

En cada máquina:
1. Ejecuta `run_tests.bat` 
2. Selecciona opción de ejecución (ej: "6. Ejecutar 50 runners en paralelo")
3. Espera a que termine y se generen los reportes
4. Los archivos CSV se generan en: `.\target\reports\step_details_*.csv`

### Paso 2: Recopilar CSV de todas las máquinas

1. Crea la carpeta `reports_consolidation\` (si no existe, el script la crea automáticamente)
2. Copia los archivos `step_details_*.csv` de cada máquina a esta carpeta:
   - Desde Máquina A: copiar `step_details_YYYYMMDD_HHMMSS.csv`
   - Desde Máquina B: copiar `step_details_YYYYMMDD_HHMMSS.csv`
   - Desde Máquina C: copiar `step_details_YYYYMMDD_HHMMSS.csv`
   - etc.

### Paso 3: Ejecutar consolidación

**Opción A: Desde el menú principal**
1. Ejecuta `run_tests.bat`
2. Selecciona opción **15. CONSOLIDAR REPORTES DE MULTIPLES MAQUINAS**

**Opción B: Script independiente**
1. Ejecuta `consolidate_reports.bat` directamente
2. El script procesará todos los CSV encontrados

## 📦 Archivos Generados

El consolidador genera **4 archivos**:

1. **consolidated_report_YYYYMMDD_HHMMSS.csv**
   - Todos los pasos de todas las máquinas consolidados
   - Incluye columna "Archivo Origen" para rastrear la fuente

2. **consolidated_report_stats_YYYYMMDD_HHMMSS.csv**
   - Estadísticas por test consolidadas
   - Columnas: Test, Batch, Máquina, Usuario, Total Pasos, Tiempo, Estado, Error Type

3. **consolidated_report_by_machine_YYYYMMDD_HHMMSS.csv**
   - Comparación entre máquinas
   - Muestra tests exitosos/fallidos por máquina

4. **consolidated_report_YYYYMMDD_HHMMSS.xlsx** (si ImportExcel disponible)
   - 8 hojas: Resumen General, Por Máquina, Estadísticas por Test, Todos los Pasos, Pasos Lentos, Tests Fallidos, Distribución Errores, Por Batch
   - Formato con tablas y estilos profesionales

## 🔍 Identificación de Origen

Cada reporte CSV incluye las columnas:
- **Maquina**: Nombre del equipo (ej: DESKTOP-ABC123)
- **Usuario**: Usuario que ejecutó los tests (ej: Sergio)
- **Archivo Origen**: Nombre del CSV fuente (agregado por consolidador)

Esto permite rastrear exactamente qué máquina ejecutó cada test.

## ⚙️ Requisitos

- **PowerShell 5.1+** (incluido en Windows 10/11)
- **ImportExcel module** (opcional, solo para generar Excel):
  ```powershell
  Install-Module ImportExcel -Scope CurrentUser
  ```
  - Si no está instalado, el script solo generará CSV (igualmente funcional)

## 📊 Ejemplo de Flujo Completo

### Escenario: 3 máquinas ejecutando 50 tests c/u = 150 tests totales

**Máquina A (DESKTOP-A):**
```
.\run_tests.bat
> Opción 6: 50 runners
> Se genera: step_details_20260511_143022.csv (50 tests)
```

**Máquina B (LAPTOP-B):**
```
.\run_tests.bat
> Opción 6: 50 runners
> Se genera: step_details_20260511_143145.csv (50 tests)
```

**Máquina C (SERVER-C):**
```
.\run_tests.bat
> Opción 6: 50 runners
> Se genera: step_details_20260511_143310.csv (50 tests)
```

**Consolidación (en cualquier máquina):**
```
reports_consolidation\
├── step_details_20260511_143022.csv  (Máquina A)
├── step_details_20260511_143145.csv  (Máquina B)
└── step_details_20260511_143310.csv  (Máquina C)

.\consolidate_reports.bat
```

**Resultado:**
```
consolidated_report_20260511_150000.csv
- 150 tests consolidados
- Columna Máquina identifica origen (DESKTOP-A, LAPTOP-B, SERVER-C)
- Estadísticas comparativas entre máquinas
```

## 🎓 Ventajas del Sistema

✅ **Independiente**: `consolidate_reports.bat` funciona standalone  
✅ **Simple**: Solo requiere copiar CSV a una carpeta  
✅ **Portable**: CSV universal, no requiere Excel instalado  
✅ **Trazable**: Cada paso identifica máquina y usuario de origen  
✅ **Escalable**: Soporta cualquier cantidad de máquinas  
✅ **Comparativo**: Genera estadísticas por máquina automáticamente  

## 🔧 Troubleshooting

**No se encuentran archivos CSV:**
- Verifica que los CSV estén en `reports_consolidation\`
- Los archivos deben llamarse `step_details_*.csv`
- Verifica que contengan las columnas: Test, Batch, Maquina, Usuario

**Excel no se genera:**
- Normal si ImportExcel no está instalado
- Los CSV consolidados son completamente funcionales
- Para instalar ImportExcel: `Install-Module ImportExcel -Scope CurrentUser`

**Errores de encoding:**
- Los CSV deben estar en UTF-8
- Los scripts generan automáticamente UTF-8 correcto

## 📞 Soporte

Para más información consulta:
- `README_PRINCIPAL.md` - Documentación general del proyecto
- `README_COMO_EJECUTAR.md` - Guía de ejecución de tests



