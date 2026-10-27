# API de Gestión de Órdenes de Compra

API REST desarrollada con Java y Spring Boot para administrar clientes, productos y órdenes de compra. El proyecto utiliza PostgreSQL para almacenar la información y permite consultar, crear, actualizar y gestionar los registros mediante endpoints HTTP.

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.0.8
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway para migraciones de base de datos
- Maven
- Bean Validation
- Swagger UI / OpenAPI
- JUnit y Mockito para pruebas automatizadas

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- JDK compatible con la versión Java configurada en `pom.xml` (Java 21).
- PostgreSQL.
- Git, si deseas clonar el repositorio.
- IntelliJ IDEA u otro IDE compatible con Maven.

## Configuración de la base de datos

1. Inicia PostgreSQL.
2. Crea una base de datos llamada `Gestion_de_pedidos`.
3. Configura la contraseña de tu usuario de PostgreSQL mediante la variable de entorno `DB_PASSWORD`.

La configuración de conexión se encuentra en `src/main/resources/application.yml`.

Por seguridad, la contraseña se obtiene de una variable de entorno y no debe escribirse directamente en el código ni subirse al repositorio.

## Ejecución del proyecto

Clona el repositorio y entra en la carpeta del proyecto:

```bash
git clone URL_DEL_REPOSITORIO
cd proyecto2
```

Sustituye `URL_DEL_REPOSITORIO` por la dirección real de tu repositorio Git.

Configura la variable de entorno `DB_PASSWORD` con la contraseña de PostgreSQL antes de iniciar la aplicación.

En Windows PowerShell, por ejemplo:

```powershell
$env:DB_PASSWORD="TU_CONTRASEÑA"
```

Ejecuta la aplicación desde IntelliJ IDEA iniciando la clase `Proyecto2Application`, o utiliza Maven Wrapper desde la terminal:

```bash
./mvnw spring-boot:run
```

En Windows también puedes utilizar:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación estará disponible en:

`http://localhost:8080`

## Documentación de la API

Swagger UI:

`http://localhost:8080/swagger-ui.html`

Especificación OpenAPI en JSON:

`http://localhost:8080/v3/api-docs`

Desde Swagger UI se pueden consultar los endpoints disponibles y probar las operaciones HTTP.

## Principales recursos

### Clientes

Ruta base: `/api/v1/clientes`

Permite registrar, consultar, listar, actualizar y desactivar clientes, buscar por nombre y consultar sus órdenes.

### Productos

Ruta base: `/api/v1/productos`

Permite registrar, consultar, listar, actualizar y desactivar productos, buscar por nombre, consultar por rango de precios y gestionar el inventario.

### Órdenes de compra

Ruta base: `/api/v1/ordenes`

Permite crear órdenes con varios productos, consultar órdenes, filtrar por estado o rango de fechas, confirmar órdenes y cancelarlas.

## Reglas de negocio

- Los totales de las órdenes se calculan en el backend.
- Las órdenes pueden contener varios productos.
- No se permite crear órdenes con clientes o productos inactivos.
- La confirmación valida la disponibilidad de inventario y descuenta las existencias.
- La cancelación de una orden confirmada restaura las existencias correspondientes.
- Las operaciones de negocio que requieren consistencia se gestionan mediante transacciones.
- Se conserva el precio unitario registrado en el detalle de la orden.

## Migraciones de base de datos

Las migraciones se encuentran en `src/main/resources/db/migration`.

Flyway administra la creación y evolución del esquema de base de datos durante el inicio de la aplicación.

Hibernate utiliza la opción `ddl-auto: validate` para validar que el esquema existente sea compatible con las entidades.

## Pruebas automatizadas

Para ejecutar todas las pruebas desde la raíz del proyecto:

```bash
./mvnw test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Para ejecutar la prueba de integración del servicio de órdenes:

```bash
./mvnw -Dtest=OrdenServiceIntegrationTest test
```

Las pruebas automatizadas verifican reglas de negocio, cálculo de totales, transiciones de estado, inventario y manejo de errores.

## Estructura general

El código fuente se encuentra en `src/main/java/com/example/proyecto2`.

La aplicación separa responsabilidades entre controladores REST, servicios, repositorios, entidades y DTOs.

Las pruebas se encuentran en `src/test/java`.

## Estado del proyecto

Prueba técnica (Richard Islas) de desarrollo de una API REST para la gestión de órdenes de compra.
