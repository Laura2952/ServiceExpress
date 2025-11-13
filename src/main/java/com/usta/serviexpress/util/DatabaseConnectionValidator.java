package com.usta.serviexpress.util;

import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnectionValidator
 *
 * Purpose:
 * - Utility Spring Component to validate PostgreSQL connections.
 * - Checks that the PostgreSQL driver is present, the server is reachable,
 *   the database exists, and tables are accessible.
 *
 * Usage:
 * - Inject this component in services or scripts where database connectivity
 *   needs to be validated before running operations.
 *
 * Notes:
 * - All connection attempts use JDBC DriverManager.
 * - Exceptions are rethrown as RuntimeException for simplicity in validation scripts.
 */
@Component
public class DatabaseConnectionValidator {

    /**
     * Performs full validation of PostgreSQL connection.
     * Steps:
     * 1. Check if the PostgreSQL JDBC driver is available.
     * 2. Test connection to the PostgreSQL server.
     * 3. Verify the specific database exists and can be queried for tables.
     *
     * Parameters:
     * @param url      JDBC URL of the database, e.g., jdbc:postgresql://localhost:5432/servicepress
     * @param username Database username
     * @param password Database password
     *
     * Returns: void
     *
     * Throws RuntimeException if any validation step fails.
     */
    public void validateConnection(String url, String username, String password) {
        System.out.println("=== VALIDACIÓN DE CONEXIÓN A POSTGRESQL ===");

        try {
            // 1. Verify driver availability
            checkDriver();

            // 2. Attempt connection to server
            testConnection(url, username, password);

            // 3. Verify specific database existence
            testDatabaseExists(url, username, password);

            System.out.println("✅ Todas las validaciones pasaron correctamente");

        } catch (Exception e) {
            System.out.println("❌ Error en la validación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks that the PostgreSQL JDBC driver is available in the classpath.
     *
     * Throws RuntimeException if the driver is not found.
     */
    private void checkDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Driver PostgreSQL encontrado");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado. Verifica las dependencias en pom.xml");
        }
    }

    /**
     * Tests connection to the PostgreSQL server using the default 'postgres' database.
     *
     * Parameters:
     * @param url      Original JDBC URL including database name
     * @param username Database username
     * @param password Database password
     *
     * Throws RuntimeException if the server cannot be reached.
     *
     * Notes:
     * - Extracts base URL (without database name) to connect to default 'postgres' database.
     * - Prints server version for verification.
     */
    private void testConnection(String url, String username, String password) {
        String baseUrl = url.substring(0, url.lastIndexOf("/"));
        String testUrl = baseUrl + "/postgres"; // Connect to default DB

        try (Connection conn = DriverManager.getConnection(testUrl, username, password)) {
            System.out.println("✅ Conexión al servidor PostgreSQL exitosa");
            System.out.println("   - URL: " + testUrl);
            System.out.println("   - Usuario: " + username);

            // Check PostgreSQL version
            var metadata = conn.getMetaData();
            System.out.println("   - Versión PostgreSQL: " + metadata.getDatabaseProductVersion());

        } catch (SQLException e) {
            throw new RuntimeException("Error conectando al servidor: " + e.getMessage());
        }
    }

    /**
     * Tests if the specific database exists and counts its tables.
     *
     * Parameters:
     * @param url      JDBC URL including target database name
     * @param username Database username
     * @param password Database password
     *
     * Throws RuntimeException if connection fails or database is not accessible.
     *
     * Notes:
     * - Counts only objects of type TABLE.
     * - Useful for verifying database schema availability.
     */
    private void testDatabaseExists(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            System.out.println("✅ Conexión a la base de datos '" + dbName + "' exitosa");

            // Retrieve all tables
            var metadata = conn.getMetaData();
            var tables = metadata.getTables(null, null, "%", new String[]{"TABLE"});

            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
            }
            System.out.println("   - Tablas encontradas: " + tableCount);

        } catch (SQLException e) {
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            throw new RuntimeException("Error conectando a la base de datos '" + dbName + "': " + e.getMessage());
        }
    }

    /**
     * Prints a list of common issues and checks for PostgreSQL connectivity.
     *
     * Notes:
     * - Useful as a troubleshooting helper in CLI or validation scripts.
     * - Does not perform actual checks; only prints guidelines.
     */
    public static void checkCommonIssues() {
        System.out.println("\n=== POSIBLES CAUSAS COMUNES ===");
        System.out.println("1. ✅ Verifica que PostgreSQL esté ejecutándose");
        System.out.println("2. ✅ Verifica el nombre de la base de datos (servicepress)");
        System.out.println("3. ✅ Verifica usuario y contraseña (user_java/0000)");
        System.out.println("4. ✅ Verifica que el puerto 5432 esté disponible");
        System.out.println("5. ✅ Verifica permisos del usuario en la BD");
        System.out.println("6. ✅ Ejecuta: GRANT ALL PRIVILEGES ON DATABASE servicepress TO user_java;");
    }
}

/*
Summary (Technical Note):
DatabaseConnectionValidator is a Spring component to validate PostgreSQL connectivity.
It checks that the JDBC driver is present, connects to the server using the default database,
verifies the specific database exists, and counts its tables. Common issues can be printed
for troubleshooting. RuntimeExceptions are thrown for any failure during validation.
*/
