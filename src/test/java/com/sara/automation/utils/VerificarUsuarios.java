package com.sara.automation.utils;

/**
 * Clase de prueba simple para verificar que UserPoolManager carga correctamente los usuarios
 * Ejecutar: java -cp build/classes/java/test com.sara.automation.utils.VerificarUsuarios
 */
public class VerificarUsuarios {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("VERIFICACION DE POOL DE USUARIOS");
        System.out.println("==============================================");
        
        try {
            // Intentar obtener credenciales para simular varios threads
            System.out.println("\nSimulando 10 threads obteniendo usuarios...\n");
            
            for (int i = 1; i <= 10; i++) {
                // Simular thread diferente obteniendo usuario
                UserPoolManager.UserCredentials creds = UserPoolManager.getCredentialsForCurrentThread();
                System.out.println("Thread simulado " + i + ": " + creds.toString());
                
                // Resetear para siguiente iteración
                UserPoolManager.releaseCurrentThreadUser();
            }
            
            System.out.println("\n==============================================");
            System.out.println("Total de usuarios disponibles: " + UserPoolManager.getTotalUsers());
            System.out.println("==============================================");
            System.out.println("\n✓ Pool de usuarios cargado correctamente!");
            
        } catch (Exception e) {
            System.err.println("\n✗ ERROR al cargar pool de usuarios:");
            e.printStackTrace();
        }
    }
}
