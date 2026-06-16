#!/bin/bash
# Menú interactivo para ejecutar tests en Docker

RUNNER_COUNT=50
MAX_PARALLEL=8

show_menu() {
    clear
    echo "╔════════════════════════════════════════════════════╗"
    echo "║          SARA3 TEST RUNNER - DOCKER               ║"
    echo "╚════════════════════════════════════════════════════╝"
    echo ""
    echo "1) Ejecutar TODOS los tests (8 paralelos)"
    echo "2) Ejecutar test ESPECÍFICO (Runner 1-50)"
    echo "3) Ejecutar rango de tests (p.e., 10-20)"
    echo "4) Ejecutar tests fallidos (si existen)"
    echo "5) Ver reportes generados"
    echo "6) Limpiar reportes y logs"
    echo "7) Salir"
    echo ""
}

run_all_tests() {
    echo "🚀 Ejecutando todos los tests con $MAX_PARALLEL procesos paralelos..."
    time ./gradlew test -DmaxParallelForks=$MAX_PARALLEL
    check_result "Todos los tests"
}

run_specific_test() {
    read -p "Ingresa número de test (1-$RUNNER_COUNT): " runner_num
    if [[ $runner_num -ge 1 && $runner_num -le $RUNNER_COUNT ]]; then
        printf -v runner_padded "%02d" $runner_num
        echo "🚀 Ejecutando CasesRunner$runner_padded..."
        time ./gradlew test --tests "*CasesRunner$runner_padded*"
        check_result "CasesRunner$runner_padded"
    else
        echo "❌ Número inválido"
    fi
}

run_test_range() {
    read -p "Test inicial (1-$RUNNER_COUNT): " start_num
    read -p "Test final (1-$RUNNER_COUNT): " end_num
    
    if [[ $start_num -ge 1 && $end_num -le $RUNNER_COUNT && $start_num -le $end_num ]]; then
        echo "🚀 Ejecutando tests del $start_num al $end_num..."
        for ((i=start_num; i<=end_num; i++)); do
            printf -v runner_padded "%02d" $i
            echo "   → CasesRunner$runner_padded"
        done
        
        # Construir filtro para gradle
        filters=""
        for ((i=start_num; i<=end_num; i++)); do
            printf -v runner_padded "%02d" $i
            filters="$filters --tests '*CasesRunner${runner_padded}*'"
        done
        
        time eval "./gradlew test $filters"
    else
        echo "❌ Rango inválido"
    fi
}

check_result() {
    test_name=$1
    if [ $? -eq 0 ]; then
        echo "✅ $test_name completado exitosamente"
    else
        echo "❌ $test_name falló"
    fi
    read -p "Presiona ENTER para continuar..."
}

view_reports() {
    echo "📊 Reportes disponibles:"
    if [ -d "target/site/serenity" ]; then
        echo "✅ Serenity Report: file:///app/target/site/serenity/index.html"
        echo ""
        echo "Resumen:"
        if [ -f "target/site/serenity/index.html" ]; then
            grep -o 'Tests passed.*Tests failed' target/site/serenity/index.html 2>/dev/null || echo "   (abre en navegador para ver detalles)"
        fi
    else
        echo "❌ No hay reportes generados aún"
    fi
    read -p "Presiona ENTER para continuar..."
}

clean_reports() {
    read -p "¿Limpiar reportes y logs? (s/n): " confirm
    if [ "$confirm" = "s" ]; then
        rm -rf target/site/serenity logs/* app_performance_logs/*
        mkdir -p logs target/reports
        echo "✅ Limpeza completada"
    fi
    read -p "Presiona ENTER para continuar..."
}

# Main loop
while true; do
    show_menu
    read -p "Selecciona opción (1-7): " option
    
    case $option in
        1) run_all_tests ;;
        2) run_specific_test ;;
        3) run_test_range ;;
        4) 
            echo "🔍 Buscando tests fallidos..."
            if [ -d "target/site/serenity" ]; then
                echo "Este feature requiere análisis de reportes previos"
            fi
            read -p "Presiona ENTER para continuar..."
            ;;
        5) view_reports ;;
        6) clean_reports ;;
        7) 
            echo "👋 Saliendo..."
            exit 0
            ;;
        *) echo "❌ Opción inválida" ;;
    esac
done
