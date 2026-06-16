# 🔍 ¿Por Qué Solo Abría 1 Navegador?

## El Problema

Cuando ejecutabas:
```bash
.\gradlew.bat test --tests CasesRunner --max-workers=12
```

Solo abría **1 navegador** porque:

### ❌ Lo que estaba mal:
1. **Solo había 1 escenario** en el feature file
2. `--max-workers=12` controla workers de Gradle (no escenarios)
3. No había configuración de paralelización de Cucumber

### ✅ Qué significa `--max-workers`:
- `--max-workers` = número de **workers de Gradle** (procesos paralelos de construcción)
- NO controla cuántos tests se ejecutan en paralelo
- NO crea múltiples navegadores

---

## La Solución Implementada

### 1️⃣ Feature Files con Múltiples Escenarios

#### **CasesRunner** (12 escenarios)
[open_cases.feature](e:\Proyectos\Reto_Siigo\Siigo_Front\Sara3\src\test\resources\features\cases\open_cases.feature)
```gherkin
Scenario Outline: Abrir la URL - Ejecucion <id>
  # ... pasos del test ...
  
  Examples:
    | id  | linea | servicio         |
    | 01  | AUTOS | PASO DE GASOLINA |
    | 02  | AUTOS | PASO DE GASOLINA |
    # ... 12 filas totales = 12 escenarios
```
**Resultado:** 12 escenarios = 12 navegadores en paralelo

#### **LoadTestRunner** (50 escenarios)
[load_test_50_users.feature](e:\Proyectos\Reto_Siigo\Siigo_Front\Sara3\src\test\resources\features\cases\load_test_50_users.feature)
```gherkin
Examples:
  | id  | linea | servicio         |
  | 01  | AUTOS | PASO DE GASOLINA |
  | 02  | AUTOS | PASO DE GASOLINA |
  # ... 50 filas totales = 50 escenarios
```
**Resultado:** 50 escenarios = 50 navegadores (en lotes de 12)

---

### 2️⃣ Configuración de Paralelización

#### **build.gradle**
```groovy
test {
    maxParallelForks = 12  // <-- ESTO controla cuántos navegadores en paralelo
}
```
**maxParallelForks** = Número de navegadores Chrome que se ejecutan **simultáneamente**

#### **junit-platform.properties**
```properties
cucumber.execution.parallel.enabled=true
cucumber.execution.parallel.config.fixed.parallelism=12
```
Habilita que Cucumber ejecute escenarios en paralelo

---

### 3️�⃣ Asignación Automática de Usuarios

**UserPoolManager** asigna automáticamente usuarios:
```
Escenario 01 → Thread 1 → BOT01
Escenario 02 → Thread 2 → BOT02
Escenario 03 → Thread 3 → BOT03
...
Escenario 12 → Thread 12 → BOT12
```

Si tienes 50 escenarios con `maxParallelForks=12`:
```
Lote 1: Escenarios 01-12 (BOT01-BOT12) → 12 navegadores
Lote 2: Escenarios 13-24 (BOT13-BOT24) → 12 navegadores
Lote 3: Escenarios 25-36 (BOT25-BOT36) → 12 navegadores
Lote 4: Escenarios 37-48 (BOT37-BOT48) → 12 navegadores
Lote 5: Escenarios 49-50 (BOT49-BOT50) → 2 navegadores
```

---

## 🚀 Cómo Ejecutar Ahora

### Opción 1: Script Interactivo (RECOMENDADO)
```bash
ejecutar_paralelo.bat
```
Verás un menú:
```
[1] Test NORMAL con 12 escenarios en paralelo
[2] Test de CARGA con 50 usuarios en paralelo
[3] Test SECUENCIAL (modo debug)
```

### Opción 2: Comandos Directos

#### 12 escenarios en paralelo:
```bash
.\gradlew.bat test --tests CasesRunner
```
**Resultado:** 12 navegadores ejecutándose simultáneamente

#### 50 escenarios en paralelo:
```bash
.\gradlew.bat test --tests LoadTestRunner
```
**Resultado:** 50 escenarios, 12 navegadores simultáneos (ejecuta en lotes)

---

## ⚙️ Ajustar Número de Navegadores Paralelos

### Cambiar `maxParallelForks` en build.gradle:

```groovy
test {
    maxParallelForks = 5   // 5 navegadores simultáneos
    maxParallelForks = 10  // 10 navegadores simultáneos
    maxParallelForks = 20  // 20 navegadores simultáneos
}
```

### O usar parámetro temporal:
```bash
.\gradlew.bat test --tests CasesRunner -Dtest.maxParallelForks=20
```

---

## 📊 Comparación Visual

### ANTES (1 navegador):
```
Test 1 → BOT01 → Chrome 1
```
**Total:** 1 navegador, ~5 minutos por test

### DESPUÉS - CasesRunner (12 navegadores):
```
Test 01 → BOT01 → Chrome 1  ┐
Test 02 → BOT02 → Chrome 2  │
Test 03 → BOT03 → Chrome 3  │
Test 04 → BOT04 → Chrome 4  ├─ Todos ejecutándose
Test 05 → BOT05 → Chrome 5  │  simultáneamente
Test 06 → BOT06 → Chrome 6  │
...                         │
Test 12 → BOT12 → Chrome 12 ┘
```
**Total:** 12 navegadores simultáneos, ~5 minutos total

### DESPUÉS - LoadTestRunner (50 usuarios, 12 paralelos):
```
LOTE 1 (0-5 min):
  Test 01-12 → BOT01-12 → 12 Chrome simultáneos
  
LOTE 2 (5-10 min):
  Test 13-24 → BOT13-24 → 12 Chrome simultáneos
  
LOTE 3 (10-15 min):
  Test 25-36 → BOT25-36 → 12 Chrome simultáneos
  
LOTE 4 (15-20 min):
  Test 37-48 → BOT37-48 → 12 Chrome simultáneos
  
LOTE 5 (20-22 min):
  Test 49-50 → BOT49-50 → 2 Chrome simultáneos
```
**Total:** 50 tests completados en ~22 minutos (vs 250 min secuencial)

---

## 💡 Conceptos Clave

### `--max-workers` (Gradle)
- Controla workers de **construcción** de Gradle
- NO afecta cuántos tests se ejecutan
- Útil para compilación paralela, no ejecución de tests

### `maxParallelForks` (Gradle Test)
- Controla cuántos tests se ejecutan **simultáneamente**
- 1 fork = 1 navegador Chrome
- **ESTO es lo que determina paralelismo real**

### `Scenario Outline + Examples`
- Crea múltiples escenarios desde una plantilla
- Cada fila en `Examples` = 1 escenario independiente
- Sin esto, solo tienes 1 escenario (1 navegador)

### `cucumber.execution.parallel.*`
- Habilita que Cucumber ejecute escenarios en paralelo
- Debe estar en `junit-platform.properties`
- Trabaja junto con `maxParallelForks`

---

## 🎯 Resumen

| Comando | Escenarios | Navegadores Simultáneos | Usuarios | Tiempo Estimado |
|---------|-----------|------------------------|----------|----------------|
| `.\gradlew.bat test --tests CasesRunner` | 12 | 12 | BOT01-12 | ~5 min |
| `.\gradlew.bat test --tests LoadTestRunner` | 50 | 12 (en lotes) | BOT01-50 | ~22 min |

---

## ✅ Verificación

Para confirmar que se están ejecutando múltiples navegadores:

1. Ejecuta el test:
   ```bash
   .\gradlew.bat test --tests CasesRunner
   ```

2. Abre el Administrador de Tareas de Windows

3. Busca procesos "chrome.exe"

4. Deberías ver **12 procesos chrome.exe** ejecutándose simultáneamente

5. En la consola verás logs como:
   ```
   [UserPoolManager] Thread 123 asignado a usuario: BOT01
   [UserPoolManager] Thread 124 asignado a usuario: BOT02
   [UserPoolManager] Thread 125 asignado a usuario: BOT03
   ...
   ```

---

**¡Ahora sí tendrás múltiples navegadores ejecutándose en paralelo! 🚀**



