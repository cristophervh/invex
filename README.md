# Employee Service

API REST para administrar empleados: alta (uno o varios), consulta, búsqueda por nombre, actualización parcial o total y baja.

Prueba técnica para el puesto de Java Backend Senior.

## Tecnologías

- Java 17, Spring Boot 2.7.18
- Spring Data JPA (Hibernate), Flyway
- MySQL 8.4 (Docker) y H2 (local y pruebas)
- Spring Security con JWT (OAuth2 Resource Server)
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc, JaCoCo
- Actuator y Micrometer (Prometheus)
- Docker, Docker Compose, GitHub Actions

## Cómo ejecutarlo

### Opción 1: local con H2 (no necesita nada más que Java 17)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Usuarios: `admin / admin123` (lectura y escritura) y `reader / reader123` (solo lectura)

### Opción 2: Docker Compose con MySQL

```bash
cp .env.example .env      # cambia las contraseñas
docker compose up -d --build
docker compose ps         # esperar a que ambos estén "healthy"
```

En este perfil (`prod`) Swagger está apagado. Los usuarios son `admin` y `reader` con las contraseñas del `.env`.

### Pruebas

```bash
./mvnw verify
```

Corre 76 pruebas (unitarias, de controlador, de repositorio e integración) y revisa que la cobertura de líneas sea de al menos 80 %.

## Autenticación

1. Pide un token:

   ```bash
   curl -X POST localhost:8080/auth/token \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. Úsalo en cada petición: `Authorization: Bearer <accessToken>`.

| Rol | Permisos |
|---|---|
| READER | GET |
| ADMIN | GET, POST, PUT, DELETE |

El token dura 30 minutos (`JWT_EXPIRATION_MINUTES`).

## Endpoints

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/employees` | Lista todos | 200 |
| GET | `/employees/{id}` | Busca por id | 200, 404 |
| GET | `/employees/search?name=` | Busca por cualquier parte del nombre o apellidos, sin importar mayúsculas | 200 |
| POST | `/employees` | Crea uno (objeto) o varios (arreglo, máximo 100) | 201, 400, 409 |
| PUT | `/employees/{id}` | Actualiza solo los campos enviados | 200, 400, 404, 409 |
| DELETE | `/employees/{id}` | Elimina | 204, 404 |

Ejemplo de alta:

```json
{
  "firstName": "Ana",
  "middleName": "Maria",
  "lastName": "Lopez",
  "secondLastName": "Diaz",
  "age": 36,
  "gender": "FEMALE",
  "birthDate": "15-05-1990",
  "position": "Backend Developer"
}
```

El contrato completo está en [docs/api/openapi.yaml](docs/api/openapi.yaml) y la colección de Postman en [postman/](postman/).

## Reglas de validación

- Fechas en formato `dd-MM-yyyy`. Fechas que no existen (31-02) se rechazan.
- `firstName`, `lastName`, `secondLastName` y `position` son obligatorios. `middleName` es opcional.
- Los nombres solo aceptan letras, espacios, apóstrofo y guion, hasta 50 caracteres.
- Edad entre 18 y 100, y debe coincidir con la fecha de nacimiento.
- `gender`: `MALE`, `FEMALE` u `OTHER` (sin importar mayúsculas).
- No se permiten dos empleados con el mismo nombre, apellidos y fecha de nacimiento (409).
- Si dos personas actualizan el mismo registro al mismo tiempo, la segunda recibe 409 (bloqueo optimista).

Todos los errores usan el mismo formato:

```json
{
  "timestamp": "07-11-2026 05:15:06",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/employees",
  "errors": [
    { "field": "[0].birthDate", "message": "Invalid value '31-02-1990', expected a valid date with format dd-MM-yyyy" }
  ]
}
```

## Logs

La configuración está en `application.yml`. Cada petición genera una línea con método, ruta, status, tiempo y headers. `Authorization` y `Cookie` se enmascaran. Cada línea lleva un `X-Request-Id` (se respeta si el cliente lo manda y se regresa en la respuesta) para seguir una petición en los logs.

## Monitoreo

| Ruta | Uso |
|---|---|
| `/actuator/health` | Estado general (incluye base de datos) |
| `/actuator/health/liveness`, `/readiness` | Probes para Kubernetes / OpenShift |
| `/actuator/info` | Versión y fecha del build |
| `/actuator/prometheus` | Métricas |

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `DB_URL` | JDBC de MySQL | `jdbc:mysql://localhost:3306/employees` |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales de la base | — |
| `JWT_SECRET` | Llave para firmar tokens, mínimo 32 caracteres | — |
| `JWT_EXPIRATION_MINUTES` | Duración del token | 30 |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Usuario administrador | admin / — |
| `READER_USERNAME`, `READER_PASSWORD` | Usuario de solo lectura | reader / — |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | vacío |

## Git Flow

| Rama | Uso |
|---|---|
| `feature/*` | Desarrollo de cada parte. Se integra a `develop` por PR |
| `develop` | Desarrollo |
| `test` | Pruebas / QA |
| `main` | Producción. Cada release lleva un tag (`v1.0.0`) |

El pipeline de GitHub Actions (`.github/workflows/ci.yml`) compila, corre las pruebas, revisa cobertura, construye la imagen Docker y la escanea con Trivy en cada PR y push a esas ramas.

## Estructura

```
src/main/java/com/invex/employees
├── config        Clock y OpenAPI
├── employee      Entidad, repositorio, servicio, controlador, DTOs y mapper
├── exception     Excepciones y manejador global de errores
├── logging       Filtro de logs de peticiones
├── security      JWT, usuarios y reglas de acceso
└── validation    Fecha estricta y validación edad / fecha de nacimiento
```

Más detalle en [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Evidencias de ejecución en [docs/evidence](docs/evidence/README.md).
