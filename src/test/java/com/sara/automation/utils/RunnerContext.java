package com.sara.automation.utils;

/**
 * ThreadLocal para almacenar el número del runner en el contexto del thread
 * Cada CasesRunnerNN establece su número aquí antes de ejecutar
 * CredentialsReader lo lee para asignar el usuario correcto
 */
public class RunnerContext {
    private static final ThreadLocal<Integer> runnerNumber = ThreadLocal.withInitial(() -> -1);
    
    /**
     * Establece el número del runner para este thread
     * Llamado por cada CasesRunnerNN en su setup
     */
    public static void setRunnerNumber(int number) {
        System.out.println("[RunnerContext] ESTABLECER runner #" + number + " en thread: " + Thread.currentThread().getName());
        runnerNumber.set(number);
    }
    
    /**
     * Obtiene el número del runner para este thread
     * Retorna -1 si no se ha establecido
     */
    public static int getRunnerNumber() {
        int number = runnerNumber.get();
        System.out.println("[RunnerContext] OBTENER runner # " + number + " del thread: " + Thread.currentThread().getName());
        return number;
    }
    
    /**
     * Limpia el valor cuando finaliza el test
     */
    public static void clear() {
        System.out.println("[RunnerContext] LIMPIAR runner context");
        runnerNumber.remove();
    }
}
