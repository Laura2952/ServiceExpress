package com.usta.serviexpress.util;

import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnectionValidator {

    public void validateConnection(String url, String username, String password) {
        System.out.println("=== VALIDACIÓN DE CONEXIÓN A POSTGRESQL ===");

        try {
            // 1. Verificar si el driver está disponible
            checkDriver();

            // 2. Intentar conexión
            testConnection(url, username, password);

            // 3. Verificar base de datos específica
            testDatabaseExists(url, username, password);

            System.out.println("✅ Todas las validaciones pasaron correctamente");

        } catch (Exception e) {
            System.out.println("❌ Error en la validación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Driver PostgreSQL encontrado");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL no encontrado. Verifica las dependencias en pom.xml");
        }
    }

    private void testConnection(String url, String username, String password) {
        // Extraer URL base (sin nombre de base de datos)
        String baseUrl = url.substring(0, url.lastIndexOf("/"));
        String testUrl = baseUrl + "/postgres"; // Conectar a la BD por defecto

        try (Connection conn = DriverManager.getConnection(testUrl, username, password)) {
            System.out.println("✅ Conexión al servidor PostgreSQL exitosa");
            System.out.println("   - URL: " + testUrl);
            System.out.println("   - Usuario: " + username);

            // Verificar versión de PostgreSQL
            var metadata = conn.getMetaData();
            System.out.println("   - Versión PostgreSQL: " + metadata.getDatabaseProductVersion());

        } catch (SQLException e) {
            throw new RuntimeException("Error conectando al servidor: " + e.getMessage());
        }
    }

    private void testDatabaseExists(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String dbName = url.substring(url.lastIndexOf("/") + 1);
            System.out.println("✅ Conexión a la base de datos '" + dbName + "' exitosa");

            // Verificar tablas existentes
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
