# Servicio de autenticación

## Descripción general

Microservicio REST para el registro y autenticación de usuarios de Expense Tracker. Emite tokens JWT firmados que el servicio de gastos valida para identificar al usuario y proteger sus datos.

## Stack tecnológico

- Java 17
- Spring Boot 4.1.0
- Spring MVC y Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- JJWT 0.12.5
- Maven Wrapper
- JUnit y H2 para pruebas

## Funcionalidades principales

- Registro de usuario con nombre, correo electrónico y contraseña.
- Normalización del nombre y correo durante el registro.
- Almacenamiento de la contraseña codificada.
- Inicio de sesión y emisión de JWT.
- Validación de solicitudes y protección mediante Spring Security.

## Estructura del servicio

```text
src/
├── main/
│   ├── java/com/donatomarino/expensetracker/auth/
│   │   ├── controllers/       # Endpoints de autenticación y errores
│   │   ├── dtos/              # Requests y respuestas
│   │   ├── entities/          # Usuario
│   │   ├── repositories/      # Persistencia de usuarios
│   │   ├── security/          # Configuración y filtro JWT
│   │   └── services/          # Autenticación y tokens
│   └── resources/
├── test/
│   ├── java/                  # Pruebas de API, servicios y repositorios
│   └── resources/             # Configuración H2
├── Dockerfile
└── pom.xml
```

## Modelo de datos

- `User`: entidad almacenada en la tabla `users`.
- `email`: identificador único utilizado para iniciar sesión.
- `password`: contraseña codificada; no se devuelve como parte del modelo.
- JWT: incluye el correo como subject y el identificador del usuario en el claim `userId`, que consume el servicio de gastos.

## Rutas API

Base URL local: `http://localhost:8080`.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registra una cuenta. No inicia sesión ni devuelve un token. |
| `POST` | `/auth/login` | Valida credenciales y devuelve un JWT. |

### Registro

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "full_name": "Ada Lovelace",
    "email": "ada@example.com",
    "password": "un-ejemplo-de-clave"
  }'
```

`full_name` debe contener nombre y apellido, con una longitud de entre 3 y 64 caracteres. El correo debe tener formato válido y la contraseña debe tener entre 6 y 64 caracteres.

Respuesta:

```json
{
  "message": "User registered successfully",
  "success": true
}
```

### Inicio de sesión

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ada@example.com",
    "password": "un-ejemplo-de-clave"
  }'
```

Respuesta:

```json
{
  "access_token": "<token>",
  "token_type": "Bearer",
  "user_name": "Ada",
  "expiresIn": 10800000
}
```

`expiresIn` se expresa en milisegundos y corresponde a la configuración actual del servicio (tres horas).

## Instalación y configuración

### Requisitos

- Java 17
- PostgreSQL

Configura estas variables antes de iniciar el servicio:

```env
JWT_SECRET=<secreto-base64-de-al-menos-32-bytes>
DB_URL_AUTH=jdbc:postgresql://localhost:5432/auth_db
DB_USER_AUTH=<usuario-postgres>
DB_PASSWORD=<contraseña-postgres>
```

La base de datos `auth_db` debe existir. Hibernate está configurado con `ddl-auto: validate`: valida que las tablas existentes coincidan con las entidades, pero no crea el esquema.

### Ejecutar

Desde el directorio `auth/`:

```bash
./mvnw spring-boot:run
```

Para empaquetar el servicio:

```bash
./mvnw package
```

## Uso con Docker

Construye la imagen desde la raíz del repositorio:

```bash
docker build -t expense-tracker-auth ./auth
```

La imagen expone el puerto `8080`. Al iniciarla, configura las variables anteriores y asegúrate de que la base de datos sea accesible desde el contenedor.

## Pruebas

```bash
./mvnw test
```

Las pruebas usan H2 en memoria y una configuración JWT de prueba; no necesitan la base PostgreSQL de desarrollo.

## Más información

Consulta el [README general](../README.md) para conocer el flujo entre autenticación y gastos.
