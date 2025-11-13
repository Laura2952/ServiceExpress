#  ServiExpress

Marketplace para la gestión de servicios domésticos y profesionales desarrollado en *Spring Boot*.

---

##  Descripción del Software

*ServiExpress* es una plataforma tipo marketplace diseñada para conectar a clientes que requieren servicios domésticos o profesionales —como aseo, plomería, jardinería o reparaciones— con personas que los ofrecen de forma rápida, segura y confiable.

La aplicación permite:
- Registro y autenticación de usuarios (clientes y proveedores).
- Búsqueda y contratación de servicios según ubicación y categoría.
- Gestión de citas y pagos en línea.
- Calificación y comentarios de servicios recibidos.

Los proveedores pueden:
- Crear un perfil profesional.
- Publicar su oferta de servicios.
- Recibir solicitudes y programar citas.

---

##  Tecnologías Utilizadas

| Componente | Tecnología |
|-------------|-------------|
| *Lenguaje principal* | Java 17 |
| *Framework backend* | Spring Boot |
| *Motor de plantillas* | Thymeleaf |
| *Base de datos* | PostgreSQL (pgAdmin) |
| *Control de versiones* | GitHub |
| *Gestión de imágenes* | API de ImgBB |
| *Procesador de pagos* | Wompi API |
| *ORM* | Spring Data JPA / Hibernate |
| *Gestión de dependencias* | Maven |

---

##  Arquitectura y Estructura del Proyecto

El proyecto sigue una *arquitectura MVC* (Modelo-Vista-Controlador) bajo las buenas prácticas de desarrollo con Spring Boot.

src/main/java/com/usta/serviexpress
│
├── Controller/           → Controladores REST y MVC
├── DTOs/                 → Objetos de transferencia de datos
├── Dao/                  → Acceso a datos (en algunos módulos específicos)
├── Entity/               → Entidades JPA mapeadas a la base de datos
├── Repository/           → Interfaces que extienden JpaRepository
├── Service/              → Lógica de negocio e integración
├── config/               → Configuración general de Spring Boot
├── payments/             → Integración con API Wompi
├── security/             → Seguridad, autenticación y roles
├── util/                 → Utilidades y helpers
└── ServiExpressApplication.java  → Clase principal del proyecto

resources/
├── db/migration/         → Scripts de base de datos (Flyway)
├── static/               → Archivos estáticos (CSS, JS, imágenes)
├── templates/            → Vistas Thymeleaf
└── application.properties / application.yml → Configuración del entorno

---

## Instalación y Configuración

### 1️ Requisitos previos

- *Java JDK 17* o superior  
- *Maven 3.8+*  
- *PostgreSQL 14+*  
- *Cuenta en GitHub y conexión a internet*

---

### 2️ Clonar el repositorio

#### Opción 1 – Desde IntelliJ IDEA:
1. Abrir IntelliJ → Get from VCS
2. Pegar la URL:
   ```bash
   https://github.com/Laura2952/ServiceExpress

	3.	Clic en Clone y esperar a que se importe el proyecto.

#### Opción 2 – Desde la terminal:

git clone https://github.com/Laura2952/ServiceExpress
cd ServiceExpress


⸻

1. Crear la base de datos

En PostgreSQL:

CREATE DATABASE db_ServiceExpress;


⸻

2. Configurar el archivo application.properties

spring.application.name=ServiceExpress
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/db_ServiceExpress
spring.datasource.username=user_java
spring.datasource.password=0000
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=true

spring.thymeleaf.cache=false
spring.thymeleaf.mode=HTML

logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.dao=DEBUG

server.servlet.session.persistent=false
spring.flyway.enabled=false

# ---- Wompi ----
wompi.public-key=pub_prod_Sx50Ad2h4xcEmiOMuaxfMfh6aKQ3y4Gx
wompi.integrity-secret=prod_integrity_ZzNXHrAjoCkVtwqgCatbMqsJWiP2v0ic
wompi.currency=COP
wompi.redirect-url=http://localhost:8080/pagos/wompi/callback
wompi.min-amount-cents=500000
# Domicilio fijo (10.000 COP)
wompi.delivery-fee-cents=1000000


⸻

3. Ejecutar el proyecto

Desde IntelliJ IDEA o terminal:

mvn spring-boot:run

O directamente desde la clase principal:

ServiExpressApplication.java → Run

Una vez iniciado, accede desde el navegador:
-> http://localhost:8080￼

⸻

Módulo de Pagos – Wompi

El sistema permite procesar pagos seguros en línea mediante Wompi.
Flujo general:
	1.	El usuario selecciona el servicio a contratar.
	2.	El sistema genera un checkout Wompi con los datos de la transacción.
	3.	Wompi procesa el pago y redirige al endpoint:

/pagos/wompi/callback


	4.	El sistema valida la respuesta y actualiza el estado del servicio.

⸻

Seguridad

El proyecto implementa Spring Security con roles personalizados:
	•	Administrador: gestión global del sistema.
	•	Proveedor: publicación de servicios y gestión de solicitudes.
	•	Cliente: búsqueda y contratación de servicios.

Los usuarios se autentican mediante un CustomUserDetailsService y contraseñas encriptadas (BCrypt).

⸻

Pruebas

Las pruebas unitarias se ubican en:

/src/test/java/com/usta/serviexpress

Para ejecutarlas:

mvn test


⸻

Estructura del Repositorio

/src             → Código fuente principal  
/resources       → Archivos de configuración, vistas y migraciones  
/README.md       → Documentación del proyecto  
/docker-compose.yml → Configuración de contenedores  
/pom.xml         → Dependencias Maven  


⸻

Buenas Prácticas Aplicadas
	•	Control de versiones mediante GitHub.
	•	Uso de ramas:
	•	main → versión estable.
	•	develop → desarrollo activo.
	•	Convenciones de estilo de código Java.
	•	Pruebas unitarias y funcionales por sprint.
	•	Auditorías periódicas de calidad según IEEE 730-2002.

⸻

Autores y Colaboradores

Proyecto desarrollado por el equipo ServiExpress USTA
Universidad Santo Tomás – Ingeniería de Software
2025

⸻

Repositorio

GitHub - ServiExpress
