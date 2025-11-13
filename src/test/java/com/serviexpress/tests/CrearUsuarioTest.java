package com.serviexpress.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.UUID;

public class CrearUsuarioTest {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // 1. Login
            driver.get("http://localhost:8080/login");
            driver.manage().window().maximize();

            driver.findElement(By.id("username")).sendKeys("admin@serviexpress.com");
            driver.findElement(By.id("password")).sendKeys("admin123");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/Admins/usuarios"));

            // 2. Click en Crear usuario
            WebElement btnCrear = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(text(),'Crear usuario')]")
            ));
            btnCrear.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[contains(text(),'Crear Usuario')]")
            ));

            // 3. Llenar formulario
            String randomEmail = "usuario" + UUID.randomUUID().toString().substring(0, 5) + "@test.com";

            driver.findElement(By.id("nombreUsuario")).sendKeys("Usuario Automatizado");
            driver.findElement(By.id("correo")).sendKeys(randomEmail);
            driver.findElement(By.id("clave")).sendKeys("clave123");
            driver.findElement(By.id("telefono")).sendKeys("3001234567");
            driver.findElement(By.id("ciudad")).sendKeys("Bogotá");

            // 4. Seleccionar el rol con Select (ARREGLA TU ERROR)
            WebElement rolSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rol")));

            // Hacer scroll hasta el select para evitar el click intercepted
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", rolSelect);
            Thread.sleep(300);

            Select select = new Select(rolSelect);
            select.selectByIndex(1); // El primer rol válido

            // 5. Guardar
            WebElement btnSubmit = driver.findElement(By.id("submitBtn"));
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", btnSubmit);
            btnSubmit.click();

            // 6. Confirmar mensaje de éxito
            WebElement successMsg = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'alert-success')]")
                    )
            );

            System.out.println("✅ Usuario creado: " + randomEmail);

            // 7. Verificar que aparece en lista
            driver.get("http://localhost:8080/Admins/usuarios");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));

            boolean found = driver.findElements(
                    By.xpath("//*[contains(text(),'" + randomEmail + "')]")
            ).size() > 0;

            if (found) {
                System.out.println("✅ El usuario aparece en la tabla.");
                System.out.println("✅ PRUEBA EXITOSA ✅");
            } else {
                System.out.println("❌ El usuario NO aparece en la tabla.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ La prueba falló por excepción.");
        } finally {
            driver.quit();
        }
    }
}
