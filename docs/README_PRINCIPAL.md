# 🤖 SARA3 Automation - Pruebas Paralelas

Automatización de pruebas **Serenity BDD + Cucumber** con ejecución paralela de **hasta 50 runners simultáneos**.

---

## ⚡ Inicio en 3 pasos

### 1️⃣ Clonar
```bash
git clone https://github.com/sergio129/Sara3.git
cd Sara3
```

### 2️⃣ Ejecutar
```batch
Dobla-click en: run_tests.bat
```

### 3️⃣ Elegir
```
Menú interactivo:
- 2 runners (3-5 min)
- 4 runners (2-3 min)
- 8 runners (1-2 min)
- 12 runners (60-90 seg)
- 50 runners (15-30 min)
- O número personalizado
```

---

## 🎉 ¿Qué pasa automáticamente?

✅ Descarga Gradle (si no existe)
✅ Descarga Java (si no existe)
✅ Descarga Selenium WebDriver
✅ Compila el proyecto
✅ Ejecuta tests en paralelo
✅ Genera reporte interactivo

**Total: Todo funciona con un click.** 🚀

---

## 📊 Características

| Feature | Details |
|---------|---------|
| **Framework** | Serenity BDD 3.4.1 + Cucumber 7.x |
| **Runners** | 50 clases independientes (CasesRunner01-50) |
| **Paralelismo** | Configurable: 1-50 runners simultáneos |
| **Usuarios** | 50 credenciales (pruebas1-pruebas50) |
| **Asignación** | Random por thread con caching |
| **Reportes** | Serenity HTML interactivo con screenshots |
| **Browser** | Chrome con WebDriver 4.6.0 |

---

## 🛠️ Requisitos

- **Windows 7+** (10/11 recomendado)
- **8 GB RAM** (16 GB si vas a usar 50 runners)
- **Conexión a internet** (solo primera ejecución)
- **Nada más.** ✓ Sin necesidad de preinstalaciones

---

## 📁 Estructura del proyecto

```
Sara3/
├── run_tests.bat                      ← EJECUTA AQUÍ
├── README_COMO_EJECUTAR.md            ← Guía detallada
├── gradle/wrapper/                    ← Gradle portable
├── build.gradle                       ← Dependencias
├── src/
│   └── test/
│       ├── java/
│       │   ├── runners/               ← CasesRunner01-50
│       │   └── stepdefinitions/       ← Step definitions
│       └── resources/
│           ├── features/              ← Cucumber scenarios
│           └── credentials.properties ← 50 usuarios
└── target/
    └── site/serenity/                 ← Reportes generados
```

---

## 🚀 Ejemplos de uso

### Ejecutar 4 runners en paralelo
```
run_tests.bat → Opción 3
(4 navegadores abren simultáneamente)
(Tiempo: 2-3 minutos)
```

### Ejecutar todos los 50 usuarios
```
run_tests.bat → Opción 6
(50 navegadores abren en paralelo)
(Requiere 16 GB RAM)
(Tiempo: 15-30 minutos)
```

### Ejecutar un test individual (debugging)
```
run_tests.bat → Opción 7 → 15
(Ejecuta solo CasesRunner15 sin paralelismo)
```

### Ver reportes
```
run_tests.bat → Opción 8
(Abre el reporte interactivo en navegador)
```

---

## 👥 Usuarios de prueba

Se incluyen 50 usuarios listos para usar:

```
usuario1 = pruebas1
usuario2 = pruebas2
...
usuario50 = pruebas50

(Credenciales en: src/test/resources/credentials.properties)
```

Cada runner obtiene un usuario **aleatorio** del pool.

---

## 📖 Documentación completa

Para guía detallada paso-a-paso:
👉 [README_COMO_EJECUTAR.md](README_COMO_EJECUTAR.md)

---

## 🔄 Ciclo de ejecución

```
run_tests.bat
    ↓
[Primera vez] Descarga Gradle + Dependencias
    ↓
Muestra menú (2-50 runners)
    ↓
Configura gradle.properties
    ↓
./gradlew.bat test (paralelo)
    ↓
Abre navegadores Chrome (cantidad configurada)
    ↓
Ejecuta open_cases.feature (50 scenarios)
    ↓
Asigna usuario aleatorio a cada thread
    ↓
Genera reporte Serenity con screenshots
    ↓
Muestra resultado: ✓ Exitoso o ✗ Fallos
    ↓
Espera a que usuario presione Enter
    ↓
Vuelve al menú
```

---

## ⚙️ Configuración avanzada

Edita `gradle.properties` para cambiar:

```properties
# Runners paralelos (default: 2)
maxParallelForks=2

# Memoria por JVM (default: 2048m)
org.gradle.jvmargs=-Xmx2048m

# Habilitar paralelismo
org.gradle.parallel=true
```

Luego ejecuta `run_tests.bat` nuevamente.

---

## 🐛 Solución de problemas

### "ERROR: No se pudo descargar dependencias"
→ Verifica tu conexión a internet y vuelve a intentar

### "Port 4444 already in use"
→ Cierra otros navegadores Chrome o reduce runners

### "Out of memory"
→ Aumenta `org.gradle.jvmargs` en gradle.properties

### Tests muy lentos
→ Reduce runners o cierra otras aplicaciones

---

## 📊 Performance esperado

| Runners | Navegadores | Tiempo estimado | RAM recomendada |
|---------|-------------|-----------------|-----------------|
| 2 | 2 | 3-5 min | 8 GB |
| 4 | 4 | 2-3 min | 8 GB |
| 8 | 8 | 1-2 min | 8 GB |
| 12 | 12 | 60-90 seg | 12 GB |
| 50 | 50 | 15-30 min | 16 GB |

---

## 🎯 Casos de uso

**Desarrollo local:**
```
2-4 runners → Feedback rápido
```

**Testing en CI/CD:**
```
8-12 runners → Balance entre velocidad y recursos
```

**Carga masiva:**
```
50 runners → Todos los usuarios en paralelo
```

---

## 📝 Notas importantes

- ✅ Cada thread obtiene un **usuario único y consistente** durante todo el test
- ✅ Los 50 usuarios se asignan **aleatoriamente** cada ejecución
- ✅ Sin necesidad de configuración previa
- ✅ Compatible con Windows 7+
- ✅ Reportes automáticos con Serenity BDD

---

## 🤝 Contribuir

Pull requests bienvenidas. Para cambios mayores:
1. Fork el repositorio
2. Crea rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto es de código cerrado. Todos los derechos reservados.

---

## 🆘 Soporte

Para problemas o preguntas:
- Revisa [README_COMO_EJECUTAR.md](README_COMO_EJECUTAR.md)
- Verifica logs en `build/test-results/test/`
- Consulta reportes en `target/site/serenity/index.html`

---

**¡Listo! Solo ejecuta `run_tests.bat` y disfruta de pruebas paralelas automáticas.** 🚀



