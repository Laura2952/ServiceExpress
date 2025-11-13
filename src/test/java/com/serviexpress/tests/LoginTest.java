package com.serviexpress.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

public class LoginTest {

    // Método para verificar si el servidor está arriba
    public static boolean isServerUp(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(2000);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) {

        String loginUrl = "http://localhost:8080/login";
        String homeAdminUrl = "/Admins/usuarios";

        System.out.println("⏳ Verificando si el servidor está arriba…");

        // Esperar hasta 30 segundos a que el servidor esté arriba
        int retries = 0;
        while (!isServerUp(loginUrl) && retries < 15) {
            retries++;
            System.out.println("❗ Servidor no disponible, reintentando… (" + retries + "/15)");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
        }

        if (!isServerUp(loginUrl)) {
            System.out.println("❌ ERROR: El servidor no está arriba. Cancelo la prueba Selenium.");
            return;
        }

        System.out.println("✅ Servidor detectado, iniciando prueba Selenium…");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get(loginUrl);
            driver.manage().window().maximize();

            driver.findElement(By.id("username")).sendKeys("admin@serviexpress.com");
            driver.findElement(By.id("password")).sendKeys("admin123");

            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Esperar que la URL cambie a la del panel Admin
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.urlContains(homeAdminUrl));

            // Validar URL final
            if (driver.getCurrentUrl().contains(homeAdminUrl)) {
                System.out.println("✅ LOGIN CORRECTO: La prueba pasó.");
            } else {
                System.out.println("❌ LOGIN FALLÓ: No llegó a la URL esperada.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ La prueba falló por excepción.");
        } finally {
            driver.quit();
        }
    }
}
