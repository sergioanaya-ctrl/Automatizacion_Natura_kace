package com.sara.automation.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Monitor de rendimiento para capturar métricas de performance
 * Mide: tiempos de pasos, memoria, CPU, tamaño de reportes
 */
public class PerformanceMonitor {
    
    private static final String PERFORMANCE_LOG_DIR = "target/performance_logs";
    private Map<String, Long> stepTimes = new LinkedHashMap<>();
    private Map<String, Double> memorySnapshots = new LinkedHashMap<>();
    private String testName;
    private long testStartTime;
    private Runtime runtime = Runtime.getRuntime();
    
    public PerformanceMonitor(String testName) {
        this.testName = testName;
        this.testStartTime = System.currentTimeMillis();
        ensureLogDirectory();
    }
    
    /**
     * Marca el inicio de un paso
     */
    public StepTimer startStep(String stepName) {
        return new StepTimer(this, stepName);
    }
    
    /**
     * Registra el tiempo de un paso
     */
    public void recordStep(String stepName, long durationMs) {
        stepTimes.put(stepName, durationMs);
        System.out.println("  [PERF] " + stepName + ": " + durationMs + "ms");
    }
    
    /**
     * Captura snapshot de memoria
     */
    public void captureMemory(String label) {
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usedMemoryMB = usedMemory / 1024.0 / 1024.0;
        memorySnapshots.put(label, usedMemoryMB);
        System.out.println("  [PERF] Memory " + label + ": " + String.format("%.2f MB", usedMemoryMB));
    }
    
    /**
     * Calcula tamaño de archivo en MB
     */
    public static double getFileSizeMB(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.length() / 1024.0 / 1024.0;
        }
        return 0;
    }
    
    /**
     * Genera reporte de performance en CSV
     */
    public void generateReport() throws IOException {
        ensureLogDirectory();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = PERFORMANCE_LOG_DIR + "/" + testName + "_" + timestamp + ".csv";
        
        try (FileWriter writer = new FileWriter(reportPath)) {
            // Encabezado
            writer.append("Métrica,Valor,Unidad\n");
            
            // Tiempos de pasos
            for (Map.Entry<String, Long> entry : stepTimes.entrySet()) {
                writer.append("Step - " + entry.getKey() + "," + entry.getValue() + ",ms\n");
            }
            
            // Snapshots de memoria
            for (Map.Entry<String, Double> entry : memorySnapshots.entrySet()) {
                writer.append("Memory - " + entry.getKey() + "," + 
                            String.format("%.2f", entry.getValue()) + ",MB\n");
            }
            
            // Tiempo total
            long totalTime = System.currentTimeMillis() - testStartTime;
            writer.append("Total Test Duration," + totalTime + ",ms\n");
            
            // CPU info
            writer.append("Available Processors," + Runtime.getRuntime().availableProcessors() + ",count\n");
            
            writer.flush();
        }
        
        System.out.println("[PERF] Report saved: " + reportPath);
    }
    
    private void ensureLogDirectory() {
        File dir = new File(PERFORMANCE_LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Inner class para medir duración de un paso
     */
    public static class StepTimer implements AutoCloseable {
        private PerformanceMonitor monitor;
        private String stepName;
        private long startTime;
        
        public StepTimer(PerformanceMonitor monitor, String stepName) {
            this.monitor = monitor;
            this.stepName = stepName;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void close() {
            long duration = System.currentTimeMillis() - startTime;
            monitor.recordStep(stepName, duration);
        }
    }
    
    /**
     * Obtiene resumen de performance
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== PERFORMANCE SUMMARY ==========\n");
        sb.append("Test: ").append(testName).append("\n");
        
        long totalStepTime = stepTimes.values().stream().mapToLong(Long::longValue).sum();
        sb.append("Total Steps Time: ").append(totalStepTime).append("ms\n");
        
        if (!stepTimes.isEmpty()) {
            long slowestStep = stepTimes.values().stream().mapToLong(Long::longValue).max().orElse(0);
            String slowestStepName = stepTimes.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("N/A");
            sb.append("Slowest Step: ").append(slowestStepName).append(" (").append(slowestStep).append("ms)\n");
        }
        
        if (!memorySnapshots.isEmpty()) {
            double maxMemory = memorySnapshots.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            sb.append("Peak Memory: ").append(String.format("%.2f MB", maxMemory)).append("\n");
        }
        
        sb.append("=========================================\n");
        return sb.toString();
    }
}
