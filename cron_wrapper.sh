#!/bin/bash
# ============================================================
# SARA3 - BATCH CRON WRAPPER
# Ejecuta batch_test_8p.sh con manejo de errores y notificaciones
# Para usar en crontab con mejor control
# ============================================================

set -e

SCRIPT_DIR="/ruta/Sara3"  # ⚠️ CAMBIAR A TU RUTA ACTUAL
BATCH_SCRIPT="$SCRIPT_DIR/batch_test_8p.sh"
NOTIFICATION_EMAIL="admin@empresa.com"  # ⚠️ CAMBIAR A TU EMAIL
SLACK_WEBHOOK=""  # ⚠️ CAMBIAR A TU WEBHOOK DE SLACK (opcional)

# ============================================================
# CONFIGURACIÓN
# ============================================================

SUCCESS_FILE="/tmp/sara3_last_success_$(date +%Y%m%d).txt"
ERROR_FILE="/tmp/sara3_last_error_$(date +%Y%m%d).txt"
REPORT_DIR="$SCRIPT_DIR/target/reports"

# ============================================================
# FUNCIONES
# ============================================================

send_slack_notification() {
    local status=$1
    local message=$2
    local color=$3
    
    if [ -z "$SLACK_WEBHOOK" ]; then
        return
    fi
    
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    curl -X POST "$SLACK_WEBHOOK" \
        -H 'Content-Type: application/json' \
        -d "{
            \"attachments\": [{
                \"color\": \"$color\",
                \"title\": \"SARA3 Batch Tests - $status\",
                \"text\": \"$message\",
                \"footer\": \"Sara3 Automation\",
                \"ts\": $(date +%s)
            }]
        }" 2>/dev/null || true
}

send_email_notification() {
    local subject=$1
    local body=$2
    
    if ! command -v mail &> /dev/null; then
        return
    fi
    
    echo -e "$body" | mail -s "$subject" "$NOTIFICATION_EMAIL" 2>/dev/null || true
}

# ============================================================
# EJECUCIÓN PRINCIPAL
# ============================================================

echo "=========================================================="
echo "  SARA3 BATCH CRON WRAPPER"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================================="

# Validar que el script exista
if [ ! -f "$BATCH_SCRIPT" ]; then
    echo "❌ ERROR: Script $BATCH_SCRIPT no encontrado"
    
    send_email_notification \
        "❌ SARA3 Batch - Script no encontrado" \
        "El script batch_test_8p.sh no existe en:\n$BATCH_SCRIPT\n\nVerifica la ruta en cron_wrapper.sh"
    
    send_slack_notification "FAILURE" "Script no encontrado en $BATCH_SCRIPT" "danger"
    exit 1
fi

# Cambiar a directorio del proyecto
cd "$SCRIPT_DIR" || exit 1

# Ejecutar script batch
echo "Ejecutando batch_test_8p.sh..."
if bash "$BATCH_SCRIPT" > "$ERROR_FILE" 2>&1; then
    # ✅ ÉXITO
    EXEC_TIME=$(date '+%Y-%m-%d %H:%M:%S')
    echo "✅ Ejecución completada exitosamente a las $EXEC_TIME"
    
    # Guardar confirmación
    echo "$EXEC_TIME: SUCCESS" > "$SUCCESS_FILE"
    
    # Contar reportes generados
    REPORT_COUNT=$(find "$REPORT_DIR" -name "*.csv" -o -name "*.html" 2>/dev/null | wc -l)
    
    # Notificación de éxito
    send_email_notification \
        "✅ SARA3 Batch Tests - ÉXITO" \
        "Los tests batch de SARA3 se ejecutaron correctamente.\n\nDetalles:\n- Timestamp: $EXEC_TIME\n- Reportes generados: $REPORT_COUNT\n- Ubicación: $REPORT_DIR\n\nPara ver logs completos:\ncat $SCRIPT_DIR/logs/batch_test_*.log | tail -50"
    
    send_slack_notification "SUCCESS" "✅ Tests completados con éxito\n📊 Reportes: $REPORT_COUNT\n📁 Ubicación: $REPORT_DIR" "good"
    
else
    # ❌ ERROR
    EXEC_TIME=$(date '+%Y-%m-%d %H:%M:%S')
    echo "❌ Ejecución falló a las $EXEC_TIME"
    
    # Guardar error
    echo "$EXEC_TIME: FAILURE" > "$ERROR_FILE"
    
    # Extraer últimas líneas del error
    LAST_ERROR=$(tail -20 "$ERROR_FILE")
    
    # Notificación de error
    send_email_notification \
        "❌ SARA3 Batch Tests - ERROR" \
        "Los tests batch de SARA3 FALLARON.\n\nDetalles:\n- Timestamp: $EXEC_TIME\n- Ubicación del script: $BATCH_SCRIPT\n\nÚltimas líneas del error:\n$LAST_ERROR\n\nPara ver log completo:\ntail -f $SCRIPT_DIR/logs/batch_test_*.log"
    
    send_slack_notification "FAILURE" "❌ Tests fallaron\n🔴 Timestamp: $EXEC_TIME\n⚠️ Ver logs para detalles" "danger"
    
    exit 1
fi

echo "=========================================================="
echo "  ✅ CRON WRAPPER FINALIZADO"
echo "=========================================================="
