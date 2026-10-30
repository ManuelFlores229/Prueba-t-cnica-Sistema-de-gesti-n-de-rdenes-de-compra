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
git clone https://github.com/ManuelFlores229/Prueba-t-cnica-Sistema-de-gesti-n-de-rdenes-de-compra.git

cd Prueba-t-cnica-Sistema-de-gesti-n-de-rdenes-de-compra
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

## Endpoints

Todos los endpoints usan el prefijo `/api/v1`. La lista completa con sus esquemas está en Swagger UI.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/clientes` | Registrar un cliente |
| GET | `/clientes` | Listar clientes paginados |
| GET | `/clientes/buscar?nombre=` | Buscar clientes por nombre |
| GET | `/clientes/{id}` | Consultar un cliente |
| PUT | `/clientes/{id}` | Actualizar un cliente |
| DELETE | `/clientes/{id}` | Desactivar un cliente |
| GET | `/clientes/{id}/ordenes` | Órdenes de un cliente |
| POST | `/productos` | Registrar un producto |
| GET | `/productos` | Listar productos con filtros |
| GET | `/productos/{id}` | Consultar un producto |
| PUT | `/productos/{id}` | Actualizar un producto |
| PATCH | `/productos/{id}/stock` | Actualizar existencias |
| DELETE | `/productos/{id}` | Desactivar un producto |
| POST | `/ordenes` | Crear una orden |
| GET | `/ordenes` | Listar órdenes paginadas |
| GET | `/ordenes/{id}` | Consultar una orden |
| GET | `/ordenes/estado?estado=` | Filtrar órdenes por estado |
| GET | `/ordenes/fechas?fechaInicio=&fechaFin=` | Filtrar órdenes por rango de fechas |
| PATCH | `/ordenes/{id}/confirmar` | Confirmar una orden |
| PATCH | `/ordenes/{id}/cancelar` | Cancelar una orden |

### Filtros de productos

`GET /api/v1/productos` acepta estos parámetros opcionales y se pueden combinar:

| Parámetro | Efecto |
|---|---|
| `nombre` | Productos cuyo nombre contiene el texto, sin distinguir mayúsculas |
| `categoria` | Productos de esa categoría |
| `conStock` | Con `true`, solo productos con existencias |
| `precioMin`, `precioMax` | Rango de precios |
| `estado` | `ACTIVO` o `INACTIVO`. Si se omite, solo se muestran los activos |

## Pruebas automatizadas

Las pruebas de integración usan la misma base de datos PostgreSQL de la aplicación, así que antes de ejecutarlas hay que tener PostgreSQL iniciado, la base `Gestion_de_pedidos` creada y la variable `DB_PASSWORD` configurada. Los datos que crean se deshacen al terminar cada prueba.

Para ejecutar todas las pruebas desde la raíz del proyecto:

```bash
./mvnw test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Tipos de pruebas:

| Tipo | Clases | Qué verifican |
|---|---|---|
| Unitarias (Mockito) | `OrdenServiceTest`, `ClienteServiceTest`, `ProductoServiceTest` | Reglas de negocio, cálculo de totales y validaciones, sin base de datos |
| Controladores (`@WebMvcTest`) | `ClienteControllerTest`, `OrdenControllerTest`, `ProductoControllerTest` | Códigos HTTP y validación de los datos de entrada |
| Integración | `OrdenServiceIntegrationTest`, `ApiIntegracionTest` | Peticiones HTTP completas contra la base real, inventario y rollback |

Para ejecutar solo una clase:

```bash
./mvnw -Dtest=ApiIntegracionTest test
```

## Autor

Manuel Flores. Prueba técnica de desarrollo de una API REST para la gestión de órdenes de compra.

Resultado de la última ejecución: NN pruebas, 0 fallos.

## Estructura del código

El código fuente está en `src/main/java/com/example/proyecto2`:

| Paquete | Contenido |
|---|---|
| `Controller` | Endpoints REST |
| `service` | Reglas de negocio y transacciones |
| `repository` | Acceso a datos con Spring Data JPA |
| `entidades` | Entidades JPA y enums de estado |
| `dto` | Objetos de entrada y salida de la API |
| `mapper` | Conversión entre entidades y DTOs |
| `exception` | Excepciones propias y manejador global de errores |

Las pruebas están en `src/test/java`.

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
