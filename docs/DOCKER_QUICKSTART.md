# 🚀 DOCKER QUICK START - SARA3

## 5 Minutos para empezar

### 1. **Clonar proyecto**
```bash
git clone https://github.com/sergio129/Sara3.git
cd Sara3
```

### 2. **Construir imagen**
```bash
docker build -t sara3:latest .
```

### 3. **Ejecutar tests**
```bash
# Batch tests (8 paralelo)
docker run --rm \
  -v $(pwd)/reports:/app/target/reports \
  sara3:latest \
  batch_test_8p.sh
```

### 4. **Ver reportes**
```bash
ls -lh reports/
cat reports/step_details_*.csv
```

---

## Comandos Comunes

### **Build**
```bash
docker build -t sara3:latest .
```

### **Batch Tests**
```bash
docker run --rm -v $(pwd)/reports:/app/target/reports sara3:latest batch_test_8p.sh
```

### **Menú Interactivo**
```bash
docker run -it --rm -v $(pwd)/reports:/app/target/reports sara3:latest ./run_tests.sh
```

### **Test Individual**
```bash
docker run --rm -v $(pwd)/reports:/app/target/reports sara3:latest \
  bash -c "./gradlew test --tests 'com.sara.automation.runners.CasesRunner01'"
```

### **Con Docker Compose**
```bash
# Batch tests
docker-compose up sara3-batch

# Menú interactivo
docker-compose up sara3-interactive

# Parar todo
docker-compose down
```

### **Ver Logs**
```bash
docker logs <container-id>
docker-compose logs -f sara3-batch
```

### **Limpiar**
```bash
docker system prune -a
```

---

## Usando docker-helper.sh

Script interactivo para facilitar operaciones:

```bash
chmod +x docker-helper.sh
./docker-helper.sh
```

Opciones:
1. Build imagen
2. Batch tests
3. Menú interactivo
4. Test individual
5. Ver logs
6. Limpiar
7. Info Docker
8. Docker Compose
9. Salir

---

## Archivos Importante

- **Dockerfile**: Imagen optimizada con multi-stage build
- **docker-compose.yml**: 3 servicios (batch, interactive, single)
- **.dockerignore**: Excluye archivos innecesarios
- **DOCKER_GUIDE.md**: Guía completa (600+ líneas)
- **docker-helper.sh**: Script interactivo

---

## Notas

- Los reportes se guardan en `./reports/` en tu máquina
- Los logs se guardan en `./logs/`
- La imagen incluye Java 8, Chromium y ChromeDriver
- Tamaño aprox: 800 MB
- Tiempo de build: 3-5 minutos (primera vez)

---

Ver: [DOCKER_GUIDE.md](DOCKER_GUIDE.md) para guía completa



