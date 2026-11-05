# Servicio de gastos

## Descripción general

Microservicio REST para administrar los gastos de Expense Tracker. Protege sus rutas con JWT: el token lo emite el servicio de autenticación y contiene el identificador del usuario al que pertenecen las operaciones.

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

- Crear, consultar, actualizar y eliminar gastos propios.
- Filtrar gastos por mes/año o por rango de fechas.
- Filtrar por importe, método de pago y texto de la descripción.
- Ordenar y paginar resultados.
- Calcular total gastado, número de transacciones y promedio para los filtros seleccionados.
- Validar que el importe sea positivo y que la fecha no sea futura.

## Estructura del servicio

```text
src/
├── main/
│   ├── java/com/donatomarino/expensetracker/expenseservice/
│   │   ├── controllers/       # Endpoints de gastos
│   │   ├── dtos/              # Requests, filtros y respuestas
│   │   ├── entities/          # Entidad Spending
│   │   ├── enums/             # Métodos de pago y ordenamiento
│   │   ├── excepciones/       # Manejo de errores
│   │   ├── repositories/      # Consultas y estadísticas
│   │   ├── security/          # Configuración y filtro JWT
│   │   └── services/          # Lógica de gastos y validación JWT
│   └── resources/
├── test/
│   ├── java/                  # Pruebas de API, servicios y repositorios
│   └── resources/             # Configuración H2
├── Dockerfile
└── pom.xml
```

## Modelo de datos

- `Spending`: gasto guardado en la tabla `spendings`.
- Cada gasto contiene descripción, importe, método de pago, fecha e `user_id`.
- Los registros se consultan y modifican usando conjuntamente el ID del gasto y el ID del usuario autenticado; no se permite operar sobre gastos de otro usuario.

## Rutas API

Base URL local: `http://localhost:8081`.

Todas las rutas requieren el encabezado:

```http
Authorization: Bearer <token>
```

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/spendings` | Lista gastos con filtros, paginación y estadísticas. |
| `POST` | `/api/spendings` | Crea un gasto para el usuario autenticado. |
| `PUT` | `/api/spendings/{id}` | Actualiza un gasto propio. |
| `DELETE` | `/api/spendings/{id}` | Elimina un gasto propio. |

### Crear un gasto

```bash
curl -X POST http://localhost:8081/api/spendings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Compra semanal",
    "amount": 42.50,
    "paymentMethod": "DEBIT_CARD",
    "date": "2026-11-05"
  }'
```

La descripción debe tener entre 4 y 100 caracteres. El importe debe ser positivo y tener hasta dos decimales. Métodos de pago válidos: `CASH`, `CREDIT_CARD`, `TRANSFER` y `DEBIT_CARD`.

Respuesta:

```json
{
  "id": 1,
  "description": "Compra semanal",
  "amount": 42.50,
  "paymentMethod": "DEBIT_CARD",
  "date": "2026-11-05"
}
```

`PUT /api/spendings/{id}` usa el mismo formato de cuerpo que el `POST`.

### Consultar gastos

```bash
curl "http://localhost:8081/api/spendings?year=2026&month=11&page=0&size=10&sort=NEWEST" \
  -H "Authorization: Bearer <token>"
```

Parámetros opcionales:

| Parámetro | Descripción |
|---|---|
| `year` y `month` | Filtra un mes específico. Deben proporcionarse juntos. |
| `startDate` y `endDate` | Rango de fechas `YYYY-MM-DD`. Deben proporcionarse juntos. |
| `minAmount` y `maxAmount` | Límites inferior y superior del importe. |
| `paymentMethod` | `CASH`, `CREDIT_CARD`, `TRANSFER` o `DEBIT_CARD`. |
| `search` | Búsqueda parcial en la descripción. |
| `sort` | `OLDEST`, `NEWEST`, `AMOUNT_LOW_TO_HIGH`, `AMOUNT_HIGH_TO_LOW` o `DESCRIPTION`. |
| `page` y `size` | Página y tamaño de página. Predeterminados: `0` y `10`. |

Si no se indica un filtro de fecha, se consultan los gastos del mes actual. No se pueden combinar `year`/`month` con `startDate`/`endDate`. El resultado incluye `spendings`, metadatos de paginación y estadísticas (`totalSpent`, `totalTransactions`, `average`).

### Eliminar un gasto

```bash
curl -X DELETE http://localhost:8081/api/spendings/1 \
  -H "Authorization: Bearer <token>"
```

Una eliminación correcta responde con `204 No Content`.

## Instalación y configuración

### Requisitos

- Java 17
- PostgreSQL
- Servicio `auth` disponible para obtener tokens

Configura estas variables antes de iniciar el servicio:

```env
JWT_SECRET=<el-mismo-secreto-base64-usado-por-auth>
DB_URL_SPENDINGS=jdbc:postgresql://localhost:5432/spendings_db
DB_USER_SPENDINGS=<usuario-postgres>
DB_PASSWORD=<contraseña-postgres>
```

La base de datos `spendings_db` debe existir. Hibernate usa `ddl-auto: update` para actualizar el esquema de las entidades.

### Ejecutar

Desde el directorio `expenseservice/`:

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
docker build -t expense-tracker-expenses ./expenseservice
```

La imagen expone el puerto `8081`. Al iniciarla, configura las variables anteriores y asegúrate de que la base de datos sea accesible desde el contenedor.

## Pruebas

```bash
./mvnw test
```

Las pruebas usan H2 en memoria y un secreto JWT de prueba; no necesitan la base PostgreSQL de desarrollo.

## Más información

Consulta el [README general](../README.md) para conocer el flujo entre gastos y autenticación.
