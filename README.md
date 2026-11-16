# UCO Kudos – Backend

Backend en Java 21 con Spring Boot. El frontend está en otro repositorio: https://github.com/RAMBBIT10/KUDOS-FRONT

Autor: Diego Alejandro Vega

## Contenido

1. [Cómo correrlo](#1-cómo-correrlo)
2. [Atributos de calidad](#2-atributos-de-calidad)
3. [Estilo y tácticas](#3-estilo-y-tácticas)
4. [ABB y SBB](#4-abb-y-sbb)
5. [Tecnologías](#5-tecnologías)
6. [Diseño](#6-diseño)
7. [Lecturas, sistemas reactivos y Twelve-Factor](#7-lecturas-sistemas-reactivos-y-twelve-factor)
8. [Observabilidad](#8-observabilidad)
9. [Decisiones de arquitectura (ADR)](#9-decisiones-de-arquitectura-adr)
10. [Pruebas](#10-pruebas)
11. [Seguridad y DevSecOps](#11-seguridad-y-devsecops)
12. [Despliegue en Azure](#12-despliegue-en-azure)
13. [Evidencias](#13-evidencias)
14. [Uso de inteligencia artificial](#14-uso-de-inteligencia-artificial)
15. [Pendientes](#15-pendientes)

---

## 1. Cómo correrlo

### Cuentas

En UCO Kudos nadie entra sin que el administrador lo apruebe. Para solicitar la cuenta sirve cualquier correo que exista (uco.net.co, gmail.com, hotmail.com, etc.): la clave temporal llega a ese correo y al WhatsApp registrado. El teléfono se escribe con 10 números y el prefijo del país sale de `TELEFONO_PREFIJO`.

El flujo es este:

1. Quien no tiene cuenta usa "Solicitar cuenta" (nombre, correo, rol y teléfono). La solicitud queda `PENDIENTE` y al administrador le llega un correo.
2. El administrador entra al panel y aprueba o rechaza. Al aprobar se crea la cuenta con una clave temporal que le llega a la persona por correo (y por WhatsApp). Al rechazar le llega un correo avisando.
3. La persona ingresa con correo y clave. La clave se guarda con BCrypt y la sesión es un token opaco guardado en Redis que vence a las `SESION_SEGUNDOS`.
4. Si intenta ingresar sin cuenta, con la solicitud pendiente o rechazada, la pantalla le dice exactamente cuál de los tres casos es.

La cuenta del administrador se crea sola al arrancar con `ADMIN_NOMBRE`, `ADMIN_CORREO` y `ADMIN_CLAVE`, si todavía no existe.

### Endpoints

| Método y ruta | Quién entra | Qué hace |
|---|---|---|
| `GET /salud` | Cualquiera | Dice si la API está viva |
| `POST /cuentas/solicitudes` | Cualquiera | Pide una cuenta (nombre, correo, teléfono y rol) |
| `POST /sesion` | Cualquiera | Ingresa con correo y clave, devuelve el token |
| `GET /sesion` | Con sesión | Datos del usuario que tiene la sesión |
| `DELETE /sesion` | Con sesión | Cierra la sesión |
| `GET /cuentas/solicitudes` | Administrador | Solicitudes pendientes |
| `POST /cuentas/solicitudes/{id}/aprobar` | Administrador | Aprueba y manda la clave temporal |
| `POST /cuentas/solicitudes/{id}/rechazar` | Administrador | Rechaza y avisa por correo |
| `GET /usuarios` | Con sesión | Usuarios activos (solo id y nombre) |
| `POST /kudos` | Con sesión | Publica un Kudo (multipart: destinatarioId, mensaje y archivos); el autor es quien tiene la sesión |
| `GET /kudos?limite=10&cursor=...` | Con sesión | Kudos recibidos por quien tiene la sesión, con paginación por cursor |
| `GET /push` | Con sesión | Notificaciones en tiempo real (SSE); el token va en `?token=` porque el navegador no deja mandar cabeceras en un EventSource |
| `GET /archivos/{nombre}` | Cualquiera | Descarga un adjunto |

Las rutas con sesión llevan la cabecera `Authorization: Bearer TOKEN`.

### Probar el flujo completo

Primero pido una cuenta (el correo tiene que cumplir el formato):

```
curl -X POST http://localhost:3000/cuentas/solicitudes -H "Content-Type: application/json" -d "{\"nombre\":\"Ana Gomez\",\"correo\":\"ana.gomez@gmail.com\",\"telefono\":\"+573000000001\",\"rol\":\"ESTUDIANTE\"}"
```

Ingreso como administrador (los datos de `ADMIN_CORREO` y `ADMIN_CLAVE` del `.env`) y copio el `token` de la respuesta:

```
curl -X POST http://localhost:3000/sesion -H "Content-Type: application/json" -d "{\"correo\":\"ADMIN_CORREO\",\"clave\":\"ADMIN_CLAVE\"}"
```

Veo las solicitudes pendientes y apruebo la de Ana con su `id`:

```
curl http://localhost:3000/cuentas/solicitudes -H "Authorization: Bearer TOKEN_ADMIN"
curl -X POST http://localhost:3000/cuentas/solicitudes/ID_DE_ANA/aprobar -H "Authorization: Bearer TOKEN_ADMIN"
```

A Ana le llega la clave temporal por correo. Repito el proceso con un segundo usuario (Luis) y los dos ingresan con `POST /sesion`. Con el token de Ana publico un Kudo para Luis (el `id` de Luis sale de `GET /usuarios`):

```
curl -X POST http://localhost:3000/kudos -H "Authorization: Bearer TOKEN_ANA" -F destinatarioId=ID_DE_LUIS -F mensaje="Gracias por ayudarme con el proyecto" -F "archivos=@nota.pdf;type=application/pdf"
```

Para ver los Kudos de Luis uso su token:

```
curl "http://localhost:3000/kudos?limite=10" -H "Authorization: Bearer TOKEN_LUIS"
```

Para ver la auditoría:

```
docker compose exec base-datos psql -U kudos -d kudos -c "select evento, canal, estado, detalle from auditoria order by creado_en"
```

Debe aparecer una fila `KUDO_CREADO` y una fila `NOTIFICACION` por cada canal (correo, whatsapp y push).

Para probar el push dejo este comando abierto en otra terminal antes de publicar:

```
curl -N "http://localhost:3000/push?token=TOKEN_LUIS"
```

Cuando publico el Kudo para Luis, esa terminal muestra un evento `kudo-recibido`. Si nadie está conectado, el canal push queda como OMITIDO en la auditoría.

### Catálogo (Strapi)

Los textos de los mensajes y las plantillas de las notificaciones viven en Strapi, un servicio aparte con su propia base de datos. Se levanta con Docker y se siembra solo la primera vez, desde `strapi/datos/mensajes.json` y `strapi/datos/notificaciones.json`.

```
docker compose up -d base-datos redis base-datos-strapi strapi
```

Tarda cerca de un minuto en arrancar. Cuando termine, `http://localhost:1337/api/mensajes` devuelve los textos. El panel para editarlos está en `http://localhost:1337/admin`: la primera vez pide crear el usuario administrador, y después se editan las colecciones Mensaje y Notificacion (el backend las lee cada 60 segundos, sin redesplegar).

### Correo y WhatsApp

**Correo (Gmail):** activo la verificación en dos pasos, creo una contraseña de aplicación y en `.env` pongo `CORREO_HOST=smtp.gmail.com`, `CORREO_PUERTO=587`, `CORREO_USUARIO` con mi correo, `CORREO_CLAVE` con la contraseña de aplicación y `CORREO_REMITENTE` con mi correo.

**WhatsApp (sandbox de Twilio):** creo una cuenta en Twilio, activo el sandbox de WhatsApp y mando el código de unión desde el celular que va a recibir los mensajes. En `.env` pongo `WHATSAPP_CUENTA_SID`, `WHATSAPP_TOKEN` y `WHATSAPP_NUMERO_ORIGEN`. El teléfono del destinatario tiene que ser el que se unió al sandbox, con el prefijo del país.

### Variables de entorno

Están todas en `.env.example`. Ninguna está escrita dentro del código.

| Variable | Para qué sirve |
|---|---|
| `PUERTO` | Puerto de la API |
| `BD_HOST`, `BD_PUERTO`, `BD_USUARIO`, `BD_CLAVE`, `BD_NOMBRE` | Conexión a PostgreSQL |
| `BD_SSLMODE` | `prefer` en local y `require` en Azure |
| `BD_ESQUEMA` | `update` crea y actualiza las tablas solo; `validate` solo revisa |
| `REDIS_HOST`, `REDIS_PUERTO`, `REDIS_CLAVE`, `REDIS_SSL` | Conexión a Redis (en Azure: puerto 6380, clave y SSL en `true`) |
| `CORREO_*` | Servidor SMTP |
| `WHATSAPP_*` | Cuenta de Twilio, la ruta de envío (`WHATSAPP_RUTA_ENVIO`) y el prefijo del número (`WHATSAPP_PREFIJO`) |
| `CANAL_CORREO`, `CANAL_WHATSAPP`, `CANAL_PUSH` | Nombre de cada canal, que también es la clave de su plantilla en el catálogo |
| `STRAPI_URL`, `STRAPI_TOKEN` | Dirección del catálogo y token opcional (la lectura es pública) |
| `STRAPI_RUTA_*`, `STRAPI_CAMPO_*` | Rutas y campos de las colecciones de Strapi (tienen valores por defecto) |
| `STRAPI_BD_*`, `STRAPI_APP_KEYS`, `STRAPI_*_SALT`, `STRAPI_*_SECRET`, `STRAPI_ENCRYPTION_KEY` | Base de datos y secretos del propio Strapi |
| `ARCHIVOS_CARPETA`, `ARCHIVOS_TAMANO_MAXIMO`, `ARCHIVOS_TIPOS_PERMITIDOS` | Reglas de los adjuntos |
| `ADMIN_NOMBRE`, `ADMIN_CORREO`, `ADMIN_TELEFONO`, `ADMIN_CLAVE` | Cuenta del administrador, que se crea sola al arrancar; su correo recibe los avisos de solicitudes |
| `TELEFONO_PREFIJO` | Prefijo del país que el sistema le agrega al teléfono de 10 números que escribe el usuario (por defecto `+57`) |
| `SESION_SEGUNDOS`, `SESION_PREFIJO` | Vida de la sesión en Redis y prefijo de sus llaves |
| `CLAVE_TEMPORAL_LONGITUD` | Largo de la clave temporal que se manda al aprobar |
| `PLANTILLA_SOLICITUD`, `PLANTILLA_APROBADA`, `PLANTILLA_RECHAZADA`, `PLANTILLA_APROBADA_WHATSAPP` | Nombres de las plantillas de cuentas en Strapi (tienen valores por defecto) |
| `CACHE_SEGUNDOS` | Vida de la caché |
| `CORS_ORIGENES` | Orígenes permitidos (en producción, el dominio del frontend) |
| `COLA_NOMBRE`, `COLA_INTENTOS`, `COLA_ESPERA_BASE`, `COLA_CANALES` | Cola de notificaciones (tienen valores por defecto) |

### Problemas que me han salido

| Problema | Qué reviso |
|---|---|
| `Could not resolve placeholder` al arrancar | Que `.env` exista en la carpeta desde donde ejecuto y tenga todas las variables de `.env.example` |
| Error de conexión a la base | Que el contenedor `base-datos` esté arriba (`docker compose ps`) |
| `no existe el rol kudos` | Que no haya otro Postgres instalado en el computador usando el puerto 5432 |
| Maven usa otra versión de Java | Que `JAVA_HOME` apunte a la 21 |
| Los mensajes de error salen como código (`AUTOR_NO_EXISTE`) | Que Strapi esté arriba (`docker compose ps`) y que `STRAPI_URL` apunte a él |
| El correo falla | Que `CORREO_HOST`, `CORREO_PUERTO` y la contraseña de aplicación sean los reales |
| WhatsApp falla | Que el teléfono se haya unido al sandbox de Twilio |
| Error 400 al subir un archivo | Que el tipo esté permitido, no pase el tamaño máximo y sean máximo 5 archivos |

---

## 2. Atributos de calidad

### AC-01 Rendimiento en lectura

| Campo | Descripción |
|---|---|
| Código | AC-01 |
| Nombre | Respuesta rápida al consultar Kudos |
| Objetivo | Que ver los Kudos recibidos sea rápido aunque haya mucha gente consultando |
| Criterio de éxito | La consulta repetida no vuelve a golpear la base de datos |
| Prerrequisitos | Redis disponible |
| Fuente del estímulo | Usuario desde el celular |
| Estímulo | Pide la lista de sus Kudos varias veces seguidas |
| Ambiente | Operación normal con alta carga de lectura |
| Artefacto | Caso de uso `ListarKudos` y `CacheKudosRedis` |
| Respuesta | Devuelve la lista desde la caché si existe; si no, consulta la base y la guarda |
| Medida de la respuesta | La segunda consulta idéntica dentro de la vida de la caché (60 s) no ejecuta consulta SQL |

### AC-02 Resiliencia de las notificaciones

| Campo | Descripción |
|---|---|
| Código | AC-02 |
| Nombre | Un canal caído no tumba a los demás |
| Objetivo | Que el Kudo se publique aunque falle el correo o WhatsApp |
| Criterio de éxito | El Kudo queda guardado y los canales sanos se entregan |
| Prerrequisitos | Cola en Redis activa |
| Fuente del estímulo | Proveedor externo (SMTP o Twilio) |
| Estímulo | El proveedor no responde o devuelve error |
| Ambiente | Operación normal |
| Artefacto | `ProcesadorNotificaciones`, `EnviarNotificacion` y la cola en Redis |
| Respuesta | Cada canal es un trabajo aparte con 3 reintentos y espera creciente; el error queda en auditoría |
| Medida de la respuesta | La API responde 201 aunque falle un canal, y la tabla `auditoria` muestra el estado ERROR de ese canal |

### AC-03 Modificabilidad

| Campo | Descripción |
|---|---|
| Código | AC-03 |
| Nombre | Agregar un canal de notificación nuevo |
| Objetivo | Poder sumar, por ejemplo, Telegram sin tocar la lógica del negocio |
| Criterio de éxito | Solo se agrega una clase y se registra |
| Prerrequisitos | Contrato `Notificador` definido |
| Fuente del estímulo | Desarrollador |
| Estímulo | Se pide un canal nuevo |
| Ambiente | Desarrollo |
| Artefacto | Interfaz `Notificador` y `EnviarNotificacion` |
| Respuesta | Se crea una clase que cumple el contrato y Spring la registra |
| Medida de la respuesta | Los casos de uso `PublicarKudo` y `EnviarNotificacion` no cambian |

### AC-04 Seguridad en los adjuntos

| Campo | Descripción |
|---|---|
| Código | AC-04 |
| Nombre | Subida segura de archivos |
| Objetivo | Evitar archivos peligrosos o demasiado grandes |
| Criterio de éxito | Solo se aceptan los tipos y tamaños permitidos |
| Prerrequisitos | Variables `ARCHIVOS_TIPOS_PERMITIDOS` y `ARCHIVOS_TAMANO_MAXIMO` configuradas |
| Fuente del estímulo | Usuario |
| Estímulo | Intenta subir un archivo ejecutable o de 50 MB |
| Ambiente | Operación normal |
| Artefacto | `KudosControlador` y `AlmacenArchivos` |
| Respuesta | Rechaza el archivo con error 400; los permitidos se guardan con nombre aleatorio |
| Medida de la respuesta | Un `.exe` o un archivo mayor al límite nunca llega a la carpeta de subidas |

---

## 3. Estilo y tácticas

### Estilo

Combiné tres ideas:

- **Monolito modular:** una sola aplicación con módulos separados (`kudos`, `notificaciones`, `auditoria`). Es lo más simple para un trabajo individual, y los módulos quedan listos para separarse si hace falta.
- **Arquitectura limpia (hexagonal):** el negocio no depende de la base de datos ni de los proveedores. Habla con puertos y las implementaciones se enchufan por fuera.
- **Orientada a eventos:** publicar un Kudo genera trabajos en una cola, y cada canal los consume por su cuenta.

### Tácticas

| Atributo | Táctica | Dónde se aplica |
|---|---|---|
| Rendimiento | Caché cache-aside | `CacheKudosRedis` |
| Rendimiento | Paginación por cursor | `ListarKudos` |
| Rendimiento | Índice en `kudo (destinatarioId, creadoEn)` | Entidad del Kudo |
| Rendimiento | Procesamiento asíncrono | Cola en Redis y `ProcesadorNotificaciones` |
| Disponibilidad | Reintentos con espera creciente | `ProcesadorNotificaciones` |
| Disponibilidad | Aislamiento de fallos (un trabajo por canal) | Cola de notificaciones |
| Modificabilidad | Puertos y adaptadores | Interfaces en `kudos/aplicacion/puertos` |
| Modificabilidad | Configuración externa | `ConfiguracionKudos` y `application.yml` |
| Seguridad | Validación de entradas | Bean Validation y reglas del dominio |
| Seguridad | Limitar y filtrar archivos | `AlmacenArchivos` |
| Seguridad | Cabeceras seguras | Spring Security |
| Trazabilidad | Auditoría por evento | `AuditoriaRepositorioPostgres` |
| Modificabilidad | Textos fuera del código (catálogos) | Módulo `catalogos` |

---

## 4. ABB y SBB

Primero definí qué se necesita (ABB) y después con qué lo resolví (SBB).

| ABB (qué se necesita) | SBB (con qué lo resolví) | Estado |
|---|---|---|
| ABB-01 Gestión de Kudos | Módulo `kudos` | Hecho |
| ABB-02 Gestión de adjuntos | `AlmacenArchivos`, carpeta de subidas | Hecho |
| ABB-03 Notificaciones | Módulo `notificaciones` con cola en Redis | Hecho |
| ABB-04 Canal de correo | `NotificadorCorreo` (JavaMailSender) | Hecho |
| ABB-05 Canal de WhatsApp | `NotificadorWhatsapp` (Twilio) | Hecho |
| ABB-06 Canal push | `NotificadorPush` y `ConexionesPush` (Server-Sent Events) | Hecho |
| ABB-07 Auditoría | Módulo `auditoria`, tabla `auditoria` | Hecho |
| ABB-08 Mensajería asíncrona | Lista de Redis como cola | Hecho |
| ABB-09 Caché | Redis, `CacheKudosRedis` | Hecho |
| ABB-10 Identidad y acceso | Cuentas aprobadas por el administrador, BCrypt, sesión en Redis y Spring Security | Hecho |
| ABB-11 Observabilidad | Endpoint `/salud`, auditoría | Parcial |
| ABB-13 Catálogos de mensajes y notificaciones | Módulo `catalogos` consumiendo Strapi (CMS) | Hecho |
| ABB-12 Entrega continua | GitHub Actions, SonarCloud, Trivy | Archivos listos, falta ejecutarlos |
| ABB-14 Baúl de secretos | Azure Key Vault, leído por los App Service con referencias; en local, `.env` | Pendiente (Azure) |

---

## 5. Tecnologías

| Pieza | Tecnología | Por qué |
|---|---|---|
| Backend | Java 21 con Spring Boot 3.3 y Maven | Es lo que mejor domino y ya trae inyección de dependencias, JPA, validación y pruebas |
| Frontend | React con Vite (PWA) | Liviano e instalable en el celular |
| Base de datos | PostgreSQL con Spring Data JPA | Datos relacionales |
| Caché y cola | Redis (Spring Data Redis) | Un solo servicio para las dos cosas |
| Tiempo real | Server-Sent Events (SSE) | Canal simple del servidor al navegador que se reconecta solo |
| Correo | SMTP con Spring Mail | Funciona con cualquier proveedor |
| WhatsApp | API de Twilio (con `RestClient`) | Tiene sandbox gratis |
| Catálogos | Strapi 5 (CMS) con su propia PostgreSQL | Los textos se editan en un panel, sin tocar código ni redesplegar el backend |
| Calidad | SonarCloud | Análisis estático |
| Seguridad | Trivy | Vulnerabilidades en dependencias e imagen |
| CI/CD | GitHub Actions | Está dentro del repositorio |
| Contenedores | Docker | Corre igual en mi computador y en la nube |

---

## 6. Diseño

### Arquitectura limpia

```
src/main/java/co/edu/uco/kudos/
  kudos/
    dominio/          Entidades y reglas (Kudo, Usuario, Adjunto)
    aplicacion/       Casos de uso (PublicarKudo, ListarKudos) y puertos
    infraestructura/  Postgres, Redis, la cola y los controladores
  notificaciones/     Canales de correo, WhatsApp y push
  catalogos/          Cliente de Strapi y catálogos de mensajes y plantillas de notificación
  auditoria/          Registro de cada paso
  compartido/         Configuración leída de variables de entorno
```

Las dependencias apuntan hacia adentro: la infraestructura conoce a la aplicación, la aplicación conoce al dominio, y el dominio no conoce a nadie.

Puertos principales: `KudoRepositorio`, `UsuarioRepositorio`, `AuditoriaRepositorio`, `PublicadorEventos`, `CacheKudos` y `Notificador`.

### Qué pasa al publicar un Kudo

1. El controlador recibe el mensaje y los archivos.
2. `PublicarKudo` verifica que existan el autor y el destinatario, y que el mensaje cumpla las reglas.
3. Guarda el Kudo y los adjuntos en Postgres.
4. Deja la auditoría `KUDO_CREADO`.
5. Borra la caché del destinatario.
6. Crea en Redis un trabajo por cada canal.
7. Responde al cliente.
8. `ProcesadorNotificaciones` toma cada trabajo y `EnviarNotificacion` lo manda por correo, WhatsApp o push, y deja la auditoría con OK, ERROR u OMITIDO.

### Modelo de datos

| Tabla | Campos |
|---|---|
| usuario | id, nombre, correo, telefono, rol (ESTUDIANTE, PROFESOR, ADMINISTRADOR), estado (PENDIENTE, ACTIVO, RECHAZADO), claveCifrada (BCrypt) |
| kudo | id, autorId, destinatarioId, mensaje, creadoEn |
| adjunto | id, nombre, tipo, tamano, url, kudo |
| auditoria | id, kudoId, evento, canal, estado, detalle, creadoEn |

### SOLID

| Principio | Dónde se ve |
|---|---|
| Responsabilidad única | Cada caso de uso hace una sola cosa |
| Abierto/cerrado | Un canal nuevo es una clase nueva, sin cambiar lo existente |
| Sustitución de Liskov | Correo, WhatsApp y push cumplen el mismo contrato `Notificador` |
| Segregación de interfaces | Puertos pequeños y separados |
| Inversión de dependencias | Los casos de uso piden puertos en el constructor y Spring inyecta la implementación |

### Patrones de diseño

| Patrón | Dónde |
|---|---|
| Repository | `KudoRepositorioPostgres`, `UsuarioRepositorioPostgres` |
| Adapter | Cada notificador adapta un servicio externo |
| Strategy | `EnviarNotificacion` elige el notificador según el canal |
| Publicador/Suscriptor | `PublicadorEventosCola` y `ProcesadorNotificaciones` |

---

### Catálogos

Los textos no están escritos dentro del código. Están en Strapi, que corre como un servicio aparte (carpeta `strapi/`, con su propia base PostgreSQL) y expone dos colecciones por HTTP. Al arrancar por primera vez se siembra desde `strapi/datos/*.json`, y el rol público solo tiene permiso de lectura: escribir sin token responde 403.

| Catálogo | Colección en Strapi | Qué guarda |
|---|---|---|
| Mensajes | `mensajes` | Código y texto de cada respuesta de error (por ejemplo `AUTOR_NO_EXISTE`) |
| Notificaciones | `notificaciones` | Canal, asunto y cuerpo, con las marcas `{nombre}` y `{mensaje}` |
| Parámetros | Variables de entorno | Límites y valores de configuración, en `ConfiguracionKudos` |

Los casos de uso lanzan una excepción con un **código** y `ManejadorDeErrores` lo convierte en texto pidiéndolo al catálogo. Los notificadores de correo y WhatsApp piden su plantilla por canal en vez de armar el texto a mano. Las dos consultas pasan por `StrapiCliente` y se guardan en una caché de `CACHE_SEGUNDOS` en el backend.

Las rutas, los campos y los nombres de los canales salen de variables de entorno (`STRAPI_RUTA_*`, `STRAPI_CAMPO_*`, `CANAL_*`), no del código. Si Strapi no responde, el mensaje de error sale como su código y el envío de la notificación queda auditado como ERROR y se reintenta desde la cola. Para cambiar un texto se edita la fila en el panel de Strapi, sin tocar Java ni volver a desplegar.

## 7. Lecturas, sistemas reactivos y Twelve-Factor

### Cómo respondo a muchas lecturas

1. Caché en Redis de 60 segundos para la lista de cada destinatario.
2. Esa caché se borra cada vez que llega un Kudo nuevo.
3. Paginación por cursor (la fecha del último Kudo) en vez de `OFFSET`.
4. Índice en la tabla de Kudos para esa consulta.
5. Los envíos pesados quedan fuera del camino de lectura.
6. La API no guarda estado, así que se pueden levantar varias copias.

Limitación: los archivos hoy los sirve la misma API. Con mucho tráfico lo correcto sería un almacenamiento de objetos o un CDN.

### Manifiesto de Sistemas Reactivos

| Pilar | Cómo lo apliqué |
|---|---|
| Responsivo | La API responde sin esperar los envíos |
| Resiliente | Cada canal tiene su trabajo, con 3 reintentos y espera creciente |
| Elástico | API sin estado; el procesador puede correr en otro proceso |
| Orientado a mensajes | Publicar y enviar se comunican por una cola |

### The Twelve-Factor App

| Factor | Cómo lo cumplo |
|---|---|
| I. Código base | Un repositorio por componente |
| II. Dependencias | Declaradas en el `pom.xml` |
| III. Configuración | Todo sale de variables de entorno (`.env.example`) |
| IV. Servicios de respaldo | Postgres, Redis, SMTP, Twilio y Strapi se conectan por configuración |
| V. Build, release, run | El pipeline compila, construye la imagen y después despliega |
| VI. Procesos | La API no guarda estado entre peticiones |
| VII. Vinculación de puertos | Escucha el puerto de la variable `PUERTO` |
| VIII. Concurrencia | Se escala levantando más procesos |
| IX. Desechabilidad | Cierre ordenado con `server.shutdown: graceful` |
| X. Paridad | El mismo Docker en local y en la nube |
| XI. Logs | Spring Boot escribe a la salida estándar |
| XII. Administración | Tareas con comandos de Maven |

---

## 8. Observabilidad

- **Salud:** `GET /salud` responde si la API está viva.
- **Auditoría:** cada Kudo deja rastro en la tabla `auditoria` (evento, canal y estado OK, ERROR u OMITIDO).
- **Logs:** salen por la salida estándar y se ven con `docker logs`.

---

## 9. Decisiones de arquitectura (ADR)

**ADR-01 Monolito modular.** Contexto: trabajo individual con pocos días y sin presupuesto. Decisión: una sola aplicación Spring Boot con paquetes separados por dominio. Alternativa: microservicios. Consecuencias: más fácil de desarrollar, desplegar y explicar; escala como una sola unidad, pero los módulos están separados para poder dividirlos después.

**ADR-02 Arquitectura limpia (hexagonal).** Contexto: debe poder cambiarse de proveedor de correo o WhatsApp, y los casos de uso deben poder probarse sin base de datos. Decisión: dominio, aplicación e infraestructura separados, con puertos entre ellos. Alternativa: capas tradicionales donde el servicio usa directamente el repositorio y el proveedor. Consecuencias: hay más archivos, pero las pruebas unitarias son simples y cambiar un proveedor es cambiar una clase.

**ADR-03 Java 21 con Spring Boot y Maven.** Contexto: necesito un framework con inyección de dependencias, persistencia, validación y pruebas, y debo poder explicarlo y modificarlo en vivo en la sustentación. Decisión: Java 21 con Spring Boot 3.3 y Maven; para el frontend, una PWA. Alternativas: NestJS con TypeScript (llegué a construir una primera versión) y Express sin estructura. Consecuencias: es la tecnología que mejor domino, así que puedo explicar cada parte y hacer cambios en vivo con más seguridad. Costó reescribir el backend, pero la arquitectura y los puertos se mantuvieron. Se requiere JDK 21.

**ADR-04 Redis para caché y cola.** Contexto: necesito caché para las lecturas y una cola para las notificaciones. Decisión: un solo Redis, con una lista como cola y un procesador propio que la lee. Alternativas: Kafka o RabbitMQ. Consecuencias: menos piezas que montar y explicar. Limitación: una lista de Redis no tiene confirmación de entrega, así que si la aplicación se cae justo después de sacar un trabajo, ese trabajo se pierde (la auditoría deja ver qué canal no quedó registrado). Si el volumen o la exigencia crecen, se puede reemplazar por un broker como RabbitMQ gracias al puerto `PublicadorEventos`.

**ADR-05 Notificaciones asíncronas, un trabajo por canal.** Contexto: enviar correo y WhatsApp es lento y puede fallar. Decisión: al publicar se crea un trabajo en la cola por cada canal, con 3 reintentos y espera creciente. Alternativa: enviar todo dentro de la misma petición. Consecuencias: la API responde rápido y un canal caído no afecta a los demás; la entrega es eventual y por eso la auditoría es importante.

**ADR-06 PWA para el frontend.** Contexto: el 90% de los usuarios entra desde el celular. Decisión: una aplicación web instalable con React. Alternativa: app nativa. Consecuencias: un solo código para todos los dispositivos y sin tiendas de apps; algunas funciones nativas quedan limitadas.

**ADR-07 Server-Sent Events para el push.** Contexto: el destinatario debe enterarse al instante si está conectado. Decisión: el navegador abre `GET /push/{usuarioId}` y el servidor le envía eventos; la aplicación guarda en memoria qué usuarios están conectados. Alternativas: WebSocket o Socket.IO, o consultar al servidor cada cierto tiempo. Consecuencias: llega al instante, el navegador lo soporta con `EventSource` y reconecta solo. Solo va del servidor al cliente, que es todo lo que necesita una notificación. Limitación: el mapa de conectados vive en memoria; con varias copias de la API habría que repartir los eventos por Redis.

**ADR-08 Paginación por cursor.** Contexto: habrá muchas lecturas y listas largas. Decisión: el cursor es la fecha del último Kudo recibido. Alternativa: `OFFSET` y `LIMIT`. Consecuencias: mantiene el rendimiento con muchos registros; no permite saltar a una página arbitraria, algo que en un feed no hace falta.

**ADR-09 Adjuntos en disco con metadatos en la base.** Contexto: los Kudos pueden llevar archivos, pero no quiero guardar binarios en la base. Decisión: los archivos van en una carpeta con nombre aleatorio y en la base solo nombre, tipo, tamaño y ruta. Alternativa: almacenamiento de objetos. Consecuencias: simple para esta entrega. En la nube los discos de los contenedores se pierden al reiniciar (en Azure se mitiga con `WEBSITES_ENABLE_APP_SERVICE_STORAGE`), y lo correcto a futuro es pasar a almacenamiento de objetos.

**ADR-10 SonarCloud y Trivy en el pipeline.** Contexto: el enunciado exige análisis estático y de vulnerabilidades. Decisión: SonarCloud para el código y Trivy para dependencias e imagen Docker, ambos en el CI. Consecuencias: son las herramientas que pide el enunciado y tienen capa gratuita. Si fallan, el pipeline se detiene.

---

## 10. Pruebas

Tengo muchas pruebas rápidas abajo, algunas de integración en el medio y pocas de extremo a extremo arriba.

| Nivel | Qué prueba | Herramienta | Cantidad | Estado |
|---|---|---|---|---|
| Unitarias | Casos de uso, reglas del dominio, cuentas, push y catálogos (con un Strapi falso en memoria) | JUnit 5 y Mockito | 57 | Por correr con `mvn test` después de los cambios de cuentas |
| Integración | La API completa con Postgres y Redis reales | Spring Boot Test y MockMvc | 11 | Escritas, falta registrar su ejecución |
| Extremo a extremo | Flujo completo desde el frontend | Pendiente | 0 | Pendiente |

### Unitarias

Terminan en `Test` y no necesitan base de datos. Se corren con `mvn test`.

| Clase | Qué comprueba |
|---|---|
| `PublicarKudoTest` | Publicación correcta, mensaje vacío, destinatario inexistente y autor inexistente |
| `EnviarNotificacionTest` | Auditoría OK, OMITIDO y ERROR (con el error vuelto a lanzar para que reintente), canal inexistente y kudo inexistente |
| `ListarKudosTest` | Respuesta desde caché, consulta a la base cuando no hay caché, siguiente cursor, uso del cursor y cursor inválido |
| `ReglasMensajeTest` | Regla del mensaje: vacío, nulo, en el límite y por encima del límite |
| `NotificadorPushTest` | Push enviado si está conectado, omitido si no, y manejo de las conexiones |
| `PlantillaNotificacionTest` | Reemplazo del nombre y del mensaje en la plantilla |
| `StrapiClienteTest` | La consulta que se manda a Strapi, la primera fila de la respuesta, vacío si no hay filas y el token solo cuando está configurado |
| `CatalogoMensajesStrapiTest` | Texto del código, el código mismo si no existe o si Strapi falla, y que la caché evita volver a preguntar |
| `CatalogoNotificacionesStrapiTest` | Plantilla con asunto y cuerpo, asunto vacío si el canal no lo tiene y error si el canal no tiene plantilla |
| `ReglasCuentaTest` | Formato del correo (cualquier dominio), teléfono de 10 números y nombre |
| `SolicitarCuentaTest` | Solicitud pendiente con aviso al administrador, correo con formato inválido, rol administrador y correo repetido |
| `ResolverSolicitudTest` | Aprobar (clave cifrada y aviso), rechazar, solicitud ya resuelta y solicitud inexistente |
| `IniciarSesionTest` | Ingreso correcto, sin cuenta, pendiente, rechazada y clave incorrecta |
| `ManejadorDeErroresTest` | Que cada error responda con el estado correcto y el texto sacado del catálogo |

### Integración

Terminan en `IT` y están en `FlujoKudoIT`. Levantan la aplicación completa contra Postgres y Redis reales; solo reemplazo los proveedores externos (correo, WhatsApp, los avisos de cuentas y el catálogo de mensajes). Se corren con `mvn verify`, con Postgres y Redis arriba y las variables de `.env` cargadas.

| Prueba | Qué comprueba |
|---|---|
| Publicar con adjunto | Con sesión, guarda el Kudo y el adjunto, y la auditoría tiene `KUDO_CREADO` y una fila por cada canal |
| Paginación por cursor | Primera página con 2 Kudos y siguiente cursor; segunda página con el último y sin cursor |
| Caché | Un Kudo nuevo aparece aunque la lista ya estuviera cacheada (se invalida) |
| Mensaje vacío | Responde 400 |
| Archivo no permitido | Un `.exe` se rechaza con 400 |
| Destinatario inexistente | Responde 404 |
| Sin sesión | Publicar sin token responde 401 con el texto del catálogo |
| Panel de solicitudes | Un estudiante recibe 403 y el administrador 200 |
| Aprobar solicitud | Pendiente no puede ingresar; el administrador aprueba y la cuenta queda activa |
| Cuenta inexistente | Ingresar con un correo sin cuenta responde 404 con su mensaje |
| Salud | `GET /salud` responde `ok` |

### Comandos

| Comando | Qué corre |
|---|---|
| `mvn test` | Solo las pruebas unitarias (no necesitan base de datos) |
| `mvn verify` | Unitarias y de integración, con cobertura (reporte en `target/site/jacoco/index.html`) |

### Falta

- Registrar la ejecución de las 68 pruebas y la cobertura en el CI.
- Prueba de extremo a extremo con el frontend.
- Probar las notificaciones contra el sandbox real de Twilio y un SMTP real.

---

## 11. Seguridad y DevSecOps

### Desarrollo seguro

| Medida | Dónde |
|---|---|
| Validación de datos de entrada (ids, correo, longitud del mensaje) | Bean Validation y reglas del dominio |
| Consultas parametrizadas, sin SQL armado a mano | Spring Data JPA con Hibernate |
| Límite de tamaño, de cantidad y lista de tipos permitidos en adjuntos | `AlmacenArchivos` |
| Archivos guardados con nombre aleatorio | `AlmacenArchivos` |
| Cabeceras de seguridad y sesión sin estado | Spring Security (`SeguridadConfig`) |
| Solo entran cuentas aprobadas por el administrador | `SolicitarCuenta`, `ResolverSolicitud` y `ReglasCuenta` |
| Claves guardadas con BCrypt y claves temporales aleatorias (`SecureRandom`) | `CifradorClavesBcrypt` y `GeneradorClavesSeguras` |
| Sesión con token aleatorio en Redis que vence solo; rutas por rol | `SesionesRedis`, `FiltroSesion` y `SeguridadConfig` |
| El autor del Kudo sale de la sesión, no del cuerpo de la petición | `KudosControlador` |
| Secretos fuera del código y del repositorio | Variables de entorno, `.env` en `.gitignore` |
| Mensajes de error sin detalles internos | `ManejadorDeErrores` |
| Usuario sin privilegios dentro del contenedor | `Dockerfile` (`USER kudos`) |
| Imagen final solo con el JRE y el JAR | `Dockerfile` en dos etapas |

Falta: límite de intentos de ingreso y de peticiones por usuario, forzar el cambio de la clave temporal y CORS restringido al dominio del frontend en producción.

### Integración continua (CI)

Archivo `.github/workflows/ci.yml`. Se ejecuta en cada push y pull request a `main`.

| Paso | Herramienta | Falla si |
|---|---|---|
| Compilar | `mvn verify` | Hay errores de compilación |
| Pruebas y cobertura | JUnit y JaCoCo con servicios de Postgres y Redis | Falla alguna prueba |
| Análisis estático | SonarCloud con espera de la puerta de calidad | No pasa la puerta de calidad |
| Vulnerabilidades en dependencias | Trivy (modo sistema de archivos) | Hay vulnerabilidades críticas o altas con arreglo disponible |
| Imagen | Docker | La imagen no construye |
| Vulnerabilidades en la imagen | Trivy (modo imagen) | Hay vulnerabilidades críticas o altas con arreglo disponible |

Los reportes de cobertura y de Trivy se guardan como artefactos de cada ejecución.

Trivy corre desde la imagen oficial `aquasec/trivy:0.69.3` y no desde la acción `trivy-action`: en marzo de 2026 las etiquetas de esa acción fueron comprometidas y las versiones viejas se borraron, así que el pipeline fija una versión conocida y limpia del escáner.

### Entrega continua (CD)

Archivo `.github/workflows/cd.yml`. Se ejecuta solo cuando el CI termina bien en `main`.

1. Construye la imagen Docker y la publica en GitHub Container Registry (con el identificador del commit y `latest`).
2. Le dice a Azure App Service que use esa imagen.
3. Verifica `GET /salud` en la URL pública, con hasta 10 intentos.
4. En un segundo trabajo, construye la imagen de `strapi/` y la despliega en su propio App Service (solo si existe la variable `AZURE_STRAPI_NOMBRE`).

### Secretos y variables en GitHub

Se crean en Settings, Secrets and variables, Actions.

| Nombre | Tipo | Qué es |
|---|---|---|
| `SONAR_TOKEN` | Secreto | Token generado en SonarCloud |
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Secreto | Perfil de publicación que se descarga del App Service |
| `AZURE_STRAPI_PUBLISH_PROFILE` | Secreto | Perfil de publicación del App Service de Strapi |
| `SONAR_PROJECT_KEY` | Variable | Clave del proyecto en SonarCloud |
| `SONAR_ORGANIZATION` | Variable | Organización en SonarCloud |
| `AZURE_WEBAPP_NAME` | Variable | Nombre del App Service |
| `AZURE_STRAPI_NOMBRE` | Variable | Nombre del App Service de Strapi |
| `URL_PUBLICA` | Variable | URL pública del App Service, sin la barra final |

---

## 12. Despliegue en Azure

El despliegue lo hace el pipeline de CD. Recursos:

| Recurso | Servicio |
|---|---|
| API | App Service en Linux, plan B1, con contenedor |
| Base de datos | Azure Database for PostgreSQL Flexible Server |
| Caché y cola | Azure Cache for Redis |
| Catálogo | Segundo App Service en Linux con la imagen de `strapi/`, y su base en el mismo servidor PostgreSQL (una base aparte llamada `strapi`) |
| Frontend | Static Web Apps |

Variables que se cargan en el App Service (Configuración, Variables de entorno), nunca en el repositorio:

| Variable | Valor |
|---|---|
| `PUERTO` y `WEBSITES_PORT` | `8080` |
| `BD_HOST`, `BD_USUARIO`, `BD_CLAVE`, `BD_NOMBRE` | Los del servidor PostgreSQL |
| `BD_SSLMODE` | `require` |
| `REDIS_HOST`, `REDIS_CLAVE` | Los de Azure Cache for Redis |
| `REDIS_PUERTO` | `6380` |
| `REDIS_SSL` | `true` |
| `ARCHIVOS_CARPETA` | `/home/subidas` |
| `WEBSITES_ENABLE_APP_SERVICE_STORAGE` | `true` (para que los adjuntos no se pierdan al reiniciar) |
| `CORREO_*`, `WHATSAPP_*` | Los reales |
| `STRAPI_URL` | La URL pública del App Service de Strapi |
| `CORS_ORIGENES` | La URL del frontend |

Variables del App Service de Strapi:

| Variable | Valor |
|---|---|
| `WEBSITES_PORT` | `1337` |
| `PROXY` | `true` (Azure termina el HTTPS) |
| `APP_KEYS`, `API_TOKEN_SALT`, `ADMIN_JWT_SECRET`, `TRANSFER_TOKEN_SALT`, `ENCRYPTION_KEY`, `JWT_SECRET` | Valores secretos nuevos, distintos a los de local |
| `DATABASE_HOST`, `DATABASE_NAME`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Los del servidor PostgreSQL |
| `DATABASE_SSL` | `true` |

Pasos:

1. Crear los recursos en el portal de Azure.
2. Cargar las variables de arriba en el App Service.
3. Descargar el perfil de publicación del App Service y guardarlo como secreto del repositorio.
4. Al hacer push a `main`, el pipeline compila, prueba, analiza y despliega.
5. Verificar con `GET /salud` en la URL pública.

---

## 13. Evidencias

Las imágenes están en la carpeta `evidencias/`.

| Evidencia | Qué muestra | Archivo |
|---|---|---|
| Pruebas | Salida de `mvn verify` con las pruebas en verde | `pruebas.png` |
| Flujo completo | Publicar un Kudo y ver la auditoría con los tres canales | `flujo-publicar-kudo.png` |
| CI | Ejecución del pipeline de integración en GitHub Actions, en verde | `ci.png` |
| CD | Ejecución del pipeline de despliegue en verde | `cd.png` |
| SonarCloud | Panel del proyecto con la puerta de calidad aprobada | `sonar.png` |
| Trivy | Reporte de dependencias e imagen sin vulnerabilidades críticas | `trivy.png` |
| Despliegue | La URL pública respondiendo en `/salud` | `despliegue.png` |
| WhatsApp y correo | El mensaje recibido en el celular y el correo en la bandeja | `notificaciones.png` |
| Push | La notificación llegando en tiempo real | `push.png` |

---

## 14. Uso de inteligencia artificial

Usé IA como ayuda, y me aseguré de entender y poder explicar todo lo que está en el proyecto.

| Tarea | Qué hizo la IA | Qué hice yo |
|---|---|---|
| Documentación | Ayudó a ordenar y redactar con la estructura que ya usamos en otros proyectos | Escogí el alcance y las decisiones, y revisé que el texto dijera solo lo que el proyecto realmente hace |
| Código del backend | Generó el esqueleto inicial con arquitectura limpia, en Java con Spring Boot | Escogí el stack, reviso el código, lo compilo, lo corro y lo pruebo en mi equipo y en el CI |
| Decisiones de arquitectura | Propuso alternativas con sus ventajas | Elegí y justifiqué cada decisión (sección 9) |

Lo que puedo explicar sin ayuda:

- El flujo de publicar un Kudo desde el controlador hasta la auditoría.
- Por qué cada capa depende solo de la de adentro.
- Cómo agregar un canal de notificación nuevo creando una clase que cumpla el contrato `Notificador`.
- Qué falta y por qué: está en la sección de pendientes.

---

## 15. Pendientes

- Login con JWT
- Límite de peticiones por usuario y CORS restringido al dominio del frontend
- Endpoint de métricas
- Almacenamiento de objetos para los adjuntos
- Migraciones de base de datos en lugar de crear tablas automáticamente
- Pruebas de extremo a extremo automatizadas
- Cola con confirmación de entrega (broker) si crece la exigencia
