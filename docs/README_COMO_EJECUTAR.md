# 🚀 Guía de Ejecución - SARA3 Automation

## ¿Cómo ejecutar los tests en otra máquina?

### Requisitos mínimos:
- **Windows 7 o superior** (10/11 recomendado)
- **8 GB RAM mínimo** (16 GB si vas a ejecutar 50 runners en paralelo)
- **Conexión a internet** (primera ejecución descarga ~2 GB de dependencias)
- **Java NO es necesario** (Gradle lo descarga automáticamente)

---

## Paso 1: Copiar el proyecto

Copia toda la carpeta `Sara3` a tu máquina:
```
C:\Users\TuUsuario\Proyectos\Sara3\
```

---

## Paso 2: Ejecutar el script

**Simplemente dobla-click en:**
```
run_tests.bat
```

**Eso es todo.** El script hará automáticamente:

✅ Descarga Gradle (si no existe)
✅ Descarga todas las dependencias (Maven, Selenium, etc)
✅ Compila el proyecto
✅ Muestra el menú para elegir cuántos tests ejecutar

---

## Primer uso (Primera vez):

```
Descargando Gradle y compilando proyecto...
(Esto puede tomar 2-5 minutos la primera vez)

✓ Dependencias descargadas correctamente
✓ Proyecto compilado

========================================================
         AUTOMATIZACION SARA3 - EJECUCION DE PRUEBAS
========================================================

1. Ejecutar numero PERSONALIZADO de runners en paralelo
2. Ejecutar 2 runners (paralelo)
3. Ejecutar 4 runners (paralelo)
4. Ejecutar 8 runners (paralelo)
5. Ejecutar 12 runners (paralelo)
6. Ejecutar todos los 50 runners
7. Ejecutar solo 1 runner (test individual)
8. Ver reporte de resultados (Serenity)
9. Salir

Selecciona opcion (1-9): _
```

---

## Opciones disponibles:

### **Opción 1: Número personalizado**
```
Selecciona opcion (1-9): 1
Numero de runners a ejecutar (1-50): 10
```
Ejecuta exactamente 10 runners en paralelo.

### **Opción 2: 2 runners** (Máquina lenta)
```
Selecciona opcion (1-9): 2
```
Abre 2 navegadores simultáneos. ~3-5 minutos.

### **Opción 3: 4 runners** (Máquina normal)
```
Selecciona opcion (1-9): 3
```
Abre 4 navegadores simultáneos. ~2-3 minutos.

### **Opción 4: 8 runners** (Máquina rápida)
```
Selecciona opcion (1-9): 4
```
Abre 8 navegadores simultáneos. ~1-2 minutos.

### **Opción 5: 12 runners** (Máquina muy rápida)
```
Selecciona opcion (1-9): 5
```
Abre 12 navegadores simultáneos. ~60-90 segundos.

### **Opción 6: 50 runners** (Máximo - Requiere 16 GB RAM)
```
Selecciona opcion (1-9): 6

ADVERTENCIA: Esto requiere mucha memoria (minimo 8 GB RAM)

Ejecutando 50 runners en paralelo...
Esto puede tomar 15-30 minutos...
```
Abre los 50 navegadores a la vez. Todos los usuarios ejecutan en paralelo.

### **Opción 7: 1 runner individual**
```
Selecciona opcion (1-9): 7
Numero del runner (01-50): 15

Ejecutando CasesRunner15 (sin paralelismo)...
```
Ejecuta un solo runner para debugging. Útil para probar un usuario específico.

### **Opción 8: Ver reporte**
```
Selecciona opcion (1-9): 8

Abriendo reporte Serenity...
(Se abre en navegador automáticamente)
```
Abre el reporte HTML interactivo en el navegador con todos los resultados.

---

## ¿Dónde están las credenciales?

Los 50 usuarios están en:
```
src/test/resources/credentials.properties
```

Ejemplo:
```
usuario1=pruebas1
contrasena1=K7m@2xQ9n#

usuario2=pruebas2
contrasena2=F4p#8xL2n@

usuario50=pruebas50
contrasena50=Z9q@5xM7n#
```

**IMPORTANTE:** Cada test obtiene un usuario **ALEATORIO** del pool. 
- Si ejecutas 2 runners: Cada uno eligirá 1 de los 50 usuarios al azar
- Si ejecutas 50 runners: Todos los usuarios ejecutan en paralelo (probablemente todos usados)
- La asignación es aleatoria pero **consistente durante el test** (mismo usuario para todo el test)

---

## Ejemplos prácticos:

### Scenario 1: Máquina con 8 GB RAM
```
run_tests.bat
→ Opción 2 (2 runners)
→ Ejecuta 2 tests en paralelo
→ Tiempo: 3-5 minutos
```

### Scenario 2: Máquina con 16 GB RAM
```
run_tests.bat
→ Opción 5 (12 runners)
→ Ejecuta 12 tests en paralelo
→ Tiempo: 60-90 segundos
```

### Scenario 3: Test específico para debugging
```
run_tests.bat
→ Opción 7 (1 runner)
→ Número del runner: 15
→ Ejecuta solo CasesRunner15
→ Tiempo: 2-3 minutos (sin paralelismo)
```

---

## Solución de problemas:

### ❌ "ERROR: No se pudo descargar dependencias"
**Solución:** 
- Verifica tu conexión a internet
- Intenta nuevamente
- Si persiste, copia el `.gradle` de otra máquina

### ❌ "Port 4444 already in use"
**Solución:**
- Cierra otros navegadores Chrome
- O ejecuta con menos runners: Opción 2 en lugar de 6

### ❌ "Tests muy lentos"
**Solución:**
- Reduce a menos runners (Opción 2 o 3)
- Cierra otras aplicaciones
- Reinicia la máquina

### ❌ "Out of memory"
**Solución:**
- Aumenta RAM o reduce runners
- En `gradle.properties`, aumenta: `org.gradle.jvmargs=-Xmx4096m`

---

## Archivos importantes:

```
Sara3/
├── run_tests.bat              ← EJECUTA ESTO (double-click)
├── gradlew.bat                ← Gradle (descargado automáticamente)
├── gradle.properties          ← Configuración (maxParallelForks, memoria)
├── build.gradle               ← Dependencias Maven
├── src/
│   └── test/
│       ├── java/
│       │   ├── runners/       ← CasesRunner01-50
│       │   └── stepdefinitions/
│       └── resources/
│           └── features/      ← open_cases.feature (50 scenarios)
└── target/
    └── site/serenity/         ← Reportes generados
        └── index.html         ← Reporte interactivo
```

---

## ¿Cómo modificar la configuración?

Edita `gradle.properties`:

```properties
# Número de runners paralelos
maxParallelForks=2

# Memoria por JVM
org.gradle.jvmargs=-Xmx2048m

# Habilitar paralelismo
org.gradle.parallel=true
```

Luego simplemente ejecuta `run_tests.bat` nuevamente.

---

## Resumen rápido:

| Acción | Comando |
|--------|---------|
| **Ejecutar en paralelo** | `run_tests.bat` → Opción 2-6 |
| **Test individual** | `run_tests.bat` → Opción 7 |
| **Ver resultados** | `run_tests.bat` → Opción 8 |
| **50 users simultáneos** | `run_tests.bat` → Opción 6 |

---

## Más información:

- **Reporte Serenity:** `target/site/serenity/index.html`
- **Logs detallados:** `build/test-results/test/`
- **Credenciales:** `src/test/resources/credentials.properties`

¡Listo! 🚀



