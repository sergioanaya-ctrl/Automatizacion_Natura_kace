package com.natura.automation.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Gestor thread-safe para asignar usuarios de forma concurrente
 * Mantiene un pool de usuarios disponibles y los asigna a cada thread de ejecución
 *
 * ESTRATEGIA: CICLO BARAJADO (shuffled round-robin). Cada asignación saca el siguiente
 * usuario de una baraja del pool completo; solo cuando TODOS los usuarios ya se usaron en
 * el ciclo actual, se vuelve a barajar y reiniciar. Así se garantiza que los 40 usuarios se
 * usan antes de repetir ninguno, sin importar cuántos runners corran en paralelo.
 */
public class UserPoolManager {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("credentials");
    private static final Map<Long, UserCredentials> threadUsers = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    private static List<UserCredentials> availableUsers;
    // Cola del ciclo actual: usuarios aún no asignados en esta vuelta. Se repone (y rebaraja)
    // cada vez que se vacía. Acceso sincronizado porque varios threads la consumen en paralelo.
    private static final Deque<UserCredentials> cycleQueue = new ArrayDeque<>();

    static {
        loadUsers();
    }

    private UserPoolManager() {}

    /**
     * Carga todos los usuarios disponibles desde credentials.properties
     */
    private static void loadUsers() {
        availableUsers = new ArrayList<>();
        int userCount = 1;
        
        while (true) {
            String userKey = "usuario" + userCount;
            String passKey = "contrasena" + userCount;
            
            try {
                String usuario = bundle.getString(userKey);
                String contrasena = bundle.getString(passKey);
                availableUsers.add(new UserCredentials(usuario, contrasena, userCount));
                userCount++;
            } catch (MissingResourceException e) {
                // No hay más usuarios disponibles
                break;
            }
        }
        
        if (availableUsers.isEmpty()) {
            // Fallback: si no hay usuarios numerados, intenta con usuario/contrasena simples
            try {
                String usuario = bundle.getString("usuario");
                String contrasena = bundle.getString("contrasena");
                availableUsers.add(new UserCredentials(usuario, contrasena, 1));
            } catch (MissingResourceException e) {
                throw new RuntimeException("No se encontraron credenciales en credentials.properties");
            }
        }
        
        System.out.println("[UserPoolManager] =====================================");
        System.out.println("[UserPoolManager] ✓ INICIALIZACION: Cargados " + availableUsers.size() + " usuarios");
        System.out.println("[UserPoolManager] Pool: pruebas1 → pruebas" + availableUsers.size());
        System.out.println("[UserPoolManager] Estrategia: CICLO BARAJADO (todos se usan antes de repetir)");
        System.out.println("[UserPoolManager] Parallelism: Cada test recibe el siguiente usuario del ciclo");
        System.out.println("[UserPoolManager] =====================================");
    }

    /**
     * Obtiene el SIGUIENTE usuario del ciclo barajado (no un índice puramente aleatorio).
     * Garantiza que TODOS los usuarios del pool se usan antes de que alguno se repita:
     * - Si el ciclo actual tiene usuarios pendientes, saca el siguiente.
     * - Si el ciclo se vació (los N usuarios ya fueron asignados), lo rebaraja y reinicia.
     * Funciona igual con cualquier número de runners en paralelo (2, 4, 8, 50, etc).
     */
    public static synchronized UserCredentials getRandomUser() {
        if (cycleQueue.isEmpty()) {
            List<UserCredentials> shuffled = new ArrayList<>(availableUsers);
            Collections.shuffle(shuffled, random);
            cycleQueue.addAll(shuffled);
            System.out.println("[UserPoolManager] 🔄 CICLO REINICIADO: los " + availableUsers.size() +
                    " usuarios fueron asignados, se rebaraja el pool completo.");
        }

        UserCredentials user = cycleQueue.poll();

        String threadName = Thread.currentThread().getName();
        int restantesEnCiclo = cycleQueue.size();
        System.out.println("[UserPoolManager] ✓ CICLO: Thread " + threadName + " → usuario: " + user.getUsuario() +
                " [quedan " + restantesEnCiclo + " de " + availableUsers.size() + " en este ciclo]");

        return user;
    }

    /**
     * Obtiene las credenciales asignadas al thread actual (CACHEADO por thread)
     * Si el thread ya tiene usuario asignado, devuelve el mismo (no cambia durante el test)
     */
    public static UserCredentials getCredentialsForCurrentThread() {
        long threadId = Thread.currentThread().getId();
        String threadName = Thread.currentThread().getName();
        
        // Si este thread ya tiene usuario asignado, devolverlo (mantiene consistencia dentro del test)
        if (threadUsers.containsKey(threadId)) {
            UserCredentials cached = threadUsers.get(threadId);
            System.out.println("[UserPoolManager] ✓ CACHED: Thread " + threadName + " → usuario: " + cached.getUsuario() + " (ya asignado)");
            return cached;
        }
        
        // Primera vez que este thread solicita usuario: asignar uno aleatorio
        UserCredentials randomUser = getRandomUser();
        threadUsers.put(threadId, randomUser);
        return randomUser;
    }

    /**
     * Libera el usuario asignado al thread actual (opcional, para liberar recursos)
     */
    public static void releaseCurrentThreadUser() {
        long threadId = Thread.currentThread().getId();
        UserCredentials released = threadUsers.remove(threadId);
        if (released != null) {
            System.out.println("[UserPoolManager] Thread " + threadId + " liberó usuario: " + released.getUsuario());
        }
    }

    /**
     * Obtiene el número total de usuarios disponibles
     */
    public static int getTotalUsers() {
        return availableUsers.size();
    }

    /**
     * Clase interna para almacenar credenciales de usuario
     */
    public static class UserCredentials {
        private final String usuario;
        private final String contrasena;
        private final int userNumber;

        public UserCredentials(String usuario, String contrasena, int userNumber) {
            this.usuario = usuario;
            this.contrasena = contrasena;
            this.userNumber = userNumber;
        }

        public String getUsuario() {
            return usuario;
        }

        public String getContrasena() {
            return contrasena;
        }

        public int getUserNumber() {
            return userNumber;
        }

        @Override
        public String toString() {
            return "Usuario: " + usuario + " (#" + userNumber + ")";
        }
    }
}
