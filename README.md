# Expense Tracker Backend

## Descripción general

Backend de Expense Tracker compuesto por dos servicios Spring Boot independientes: uno gestiona usuarios y autenticación con JWT, y el otro administra los gastos asociados al usuario autenticado. Cada servicio tiene su propio proyecto Maven, configuración, base de datos y pruebas.

El servicio de autenticación emite tokens firmados que incluyen el identificador del usuario. El servicio de gastos valida esos tokens y usa dicho identificador para aislar las consultas y modificaciones de cada usuario.

## Stack tecnológico

- Java 17
- Spring Boot 4.1.0
- Spring MVC y Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL para los datos de cada servicio
- JSON Web Token (JJWT 0.12.5)
- Maven Wrapper
- JUnit y H2 para pruebas
- Docker (opcional; cada servicio incluye su `Dockerfile`)

## Servicios y funcionalidades

| Servicio | Puerto local | Funcionalidades |
|---|---:|---|
| [`auth`](./auth/README.md) | `8080` | Registro, inicio de sesión y emisión de JWT. |
| [`expenseservice`](./expenseservice/README.md) | `8081` | Consulta filtrada y paginada, estadísticas, creación, actualización y eliminación de gastos propios. |

## Estructura del repositorio

```text
.
├── auth/
│   ├── src/main/java/       # API, seguridad, usuarios y JWT
│   ├── src/test/java/       # Pruebas del servicio de autenticación
│   ├── Dockerfile
│   └── pom.xml
├── expenseservice/
│   ├── src/main/java/       # API, seguridad, gastos y filtros
│   ├── src/test/java/       # Pruebas del servicio de gastos
│   ├── Dockerfile
│   └── pom.xml
└── README.md
```

## Instalación y configuración

### Requisitos

- Java 17
- PostgreSQL
- Docker, si quieres construir las imágenes

Cada servicio incluye `mvnw`; no es necesario instalar Maven globalmente.

### Variables de entorno

Configura las variables antes de ejecutar cada servicio. No guardes contraseñas ni secretos reales en el repositorio.

| Variable | Servicio | Descripción |
|---|---|---|
| `JWT_SECRET` | Ambos | Clave JWT codificada en Base64, igual para los dos servicios. Tras decodificarla debe tener al menos 32 bytes. |
| `DB_URL_AUTH` | `auth` | URL JDBC de la base de autenticación. |
| `DB_USER_AUTH` | `auth` | Usuario de la base de autenticación. |
| `DB_PASSWORD` | Ambos | Contraseña de la base de datos correspondiente. |
| `DB_URL_SPENDINGS` | `expenseservice` | URL JDBC de la base de gastos. |
| `DB_USER_SPENDINGS` | `expenseservice` | Usuario de la base de gastos. |

Ejemplo de formato para una URL JDBC: `jdbc:postgresql://localhost:5432/nombre_base`. Las bases de datos deben existir y ser accesibles desde los servicios.

La configuración de autenticación usa `ddl-auto: validate`, por lo que necesita un esquema compatible con la entidad `users`. El servicio de gastos usa `ddl-auto: update` para el esquema de gastos.

### Ejecutar localmente

Inicia cada servicio desde una terminal distinta, con las variables de entorno correspondientes configuradas:

```bash
cd auth
./mvnw spring-boot:run
```

```bash
cd expenseservice
./mvnw spring-boot:run
```

Las API estarán disponibles en `http://localhost:8080` y `http://localhost:8081`, respectivamente.

## Flujo de autenticación

1. Registra una cuenta o inicia sesión en `auth`.
2. Usa el valor de `access_token` como `Authorization: Bearer <token>` al llamar a `expenseservice`.
3. El servicio de gastos valida la firma con el mismo `JWT_SECRET` y limita las operaciones a los gastos del usuario identificado por el token.

El detalle de endpoints, ejemplos `curl` y configuración está en los README de cada servicio:

- [Servicio de autenticación](./auth/README.md)
- [Servicio de gastos](./expenseservice/README.md)

## Pruebas

Ejecuta cada suite desde el directorio correspondiente. Las configuraciones de prueba usan una base H2 en memoria.

```bash
cd auth && ./mvnw test
```

```bash
cd expenseservice && ./mvnw test
```

## Docker

Desde la raíz del repositorio, construye cada imagen por separado:

```bash
docker build -t expense-tracker-auth ./auth
docker build -t expense-tracker-expenses ./expenseservice
```

Al ejecutar los contenedores, pasa las variables de entorno necesarias y usa una URL de base de datos accesible desde el contenedor. Los puertos internos son `8080` y `8081`. El repositorio no incluye una configuración `docker-compose.yml`.

## Author

Donato Marino
