#!/bin/bash
# Script para ejecutar un solo test en Docker
set -e

export DISPLAY=:99
export JAVA_OPTS="-Xmx2048m -Xms512m"

echo "🧪 Ejecutando 1 runner para debug..."
./gradlew test -PmaxParallelForks=1 --continue --no-daemon 2>&1

echo "✅ Tests completados!"
echo "📁 Buscando archivos debug..."
find /app/target -name "agent_page_debug_*.txt" -type f 2>/dev/null && echo "✅ Debug files encontrados" || echo "⚠️ No se encontraron debug files"
