# TDD MVP v0.2 - Project GM

## 1. Propósito

Este documento transforma el GDD de Project GM y el alcance de [MVP v0.2](MVP%20v0.2.md) en una especificación técnica implementable con Java y Spring Boot.

El objetivo es construir un vertical slice jugable de 1–2 horas que demuestre:

- acciones expresadas libremente;
- interpretación de intención mediante la API de OpenAI;
- validación autoritativa por el Game Engine;
- resolución mediante reglas;
- consecuencias persistentes;
- NPCs, relaciones, misiones y combate básicos;
- recuperación de una partida guardada;
- trazabilidad completa de los eventos.

La arquitectura será un monolito modular. No se dividirá en microservicios durante el MVP.

## 2. Decisiones técnicas cerradas

| Área | Decisión |
|---|---|
| Lenguaje | Java 21 LTS |
| Framework | Spring Boot 4.1.1 |
| Build | Maven |
| API | REST/JSON sobre HTTPS |
| Persistencia | MySQL 8+ |
| Acceso a datos | Spring Data JPA/Hibernate |
| Migraciones | Flyway 13.7.0 |
| Mapeo DTO/comando | MapStruct 1.6.3 |
| IA | API de OpenAI con tool calling y respuestas estructuradas |
| Cliente IA | Adaptador HTTP basado en Spring `RestClient` |
| Identidad | JWT validado por Spring Security; proveedor de identidad configurable |
| IDs | UUID |
| Fechas | Instant en UTC |
| Arquitectura | Monolito modular con límites de paquete |
| Eventos | Event log append-only en MySQL |
| Concurrencia | Optimistic locking con version de agregado |
| Transporte de narración | Respuesta REST síncrona en MVP |
| Ejecución local | Maven y MySQL instalado o gestionado externamente |
| Observabilidad | Actuator, logs estructurados y métricas Micrometer |
| Idioma inicial | Español para contenido y narración |

Estas decisiones pueden cambiar después de validar el MVP, pero no deben bloquear su implementación.

## 3. Alcance funcional

### 3.1 Incluido

- Un mundo y un escenario cerrado.
- Una región y tres localizaciones.
- Cinco NPCs.
- Una facción.
- Tres tipos de enemigo.
- Inventario básico.
- Tres misiones relacionadas con ramas.
- Personaje de jugador persistente.
- Conversación y acciones libres.
- Exploración e investigación.
- Combate por turnos con dos acciones por turno.
- Relaciones básicas con NPCs.
- Memoria básica de hechos conocidos.
- Avance de tiempo entre acciones relevantes.
- Eventos auditables.
- Guardado y recuperación de partida.

### 3.2 Fuera de alcance

- Economía dinámica.
- Magia compleja.
- Más de una facción funcional.
- Simulación global del mundo.
- Mapa visual completo.
- Multijugador.
- Crafting.
- Voz.
- Generación de imágenes.
- Mundo generado infinitamente.
- Editor de mundos.
- Marketplace.

## 4. Objetivos de calidad

| Objetivo | Criterio MVP |
|---|---|
| Autoridad del motor | Ninguna modificación de estado procede directamente de texto generado por IA |
| Persistencia | Una partida recuperada conserva estado y eventos tras reiniciar la aplicación |
| Trazabilidad | Cada mutación relevante produce un evento con actor, causa y timestamp |
| Determinismo | Con la misma semilla y estado, las reglas producen el mismo resultado |
| Seguridad | El cliente no puede modificar IDs, resultados, tiradas o estado sin validación |
| Resiliencia | Un fallo de OpenAI no corrompe el estado de la partida |
| Rendimiento | p95 inferior a 5 s excluyendo la latencia externa de OpenAI; p95 inferior a 15 s incluyendo OpenAI |
| Coste | El contexto enviado a OpenAI se limita a la escena y memoria necesaria |

## 5. Arquitectura

```text
Cliente web
    |
    v
REST API / Spring Security
    |
    v
Application layer
    +-- GameSessionService
    +-- ActionOrchestrator
    +-- NarrativeService
    +-- CombatService
    +-- QuestService
    +-- SaveGameService
    |
    +--------------------+
    |                    |
    v                    v
  Domain / Game Engine     OpenAI Adapter
    |                    |
    +----------+---------+
               v
        MySQL + Flyway
        - world state
        - event log
        - conversation turns
```

### 5.1 Regla de autoridad

OpenAI solo puede:

- interpretar el texto del jugador;
- solicitar una herramienta declarada;
- pedir datos de escena autorizados;
- generar narración basada en resultados reales.

OpenAI no puede:

- modificar entidades directamente;
- inventar objetos o recompensas;
- decidir una tirada o su resultado;
- cambiar una dificultad fuera de una configuración validada;
- leer memoria no autorizada;
- declarar que una acción se ha ejecutado.

El Game Engine es la única autoridad para estado, reglas, dados, consecuencias y eventos.

### 5.2 Estructura de paquetes

```text
com.projectgm
├── ProjectGmApplication
├── api
│   ├── game
│   ├── session
│   └── error
├── application
│   ├── game
│   ├── action
│   ├── combat
│   ├── quest
│   └── save
├── domain
│   ├── action
│   ├── character
│   ├── combat
│   ├── event
│   ├── item
│   ├── location
│   ├── memory
│   ├── npc
│   ├── quest
│   ├── relationship
│   ├── rules
│   └── world
└── infrastructure
    ├── openai
    ├── persistence
    ├── security
    └── time
```

Los controladores no accederán directamente a repositorios. La dependencia permitida es `api -> application -> domain`, con `infrastructure` implementando puertos del dominio o de aplicación.

## 6. Flujo de una acción

```text
1. El cliente envía texto y sessionId.
2. La API autentica al jugador y valida el tamaño del texto.
3. Se carga la sesión y su versión.
4. Se obtiene una escena filtrada para el jugador.
5. OpenAI interpreta el texto o solicita una herramienta.
6. El sistema valida el comando estructurado.
7. El Game Engine comprueba ubicación, estado, permisos y requisitos.
8. El motor resuelve la acción o la rechaza sin mutar estado.
9. Se actualizan agregados y se escriben eventos en la misma transacción.
10. Se incrementa la versión de la sesión.
11. Se construye un resumen autorizado del nuevo estado.
12. OpenAI redacta la narración usando solo el resultado real.
13. La API devuelve resultado, narración y estado visible.
```

Si falla OpenAI después de persistir una acción, el resultado queda guardado y se devuelve una narración de recuperación en la siguiente consulta. Nunca se ejecuta dos veces una acción por reintentar la narración.

## 7. Modelo de dominio

### 7.1 Agregados

| Agregado | Responsabilidad |
|---|---|
| GameSession | Partida, jugador, mundo, localización actual y versión |
| Character | Atributos, habilidades, salud, nivel y estados |
| NPC | Identidad, estado, ubicación, objetivos y conocimiento |
| Item | Objeto existente, propietario, ubicación y propiedades |
| Combat | Orden de iniciativa, turno, acciones y participantes |
| Quest | Estado, objetivos, ramas y consecuencias |
| Relationship | Relación entre jugador y NPC |
| World | Tiempo, localizaciones y configuración del escenario |

### 7.2 Entidades y valores

```text
GameSession
- id: UUID
- playerId: UUID
- worldId: UUID
- currentLocationId: UUID
- status: ACTIVE | COMPLETED | PLAYER_DEAD
- worldTime: Instant
- version: long

Character
- id: UUID
- sessionId: UUID
- name: String
- level: int
- experience: int
- attributes: AttributeSet
- skills: SkillSet
- health: HealthState
- conditions: Set<Condition>

NPC
- id: UUID
- sessionId: UUID
- name: String
- locationId: UUID
- factionId: UUID?
- status: NPCStatus
- knowledge: Set<KnowledgeFact>

Item
- id: UUID
- sessionId: UUID
- templateId: String
- ownerId: UUID?
- locationId: UUID?
- quantity: int
- durability: int?
```

Las colecciones pequeñas de atributos, habilidades y estados pueden almacenarse como JSON dentro de sus agregados en el MVP. Las relaciones, misiones, eventos e inventario tendrán tablas propias para poder consultarse y auditarse.

## 8. Reglas de juego

### 8.1 Atributos

Los seis atributos son Fuerza, Agilidad, Intelecto, Voluntad, Percepción y Presencia. Su rango es 1–20.

```text
modifier = floor((attribute - 10) / 2)
```

Esto produce la escala definida en el GDD: 1–3 = -4, 10–11 = 0 y 20 = +5.

### 8.2 Habilidades

Cada habilidad tiene un rango de 0–5. En el MVP no se permiten valores negativos.

```text
checkTotal = d20 + attributeModifier + skillBonus + circumstanceModifier
```

Los modificadores circunstanciales estarán limitados a -3..+3 y serán definidos por contenido del escenario o por el motor, nunca por texto libre de OpenAI.

### 8.3 Tiradas

- El backend genera una semilla de acción con `SecureRandom` y la persiste.
- La resolución usa un generador determinista inicializado con esa semilla.
- Se persisten semilla, resultado, fórmula y dificultad para poder auditar y reproducir la tirada.
- La dificultad solo puede ser 8, 11, 14, 17, 20, 23 o 26.
- Las acciones triviales se resuelven automáticamente.
- Las acciones imposibles se rechazan automáticamente.
- El motor decide si una acción requiere tirada.

### 8.4 Grados de resultado

Se calcula `margin = checkTotal - difficulty`:

| Margen | Resultado |
|---:|---|
| 6 o más | GRAN_EXITO |
| 0 a 5 | EXITO |
| -1 a -3 | EXITO_CON_COSTE |
| -4 a -7 | FRACASO |
| -8 o menos | FRACASO_GRAVE |

El contenido de los costes y consecuencias se define en la misión, escena o regla de acción. El modelo no los inventa.

### 8.5 Combate

- La iniciativa es `d20 + Agility modifier`.
- Cada combatiente comienza con 2 acciones por turno.
- Una acción de ataque valida alcance, arma, objetivo y estado.
- El daño procede de la definición del arma y del resultado de la acción.
- A 0 de salud el personaje queda `DOWNED`.
- `DOWNED` permite acciones limitadas.
- El combate termina cuando no quedan oponentes activos o se cumple una condición de retirada.

## 9. Contratos de API

### 9.1 Crear partida

`POST /api/v1/sessions`

Respuesta `201 Created`:

```json
{
  "sessionId": "uuid",
  "character": { "id": "uuid", "name": "Aren", "level": 1 },
  "location": { "id": "village_square", "name": "Plaza de la Aldea" },
  "worldTime": "2026-01-01T08:00:00Z",
  "version": 0
}
```

### 9.2 Obtener estado visible

`GET /api/v1/sessions/{sessionId}`

Devuelve únicamente la escena, hechos conocidos por el jugador, estado del personaje, inventario visible, misiones visibles y relaciones conocidas.

### 9.3 Enviar acción

`POST /api/v1/sessions/{sessionId}/actions`

Request:

```json
{
  "text": "Intento convencer al guardia de que soy un comerciante.",
  "expectedVersion": 12,
  "idempotencyKey": "uuid"
}
```

Respuesta `200 OK`:

```json
{
  "actionId": "uuid",
  "status": "RESOLVED",
  "narration": "El guardia observa tu documentación...",
  "result": {
    "type": "SUCCESS_WITH_COST",
    "roll": { "d20": 12, "modifier": 3, "total": 15, "difficulty": 14 }
  },
  "events": ["ACTION_RESOLVED", "RELATIONSHIP_CHANGED"],
  "stateVersion": 13
}
```

### 9.4 Consultar eventos

`GET /api/v1/sessions/{sessionId}/events?after=sequence`

Solo para diagnóstico y administración durante el MVP. El cliente normal no recibe eventos internos sin filtrar.

### 9.5 Errores

```json
{
  "code": "STALE_SESSION_VERSION",
  "message": "La partida ha cambiado. Actualiza el estado antes de repetir la acción.",
  "correlationId": "uuid"
}
```

Códigos mínimos: `INVALID_REQUEST`, `SESSION_NOT_FOUND`, `FORBIDDEN_SESSION`, `STALE_SESSION_VERSION`, `ACTION_NOT_ALLOWED`, `OPENAI_UNAVAILABLE`, `INTERNAL_ERROR`.

## 10. Contrato interno con OpenAI

El adaptador usará la Responses API de OpenAI con function calling y respuestas estructuradas. OpenAI recibirá:

- instrucciones inmutables del Master;
- escena visible;
- hechos conocidos por el personaje;
- resumen de conversación reciente;
- catálogo de herramientas permitido;
- texto del jugador.

No recibirá el estado secreto completo del mundo.

### Herramientas MVP

```text
inspect_scene()
inspect_npc(npcId)
inspect_location(locationId)
propose_action(intent, targetId, skill, objective)
request_combat(targetIds)
request_dialogue(npcId, topic)
```

Las herramientas de mutación no se ofrecen directamente al modelo. El `ActionOrchestrator` convierte una propuesta válida en una llamada interna al dominio:

```text
resolveAction()
startCombat()
performAttack()
advanceTime()
updateRelationship()
progressQuest()
```

La respuesta estructurada de OpenAI debe cumplir JSON Schema. Un JSON inválido, una herramienta desconocida o un ID fuera de la escena se rechaza y no modifica el estado.

## 11. Persistencia

### Tablas principales

```text
game_session
character
npc
item
location
quest
quest_state
relationship
combat
combat_participant
conversation_turn
game_event
world_snapshot
```

### Reglas de persistencia

- Flyway crea y versiona el esquema.
- Todas las mutaciones de una acción se ejecutan en una transacción.
- `game_session.version` implementa optimistic locking.
- `game_event` es append-only.
- `idempotency_key` evita duplicar acciones reenviadas.
- Las conversaciones se almacenan separadas del estado autoritativo.
- Los secretos y credenciales nunca se guardan en la base de datos.

### Evento

```json
{
  "eventId": "uuid",
  "sessionId": "uuid",
  "sequence": 42,
  "type": "RELATIONSHIP_CHANGED",
  "actorId": "uuid",
  "payload": { "targetId": "uuid", "oldValue": 0, "newValue": 2 },
  "worldTime": "2026-01-01T08:15:00Z",
  "createdAt": "2026-01-01T08:15:02Z"
}
```

## 12. Seguridad

- Spring Security valida el JWT mediante issuer y audience configurados.
- El `playerId` se toma del token, nunca del body.
- Cada consulta verifica que la sesión pertenece al jugador.
- Los IDs de recursos se validan dentro de la sesión actual.
- Se limita el texto de acción a 2.000 caracteres.
- Se aplican límites por usuario para acciones y llamadas a OpenAI.
- Las instrucciones del sistema de OpenAI no se aceptan desde el cliente.
- Se registran correlation ID, usuario y sesión sin registrar secretos.
- En local se permite un perfil `dev` con identidad fija, deshabilitado en producción.

## 13. Integración del proveedor de IA y despliegue

### Cliente OpenAI

- OpenAI Responses API para inferencia.
- `RestClient` de Spring para comunicación HTTP.
- API key de OpenAI proporcionada mediante configuración segura del entorno.
- El `OpenAIAdapter` será el único componente que conocerá el protocolo externo.

### Servicios externos configurables

- MySQL administrado o instalación local.
- Proveedor de identidad compatible con JWT, configurable mediante issuer y audience.
- Gestor de secretos del entorno de despliegue o variables de entorno.
- Backend de logs y métricas compatible con la plataforma elegida.

La aplicación no dependerá de una cuenta, región o servicio propietario de un proveedor cloud concreto. El despliegue futuro podrá realizarse en cualquier plataforma que permita ejecutar Java 21, exponer la API por HTTPS, conectar con MySQL y acceder a la API de OpenAI.

### Configuración

```text
OPENAI_API_KEY
OPENAI_BASE_URL
OPENAI_MODEL
OPENAI_MAX_OUTPUT_TOKENS
OPENAI_TEMPERATURE
JWT_ISSUER_URI
JWT_AUDIENCE
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
```

La temperatura narrativa se configura separada de la interpretación estructurada. La interpretación debe usar una configuración conservadora y validación de esquema.

### Fallos

- Timeout de OpenAI: devolver estado persistido y error recuperable.
- Respuesta inválida: registrar diagnóstico, no mutar estado.
- Throttling: reintento con backoff limitado solo para llamadas idempotentes.
- Error permanente: `OPENAI_UNAVAILABLE` y posibilidad de consultar la partida.

## 14. Observabilidad

Cada petición tendrá `correlationId` y `sessionId`.

Métricas mínimas:

- duración de acciones;
- duración y errores de OpenAI;
- tokens de entrada y salida;
- acciones resueltas por tipo;
- errores de validación;
- conflictos de versión;
- partidas creadas y recuperadas;
- coste estimado por sesión.

Logs estructurados en JSON. Nunca se registra el prompt completo ni datos sensibles en producción sin una política explícita de retención.

## 15. Pruebas

### Unitarias

- modificador de atributos;
- cálculo de tiradas;
- grados de resultado;
- validación de acciones;
- inventario;
- combate;
- transiciones de misión;
- relaciones;
- filtrado de memoria;
- reglas de muerte.

### Integración

- migraciones Flyway;
- repositorios MySQL;
- transacción de una acción;
- optimistic locking;
- idempotencia;
- serialización de contratos;
- adaptador OpenAI con WireMock o stub local.

### End-to-end

1. Crear partida.
2. Obtener escena.
3. Enviar una acción libre.
4. Resolver una tirada.
5. Verificar evento y estado.
6. Reiniciar la aplicación.
7. Recuperar la partida.
8. Verificar que la consecuencia permanece.
9. Iniciar combate.
10. Completar una rama de misión.

### Propiedades invariantes

- Una acción rechazada no cambia estado ni crea evento de mutación.
- Ningún objeto aparece sin evento `ITEM_ACQUIRED`.
- Ningún NPC conoce un hecho sin una fuente válida.
- El resultado de una tirada no puede ser modificado por el cliente.
- Una misma `idempotencyKey` produce un único resultado.
- Una versión antigua de sesión nunca sobrescribe una versión nueva.

## 16. Ejecución y despliegue

La aplicación se ejecutará directamente mediante Maven. MySQL deberá estar instalado localmente o disponible como servicio externo. WireMock podrá ejecutarse como dependencia de pruebas o sustituirse por un stub en memoria.

Perfiles:

- `local`: MySQL local o externo, OpenAI stub y usuario de desarrollo.
- `test`: base de datos de pruebas dedicada o instancia efímera gestionada por la infraestructura de pruebas.
- `cloud`: OpenAI real, proveedor JWT elegido, gestor de secretos y backend de observabilidad elegidos.

Comandos previstos:

```text
./mvnw test
./mvnw verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 17. Orden de implementación

1. Crear proyecto Spring Boot, configuración y perfiles.
2. Añadir MySQL, Flyway y esquema inicial.
3. Implementar `GameSession` y `Character`.
4. Implementar estado visible y endpoint de consulta.
5. Implementar eventos e idempotencia.
6. Implementar reglas de tiradas y acciones básicas.
7. Implementar NPCs, relaciones y memoria filtrada.
8. Implementar misiones y consecuencias.
9. Implementar combate.
10. Añadir adaptador OpenAI con stub.
11. Integrar interpretación de texto y narración.
12. Añadir seguridad JWT.
13. Añadir observabilidad.
14. Ejecutar pruebas end-to-end del vertical slice.

## 18. Criterio de aceptación técnico

El MVP v0.2 estará técnicamente listo cuando:

- el flujo completo de acción funcione desde REST hasta persistencia y narración;
- el Game Engine pueda resolver una acción sin OpenAI;
- OpenAI solo pueda proponer operaciones permitidas;
- una partida pueda cerrarse y recuperarse sin pérdida de consecuencias;
- exista un combate jugable y una misión con dos ramas;
- todos los cambios relevantes tengan eventos auditables;
- las invariantes críticas estén cubiertas por pruebas;
- los fallos de OpenAI no corrompan partidas;
- la aplicación pueda ejecutarse localmente mediante Maven con una base de datos MySQL configurada.

## 19. Evolución posterior

Cuando el MVP v0.2 esté validado, se podrán añadir economía, magia, múltiples facciones, world ticks globales, mapa visual y más contenido. La evolución debe conservar:

- la autoridad del Game Engine;
- los comandos estructurados;
- los eventos append-only;
- la separación entre memoria visible y estado secreto;
- los límites de cada agregado.

No se recomienda extraer microservicios antes de que existan problemas reales de escala o de ownership.
