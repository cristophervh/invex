# Arquitectura y decisiones

## Capas

```
Cliente ──> RequestLoggingFilter ──> Spring Security (JWT) ──> EmployeeController
                                                                   │  validación (Bean Validation)
                                                                   ▼
                                                            EmployeeService
                                                                   │  reglas de negocio
                                                                   ▼
                                                           EmployeeRepository ──> MySQL / H2
```

El paquete se organiza por funcionalidad (`employee`) y no por capa técnica, para que todo lo de empleados quede junto. Lo transversal (seguridad, logs, errores) vive en sus propios paquetes.

## Decisiones

**DTOs con records y mapper propio.** La entidad JPA nunca sale del servicio. Los records son inmutables y dejan claro qué recibe y qué regresa la API. El mapper es una clase simple; con un solo modelo no vale la pena agregar MapStruct.

**POST acepta un objeto o un arreglo.** Se usa `accept-single-value-as-array` de Jackson, así el mismo endpoint cubre los dos casos sin duplicar código. Todo el lote se guarda en una transacción: si un empleado falla, no se guarda ninguno.

**PUT parcial.** Los campos que no se mandan no se tocan. Un texto vacío en `middleName` lo borra. Se eligió PUT porque así lo pide la prueba; en una API nueva usaría PATCH para la actualización parcial.

**Fechas estrictas.** Por defecto Jackson convierte 31-02 en 29-02 sin avisar. Se agregó un deserializador con `ResolverStyle.STRICT` para que esas fechas se rechacen.

**Edad vs fecha de nacimiento.** La edad se recibe porque es parte del modelo pedido, pero se valida contra la fecha de nacimiento con un `Clock` inyectado, lo que permite probarlo con una fecha fija.

**Duplicados.** Se considera duplicado el mismo nombre, apellidos y fecha de nacimiento, sin importar mayúsculas. Se revisa dentro del mismo lote y contra la base, y se responde 409.

**Flyway y `ddl-auto: validate`.** El esquema se versiona con migraciones; Hibernate solo valida que la entidad coincida. Así H2 y MySQL usan el mismo script.

**Bloqueo optimista.** `@Version` evita que dos actualizaciones simultáneas se pisen. La segunda recibe 409.

**Búsqueda.** Una sola consulta JPQL con `LIKE` sobre los cuatro campos del nombre, en minúsculas. Los caracteres `%` y `_` que mande el usuario se escapan para que se busquen como texto.

**Seguridad.** JWT firmado con HS256 usando el soporte nativo de Spring (OAuth2 Resource Server), sin librerías externas. Los usuarios están en memoria con contraseñas en BCrypt porque la prueba no pide un módulo de usuarios; en un entorno real el token lo emitiría un proveedor de identidad (Keycloak, Azure AD) y este servicio solo lo validaría. CSRF está apagado porque la API no usa cookies ni sesión.

**Errores.** Un `@RestControllerAdvice` convierte cada excepción en el mismo JSON (`ApiError`). Los 401 y 403 de Spring Security usan el mismo formato. Los errores 500 no muestran detalles al cliente, solo se registran en el log.

**Logs.** Un filtro registra una línea por petición con los headers (credenciales enmascaradas) y el tiempo de respuesta. El cuerpo no se registra porque contiene datos personales. El `X-Request-Id` se guarda en el MDC para que aparezca en todas las líneas de esa petición.

**Docker.** Imagen multi-stage: se compila con JDK y se ejecuta con JRE. El jar se separa en capas para que un cambio de código no vuelva a copiar las dependencias. Corre con usuario sin privilegios y grupo 0, compatible con OpenShift.

## Lo que haría con más tiempo

- Paginación en `GET /employees`.
- Baja lógica usando el campo `active` en lugar de borrar el registro.
- Pruebas de integración contra MySQL real con Testcontainers.
- Auditoría de quién creó o modificó cada registro.
