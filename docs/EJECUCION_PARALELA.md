# Configuración de Ejecución Paralela con Múltiples Usuarios

## 📋 Descripción General

Este proyecto está configurado para ejecutar pruebas automatizadas con **50 usuarios concurrentes**. Cada thread de ejecución toma automáticamente un usuario del pool disponible de forma thread-safe.

---

## 🔧 Componentes Principales

### 1. **UserPoolManager.java**
Gestor thread-safe que asigna usuarios automáticamente a cada thread de ejecución:
- Mantiene un pool de usuarios disponibles
- Asigna un usuario único a cada thread
- Usa estrategia round-robin para distribuir la carga
- Thread-safe mediante `ConcurrentHashMap` y sincronización

### 2. **CredentialsReader.java** (Actualizado)
Ahora utiliza `UserPoolManager` para obtener credenciales:
- Cada thread obtiene automáticamente un usuario diferente
- Fallback a credenciales simples si hay error con el pool
- Compatible con ejecución no paralela

### 3. **credentials.properties**
Archivo de configuración con 50 usuarios:
```properties
# Fallback (ejecución no paralela)
usuario=BOT01
contrasena=Yt7PNg_VnXkvsQ*tPmfi

# Pool para ejecución paralela
usuario1=BOT01
contrasena1=Yt7PNg_VnXkvsQ*tPmfi
usuario2=BOT02
contrasena2=Yt7PNg_VnXkvsQ*tPmfi
...
usuario50=BOT50
contrasena50=Yt7PNg_VnXkvsQ*tPmfi
```

---

## 🚀 Cómo Ejecutar Pruebas en Paralelo

### Opción 1: Ejecución Automática con Todos los Cores
```bash
./gradlew test --tests CasesRunner
```
El sistema usará automáticamente `(Número de CPUs / 2)` forks paralelos.

### Opción 2: Especificar Número de Forks Manualmente
```bash
./gradlew test --tests CasesRunner --max-workers=10
```
Esto ejecutará hasta 10 tests en paralelo simultáneamente.

### Opción 3: Ejecución con Tags Específicos (Cucumber)
Si necesitas ejecutar solo ciertos escenarios en paralelo:
```bash
./gradlew test -Dcucumber.filter.tags="@paralelo"
```

---

## ⚙️ Configuración de Paralelismo

### **build.gradle**
```groovy
test {
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
    maxHeapSize = '2048m'
    forkEvery = 0  // Reutilizar fork para eficiencia
}
```

**Ajustes recomendados según recursos:**
- **4 CPUs / 8 GB RAM**: `maxParallelForks = 2-4`
- **8 CPUs / 16 GB RAM**: `maxParallelForks = 4-8`
- **16+ CPUs / 32+ GB RAM**: `maxParallelForks = 8-16`

### **serenity.properties**
```properties
# Cada test debe tener su propia sesión de navegador
serenity.restart.browser.for.each.scenario = true
serenity.keep.browser.open = false
webdriver.close.driver = true
webdriver.quit.driver = true
```

---

## 📊 Monitoreo de Ejecución

Durante la ejecución verás logs como:
```
=========================================
CONFIGURACION EJECUCION PARALELA
=========================================
Max Parallel Forks: 8
Available Processors: 16
Heap Size: 2048m
=========================================

[UserPoolManager] Cargados 50 usuarios disponibles
[UserPoolManager] Thread 123 asignado a usuario: BOT01
[UserPoolManager] Thread 124 asignado a usuario: BOT02
[UserPoolManager] Thread 125 asignado a usuario: BOT03
...
```

---

## 🔄 Distribución de Usuarios

El sistema usa **Round-Robin** para asignar usuarios:
- Test 1 → BOT01
- Test 2 → BOT02
- Test 3 → BOT03
- ...
- Test 50 → BOT50
- Test 51 → BOT01 (reinicia ciclo)

**Ventajas:**
- No hay colisiones entre usuarios
- Distribución equitativa de carga
- Automático, no requiere configuración manual

---

## 📝 Personalizar Usuarios

### Cambiar Contraseñas
Edita `src/test/resources/credentials.properties`:
```properties
usuario1=BOT01
contrasena1=TU_CONTRASEÑA_AQUI

usuario2=BOT02
contrasena2=OTRA_CONTRASEÑA
```

### Agregar Más Usuarios
Simplemente agrega más líneas:
```properties
usuario51=BOT51
contrasena51=password51

usuario52=BOT52
contrasena52=password52
```

### Reducir Usuarios
El sistema detecta automáticamente cuántos usuarios hay disponibles. Puedes tener menos de 50.

---

## 🐛 Debug y Ejecución No Paralela

Para desarrollo local o debug individual:

### Desactivar Paralelización Temporalmente
En `build.gradle`:
```groovy
test {
    maxParallelForks = 1  // Secuencial
}
```

En `serenity.properties`:
```properties
serenity.restart.browser.for.each.scenario = false
serenity.keep.browser.open = true
webdriver.close.driver = false
webdriver.quit.driver = false
```

---

## 📈 Estimación de Tiempos

**Ejemplo:** Si cada test tarda 5 minutos:

| Configuración | Tiempo Total |
|--------------|-------------|
| Secuencial (1 fork) | 250 minutos (4h 10m) |
| 5 forks paralelos | 50 minutos |
| 10 forks paralelos | 25 minutos |
| 20 forks paralelos | 12.5 minutos |

**Nota:** El tiempo real depende de recursos del sistema y red.

---

## ⚠️ Consideraciones Importantes

1. **Recursos del Sistema:**
   - Cada fork necesita ~500 MB RAM
   - Cada navegador Chrome necesita ~300 MB RAM adicionales
   - Para 10 forks paralelos: ~8 GB RAM recomendados

2. **Rendimiento de Red:**
   - Las pruebas paralelas generan más tráfico de red
   - Asegúrate de que tu conexión soporte múltiples requests simultáneos

3. **Límites del Servidor:**
   - Verifica que el servidor de aplicación soporte 50+ usuarios concurrentes
   - Puede ser necesario coordinar con el equipo de infraestructura

4. **ChromeDriver:**
   - Asegúrate de tener suficientes instancias de ChromeDriver
   - El sistema gestiona automáticamente múltiples sesiones

---

## 🎯 Ejemplo de Ejecución Completa

```bash
# 1. Verificar configuración
cat src/test/resources/credentials.properties | grep "usuario" | wc -l
# Debería mostrar 51 (1 fallback + 50 usuarios)

# 2. Ejecutar tests en paralelo
./gradlew clean test --tests CasesRunner

# 3. Ver reportes
./gradlew aggregate
# Los reportes estarán en: target/site/serenity/index.html
```

---

## 🔍 Troubleshooting

### Problema: "No se encontraron credenciales"
**Solución:** Verifica que `credentials.properties` tenga el formato correcto.

### Problema: Tests fallan por timeouts
**Solución:** Aumenta timeouts en `serenity.properties`:
```properties
serenity.timeout = 20000
webdriver.timeouts.implicitlywait = 10000
```

### Problema: Consumo excesivo de RAM
**Solución:** Reduce `maxParallelForks` en `build.gradle`:
```groovy
maxParallelForks = 3  // Menos forks = menos RAM
```

### Problema: Usuarios duplicados/colisiones
**Solución:** El sistema es thread-safe, pero verifica que cada usuario tenga credenciales únicas válidas.

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa los logs en consola para mensajes de `[UserPoolManager]`
2. Verifica que todos los usuarios existen en el sistema
3. Confirma que las contraseñas sean correctas
4. Valida recursos del sistema (RAM, CPU)

---

**¡Listo para ejecutar 50 usuarios en paralelo! 🚀**



