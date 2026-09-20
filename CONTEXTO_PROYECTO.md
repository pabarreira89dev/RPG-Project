# Contexto del proyecto Project GM

## Propósito

Project GM es un RPG narrativo conversacional de fantasía para web. La IA actúa como Master, pero no es la autoridad del mundo.

Principio arquitectónico fundamental:

> La IA interpreta y narra. El Game Engine determina qué es verdad.

El proyecto empieza como experiencia individual y debe dejar margen para una futura evolución multijugador.

## Documentos de diseño

- `GDD v0.1.md`: visión, pilares de diseño, reglas, mundo, NPCs, combate, memoria, eventos y arquitectura conceptual.
- `MVP v0.1.md`: alcance completo previsto: campaña de 5–10 horas, 3 regiones, 10 localizaciones, 20 NPCs, 5 facciones, economía, relaciones, memoria, tiempo y World Tick.
- `MVP v0.2.md`: vertical slice actual de 1–2 horas: 1 escenario, 3 localizaciones, 5 NPCs, 1 facción, 3 tipos de enemigo, 3 misiones ramificadas, combate básico, inventario y persistencia.
- `TDD MVP v0.2.md`: especificación técnica para implementar el vertical slice con Java y Spring Boot.

## Alcance actual

El desarrollo debe centrarse en `MVP v0.2`, no en el alcance completo de `MVP v0.1`.

El vertical slice debe demostrar:

- crear y recuperar una partida;
- personaje persistente;
- acciones libres del jugador;
- interpretación de intención mediante IA;
- validación por el Game Engine;
- resolución de reglas y dados;
- NPCs, relaciones y memoria básica;
- combate por turnos;
- misión con ramas;
- consecuencias persistentes;
- eventos auditables.

Fuera del MVP v0.2 quedan economía dinámica, magia compleja, múltiples facciones, simulación global, mapa visual completo, multijugador, crafting, voz, imágenes generadas, editor y marketplace.

## Decisiones técnicas

- Java 21 LTS.
- Spring Boot 4.1.1.
- Maven.
- Monolito modular, no microservicios durante el MVP.
- REST/JSON sobre HTTPS.
- MySQL.
- Spring Data JPA/Hibernate.
- Flyway 13.7.0 para migraciones.
- MapStruct 1.6.3 para mapeo entre DTOs y comandos/entidades.
- IDs UUID.
- Fechas con `Instant` en UTC.
- Eventos append-only en MySQL.
- Optimistic locking mediante versión del agregado.
- Ejecución local con Maven y MySQL instalado o externo.
- No usar Docker ni Docker Compose por ahora.
- El proveedor de despliegue cloud queda sin decidir.

## IA

Se sustituyó AWS Bedrock por la API de OpenAI.

El diseño usa:

- OpenAI Responses API.
- Function calling y respuestas estructuradas.
- Adaptador propio `OpenAIAdapter` mediante `RestClient` de Spring.
- OpenAI aislado detrás de una interfaz para que el dominio no dependa del proveedor.
- OpenAI puede interpretar texto, solicitar herramientas autorizadas y generar narración.
- OpenAI no puede modificar entidades, inventar objetos, decidir tiradas, alterar dificultades ni declarar ejecuciones.
- El Game Engine sigue siendo la única autoridad para estado, reglas, dados, consecuencias y eventos.

Variables esperadas para el perfil cloud:

```text
OPENAI_API_KEY
OPENAI_BASE_URL
OPENAI_MODEL
OPENAI_MAX_OUTPUT_TOKENS
OPENAI_TEMPERATURE
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
```

## Perfiles Spring Boot

En `Project GM/src/main/resources` existen:

- `application.yml`: configuración común y perfil por defecto `local`.
- `application-local.yml`: MySQL local/externo (host/BD fijos vía `DATABASE_URL`, usuario/contraseña vía `DATABASE_USERNAME`/`DATABASE_PASSWORD`, todas con default `admin`/`admin`/`jdbc:mysql://localhost:3306/project_gm` para desarrollo), logs detallados, OpenAI real activado por defecto (requiere `OPENAI_API_KEY`/`OPENAI_MODEL` como variables de entorno, sin default; se puede desactivar con `OPENAI_ENABLED=false` para volver al stub) y usuario de desarrollo. Los valores locales actuales de estas variables se guardan en `Project GM/.env.local` (gitignored, ver sección "Estado de la sesión").
- `application-test.yml`: base de datos de pruebas, OpenAI desactivado (stub, para que los tests sean deterministas y no dependan de red/credenciales).
- `application-cloud.yml`: MySQL y OpenAI configurados mediante variables de entorno, seguridad de desarrollo desactivada.

El perfil anterior `prod` fue sustituido por `cloud`.

## Estado de implementación

El proyecto Spring Boot está en:

`Project GM/`

Configuración inicial existente:

- `pom.xml` con Spring Boot 4.1.1, Java 21, Web, JPA, MySQL Connector/J, Flyway 13.7.0, MapStruct 1.6.3, Lombok y tests.
- Clase principal `pab.rpg.Application`.
- Test inicial de contexto.
- Configuración YAML por perfiles.

El proyecto compila correctamente. La compilación y las pruebas las ejecuta manualmente el usuario.

### Punto 1 implementado: GameSession y Character

Paquetes actuales:

`Project GM/src/main/java/pab/rpg/domain/entity/`

`Project GM/src/main/java/pab/rpg/domain/repository/`

Clases:

- `GameSession`: sesión, jugador, mundo, localización, estado, tiempo, versión y personaje.
- `Character`: nombre, nivel, experiencia, atributos y salud.
- `AttributeSet`: seis atributos: fuerza, agilidad, intelecto, voluntad, percepción y presencia.
- `HealthState`: salud máxima, salud actual y heridas.
- `SessionStatus`: `ACTIVE`, `COMPLETED`, `PLAYER_DEAD`.

Los repositorios están separados de las entidades en `domain.repository`:

- `CharacterRepository`.
- `GameSessionRepository`.

Las entidades usan JPA y Lombok:

- `@Entity` y `@Embeddable`.
- `@Getter`.
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` para JPA.
- `@AllArgsConstructor` en las entidades y objetos de valor según el estado actual.
- No se han añadido setters públicos.

La migración inicial ya está creada en:

`Project GM/src/main/resources/db/migration/V1__create_game_session_and_character.sql`

Incluye las tablas `player_character` y `game_session`, la relación uno a uno mediante `character_id`, la columna `version` para optimistic locking, la restricción de estados y los índices básicos por jugador y mundo.

La capa de servicio de partidas está organizada así:

- `pab.rpg.service.GameSessionService`: interfaz pública del servicio.
- `pab.rpg.service.impl.GameSessionServiceImpl`: implementación Spring del servicio.
- `pab.rpg.service.CreateGameSessionCommand`: comando de creación.
- `pab.rpg.exception.SessionNotFoundException`: error de consulta no encontrada.

Los endpoints REST de sesiones están organizados así:

- `pab.rpg.api.controller.GameSessionController`.
- `pab.rpg.api.dto.request.CreateSessionRequest`.
- `pab.rpg.api.dto.response.GameSessionResponse`.
- `pab.rpg.api.mapper.CreateSessionRequestMapper`: interfaz MapStruct (`@Mapper(componentModel = "spring")`) que convierte `CreateSessionRequest` en `CreateGameSessionCommand`.
- `pab.rpg.exception.GlobalExceptionHandler`: traduce `SessionNotFoundException` a 404 y `IllegalArgumentException`/`NullPointerException` a 400, devolviendo un `ApiError` con `code`, `message` y `timestamp`.

Endpoints disponibles:

- `POST /api/v1/sessions` para crear una sesión.
- `GET /api/v1/sessions/{sessionId}?playerId={playerId}` para consultar una sesión.
- `GET /api/v1/sessions?playerId={playerId}` para listar las sesiones de un jugador.

Mientras no exista autenticación JWT, `playerId` se recibe provisionalmente en el body o query string.

Existe un test unitario `CreateSessionRequestMapperTest` que verifica el mapeo de `CreateSessionRequestMapper`.

### Punto 2 implementado: eventos e idempotencia (infraestructura base)

La migración `Project GM/src/main/resources/db/migration/V2__create_game_event_and_processed_action.sql` añade:

- `game_event`: tabla append-only con `session_id`, `sequence` (único por sesión), `type`, `actor_id`, `payload` JSON, `world_time` y `created_at`.
- `processed_action`: tabla de idempotencia con `session_id`, `idempotency_key` (único por sesión), `action_id` y `response_payload` JSON.

Entidades nuevas en `domain.entity`: `GameEvent` y `ProcessedAction`, con el mismo estilo Lombok/JPA que `GameSession` y `Character`. El campo JSON se mapea como `Map<String, Object>` con `@JdbcTypeCode(SqlTypes.JSON)` para evitar problemas de doble serialización.

Repositorios nuevos en `domain.repository`: `GameEventRepository` y `ProcessedActionRepository`.

Servicios nuevos:

- `pab.rpg.service.GameEventService` / `impl.GameEventServiceImpl`: calcula el siguiente `sequence` por sesión y añade eventos.
- `pab.rpg.service.IdempotencyService` / `impl.IdempotencyServiceImpl`: busca y registra resultados de acciones por `(sessionId, idempotencyKey)`.

Endpoint de diagnóstico añadido:

- `GET /api/v1/sessions/{sessionId}/events?playerId={playerId}&after={sequence}` en `pab.rpg.api.controller.GameEventController`, que reutiliza `GameSessionService.getSession` para validar que la sesión pertenece al jugador.

Tests unitarios con Mockito: `GameEventServiceImplTest` e `IdempotencyServiceImplTest`, cubriendo el cálculo de `sequence` y el registro/búsqueda de resultados idempotentes.

Todavía no existe el endpoint `POST /api/v1/sessions/{sessionId}/actions`, así que `GameEventService` e `IdempotencyService` no tienen todavía ningún llamador real fuera de los tests.

### Punto 3 implementado: reglas y primera acción sin OpenAI

Nuevo paquete `pab.rpg.domain.rules` con la lógica de reglas del GDD/TDD (independiente de Spring salvo `CheckResolver`, que es un `@Component` para poder inyectarse):

- `Attribute`: los seis atributos.
- `Difficulty`: los siete niveles de dificultad permitidos (8, 11, 14, 17, 20, 23, 26).
- `ResultGrade`: los cinco grados de resultado y `fromMargin(margin)` con los umbrales del TDD.
- `CheckResolution`: resultado inmutable de una tirada (semilla, d20, modificadores, total, dificultad, margen, grado).
- `CheckResolver`: genera la semilla con `SecureRandom`, tira el d20 con un `Random` determinista inicializado con esa semilla y calcula el resultado.

Como todavía no existe interpretación de texto vía OpenAI, `ActionServiceImpl` resuelve toda acción como una comprobación genérica de **Intelecto** con dificultad **MODERATE (14)** y sin bonificadores de habilidad ni circunstanciales; esto está marcado explícitamente como provisional en el código, pendiente de que la interpretación real decida atributo/dificultad/bonificadores.

Endpoint nuevo:

- `POST /api/v1/sessions/{sessionId}/actions?playerId={playerId}` en `pab.rpg.api.controller.ActionController`, con body `SubmitActionRequest` (`text`, `expectedVersion`, `idempotencyKey`) y respuesta `ActionResponse` (`actionId`, `status`, `narration`, `result.type`, `result.roll` (`d20`, `modifier`, `total`, `difficulty`), `events`, `stateVersion`), igual que el contrato 9.3 del TDD.

`pab.rpg.service.ActionService` / `impl.ActionServiceImpl` implementa el flujo:

1. Valida el comando (`text` no vacío, longitud máxima 2000).
2. Carga la sesión con `GameSessionService.getSession` (valida que pertenece al jugador).
3. Comprueba idempotencia con `IdempotencyService.findExisting`; si la acción ya se procesó, devuelve la respuesta guardada sin repetir nada.
4. Compara `expectedVersion` con la versión real de la sesión; si no coincide, lanza `StaleSessionVersionException`.
5. Resuelve la tirada con `CheckResolver`.
6. Avanza `worldTime` de la sesión (`GameSession.advanceWorldTime(Duration)`, método añadido a la entidad) y persiste con `gameSessionRepository.saveAndFlush(...)`, lo que fuerza el incremento de `version` por optimistic locking antes de construir la respuesta.
7. Registra un evento `ACTION_RESOLVED` vía `GameEventService.append` con el detalle completo de la tirada (semilla incluida, para auditoría/reproducibilidad).
8. Guarda el resultado con `IdempotencyService.record` para futuras repeticiones.

Errores nuevos:

- `pab.rpg.exception.StaleSessionVersionException` → `GlobalExceptionHandler` la traduce a 409 con código `STALE_SESSION_VERSION`.
- `org.springframework.orm.ObjectOptimisticLockingFailureException` (conflicto real de concurrencia de Hibernate) también se traduce a 409 `STALE_SESSION_VERSION`.

Tests añadidos: `ResultGradeTest`, `CheckResolverTest` (lógica de reglas) y `ActionServiceImplTest` con Mockito (versión obsoleta, repetición idempotente y flujo exitoso).

El proyecto compila y todos los tests pasan, incluyendo los nuevos.

### Punto 4 implementado: localizaciones y motor mínimo de reglas

Migración `Project GM/src/main/resources/db/migration/V3__create_location.sql`: crea la tabla `location` (`id`, `code` único, `name`, `description`), inserta las 3 localizaciones del vertical slice (`village_square`/Plaza de la Aldea, `tavern`/Taberna, `forest_edge`/Lindero del Bosque) con UUIDs fijos, y añade una foreign key desde `game_session.current_location_id` hacia `location.id`.

Entidad nueva `pab.rpg.domain.entity.Location` (mismo estilo Lombok/JPA que el resto) y repositorio `pab.rpg.domain.repository.LocationRepository` con `findByCode`.

Nuevo paquete de reglas de acción en `pab.rpg.domain.rules`:

- `ActionType`: enum mínimo (`EXPLORATION`, `SOCIAL`, `INVESTIGATION`, `PHYSICAL`) que asocia cada categoría de acción (ver GDD sección 5) con su `Attribute` y `Difficulty` base. Sustituye el Intelecto/MODERATE fijos de `ActionServiceImpl`.
- `ActionContext`: record con la sesión y el `ActionType` de la acción en curso.
- `GameRule`: interfaz funcional (`check(ActionContext)`) que lanza `ActionNotAllowedException` si la acción no está permitida.
- `ActorAliveRule`: rechaza la acción si la salud actual del personaje es 0.
- `LocationExistsRule`: rechaza la acción si `currentLocationId` es nulo o no existe en `LocationRepository`.

`ActionServiceImpl` ahora recibe `List<GameRule>` (Spring inyecta automáticamente todos los beans que implementan la interfaz) y las ejecuta todas contra un `ActionContext` antes de resolver la tirada; el atributo y la dificultad de la tirada se derivan de `command.actionType()` en lugar de constantes fijas. `SubmitActionRequest`/`SubmitActionCommand` añaden el campo `actionType`, marcado explícitamente como provisional hasta que exista interpretación real de texto.

Error nuevo `pab.rpg.exception.ActionNotAllowedException` → `GlobalExceptionHandler` la traduce a `422 Unprocessable Entity` con código `ACTION_NOT_ALLOWED`.

Tests añadidos: `ActorAliveRuleTest`, `LocationExistsRuleTest` (con Mockito para el repositorio). `ActionServiceImplTest` se actualizó para el nuevo parámetro `gameRules` (lista vacía en los tests unitarios) y el campo `actionType` en los comandos.

### Punto 4 implementado: NPCs, relaciones y memoria básica (infraestructura)

Migración `Project GM/src/main/resources/db/migration/V4__create_npc_relationship_and_knowledge.sql`: crea `npc` (catálogo compartido, igual que `Location`, con `code` único, `location_id` FK, `faction` opcional y `status` ALIVE/DEAD) sembrando 5 NPCs del vertical slice (`aron_tavernkeeper`, `village_guard`, `village_elder`, `forest_hunter`, `traveling_merchant`) repartidos entre las 3 localizaciones existentes; `relationship` (`session_id` + `npc_id` único, `value` entero, `updated_at`) para el estado de relación por partida; y `npc_knowledge_fact` (`session_id` + `npc_id` + `fact_key` único, `learned_at`) como base para el conocimiento asimétrico del GDD (secciones 18-19).

Decisión de diseño: a diferencia del `NPC.sessionId` literal del TDD, los NPCs se modelan como catálogo global (como `Location`), y solo `Relationship`/`NpcKnowledgeFact` son por sesión. Esto evita duplicar identidad de NPCs por partida y mantiene la coherencia con el patrón ya usado para `Location`.

Entidades nuevas en `domain.entity`: `Npc`, `NpcStatus` (enum `ALIVE`/`DEAD`), `Relationship` (con `changeBy(delta, now)`), `NpcKnowledgeFact`. Repositorios nuevos en `domain.repository`: `NpcRepository` (`findByCode`, `findAllByLocationId`), `RelationshipRepository` (`findBySessionIdAndNpcId`, `findAllBySessionId`), `NpcKnowledgeFactRepository` (`existsBySessionIdAndNpcIdAndFactKey`, `findAllBySessionIdAndNpcId`).

Servicio nuevo `pab.rpg.service.NpcService` / `impl.NpcServiceImpl`: `getNpcsAtLocation(locationId)`, `getRelationshipValue(sessionId, npcId)` (devuelve 0 si no existe fila de relación todavía), `changeRelationship(sessionId, npcId, delta)` (crea la fila si no existe, con valor inicial 0, y aplica `Relationship.changeBy`), `recordKnowledge(sessionId, npcId, factKey)` (inserción idempotente, devuelve si el hecho era nuevo) y `knowsFact(sessionId, npcId, factKey)`.

Endpoint de solo lectura nuevo: `GET /api/v1/sessions/{sessionId}/npcs?playerId={playerId}` en `pab.rpg.api.controller.NpcController`, que valida la sesión con `GameSessionService.getSession` y devuelve los NPCs de la localización actual con su relación (`NpcResponse`), igual al "relaciones conocidas" del contrato 9.2 del TDD.

`SubmitActionRequest`/`SubmitActionCommand` añaden el campo opcional `targetNpcId` para acciones que apuntan a un NPC concreto. Nueva regla `pab.rpg.domain.rules.NpcTargetRule` (añadida a `ActionContext`, que ahora también lleva `targetNpcId`): si se indica un NPC, debe existir y estar en la localización actual de la sesión, si no lanza `ActionNotAllowedException`.

`ActionServiceImpl` ahora inyecta `NpcService`: cuando una acción resuelta es de tipo `SOCIAL` y trae `targetNpcId`, calcula un delta de relación a partir del `ResultGrade` (`GRAN_EXITO` +2, `EXITO` +1, `EXITO_CON_COSTE` 0, `FRACASO` -1, `FRACASO_GRAVE` -2), llama a `NpcService.changeRelationship`, registra un segundo evento `RELATIONSHIP_CHANGED` (con `npcId`, `delta`, `newValue`) vía `GameEventService.append` y lo añade a la lista `events` de la respuesta.

`NpcKnowledgeFact` todavía no tiene ningún llamador real (ni lectura ni escritura desde acciones): el método `recordKnowledge`/`knowsFact` existe en `NpcService` pero queda sin conectar hasta que existan diálogo/investigación/misiones con hechos concretos que registrar.

Test añadido/actualizado: `NpcServiceImplTest` (NPCs por localización, relación por defecto y con valor existente, `changeRelationship` crea/actualiza fila, `recordKnowledge` idempotente), `NpcTargetRuleTest` (sin NPC objetivo, NPC en la localización correcta, NPC inexistente, NPC en otra localización), `ActionServiceImplTest` (nuevo test de acción `SOCIAL` con `targetNpcId` que verifica el cambio de relación y el evento `RELATIONSHIP_CHANGED`), y `ActorAliveRuleTest`/`LocationExistsRuleTest` actualizados al nuevo constructor de `ActionContext`.

### Punto 5 implementado: misiones como máquina de estados

Migración `Project GM/src/main/resources/db/migration/V5__create_quest_and_quest_state.sql`: crea `quest` (catálogo, como `Location`/`Npc`: `code` único, `title`, `description`), `quest_stage` (etapas de cada misión; `is_initial`/`is_terminal`, única por `quest_id`+`code`), `quest_stage_transition` (aristas del grafo: `from_stage_id` + `choice_key` único por misión, apunta a `to_stage_id`) y `quest_state` (progreso por sesión: `session_id`+`quest_id` único, `current_stage_id`, `status` ACTIVE/COMPLETED). Siembra 3 misiones ramificadas ligadas a NPCs ya existentes: `aron_debt` (pagar la deuda o enfrentar al prestamista), `forest_threat` (investigar o ignorar la amenaza del bosque, ligada a `forest_hunter`) y `village_elder_history` (escuchar o descartar la historia de la anciana). Cada una tiene una etapa inicial y dos etapas terminales alcanzables mediante `choiceKey` distintos (`pay`/`confront`, `investigate`/`ignore`, `listen`/`dismiss`).

Entidades nuevas en `domain.entity`: `Quest`, `QuestStage`, `QuestStageTransition`, `QuestState`, `QuestStatus` (enum `ACTIVE`/`COMPLETED`), con `QuestState.advanceTo(stageId, terminal, now)` para aplicar una transición. Repositorios nuevos en `domain.repository`: `QuestRepository` (`findByCode`), `QuestStageRepository` (`findByQuestIdAndCode`, `findByQuestIdAndInitialTrue`), `QuestStageTransitionRepository` (`findByQuestIdAndFromStageIdAndChoiceKey`), `QuestStateRepository` (`findBySessionIdAndQuestId`, `findAllBySessionId`).

Servicio nuevo `pab.rpg.service.QuestService` (con el record anidado `QuestStateView` para no filtrar entidades JPA fuera de la capa de servicio, siguiendo la restricción de `api -> application/service -> domain` del TDD) / `impl.QuestServiceImpl`:

- `startQuest(sessionId, questCode)`: idempotente — si la sesión ya tiene progreso en esa misión, devuelve el estado existente; si no, crea `QuestState` en la etapa marcada `is_initial`.
- `advanceQuest(sessionId, questCode, choiceKey)`: busca una `QuestStageTransition` que salga de la etapa actual con ese `choiceKey`; si no existe, o la misión no ha empezado, o ya está `COMPLETED`, lanza `QuestTransitionNotAllowedException`. Si la transición es válida, mueve `QuestState` a la etapa destino y la marca `COMPLETED` si esa etapa es terminal.
- `getQuestState(sessionId, questCode)` / `getVisibleQuests(sessionId)`: lectura del progreso.

Errores nuevos: `pab.rpg.exception.QuestNotFoundException` → 404 `QUEST_NOT_FOUND`; `pab.rpg.exception.QuestTransitionNotAllowedException` → 422 `QUEST_TRANSITION_NOT_ALLOWED` en `GlobalExceptionHandler`.

Endpoints nuevos en `pab.rpg.api.controller.QuestController` (mismo patrón que `NpcController`: valida la sesión con `GameSessionService.getSession` antes de delegar):

- `GET /api/v1/sessions/{sessionId}/quests?playerId={playerId}`: misiones visibles de la sesión.
- `POST /api/v1/sessions/{sessionId}/quests/{questCode}/start?playerId={playerId}`.
- `POST /api/v1/sessions/{sessionId}/quests/{questCode}/advance?playerId={playerId}` con body `AdvanceQuestRequest` (`choiceKey`).

Todavía no hay ninguna integración entre `ActionServiceImpl`/interpretación de texto y las misiones: `advanceQuest` se invoca explícitamente con un `choiceKey` conocido de antemano (provisional, igual que `actionType`/`targetNpcId` hoy), pendiente de que la interpretación real (punto 9) traduzca texto libre a una elección de misión válida.

Test añadido: `QuestServiceImplTest` con Mockito (misión desconocida, arranque idempotente, transición inválida, misión ya completada, transición válida que completa la misión, listado de misiones visibles).

### Punto 6 implementado: combate básico

Migración `Project GM/src/main/resources/db/migration/V6__create_combat_and_combat_participant.sql`: añade `strength`/`agility`/`intellect`/`willpower`/`perception`/`presence`/`health_maximum` a `npc` (con valores concretos por NPC seed, necesarios para iniciativa y ataques), y crea `combat` (`session_id`, `status` ACTIVE/COMPLETED, `round_number`, `current_turn_order`, `created_at`, `ended_at`; índice único parcial que solo permite un combate ACTIVE por sesión) y `combat_participant` (`combat_id`, `team` PLAYER/ENEMY, `character_id` o `npc_id` -exactamente uno-, `name`, `initiative`, `turn_order` único por combate, `actions_remaining`, `health_current`/`health_maximum`, `status` ACTIVE/DOWNED/DEAD).

Entidad `Npc` ahora también tiene `attributes` (`AttributeSet`, mismo embeddable que `Character`) y `healthMaximum`, para poder tratar a los NPCs como combatientes con las mismas reglas que el jugador.

Entidades nuevas en `domain.entity`: `Combat` (con `advanceTurn`, `startNewRound`, `complete`), `CombatParticipant` (con `consumeAction`, `resetActions`, `applyDamage` -a 0 de salud pasa a `DOWNED`, nunca a `DEAD` todavía-), `CombatStatus`, `CombatTeam`, `CombatParticipantStatus` (`DEAD` queda reservado para reglas de muerte futuras, sección 12 del GDD). Repositorios nuevos: `CombatRepository` (`findBySessionIdAndStatus`), `CombatParticipantRepository` (`findAllByCombatIdOrderByTurnOrderAsc`).

Servicio nuevo `pab.rpg.service.CombatService` (con `CombatView`/`ParticipantView` anidados, mismo patrón que `QuestService.QuestStateView`) / `impl.CombatServiceImpl`:

- `startCombat(sessionId, npcIds)`: rechaza si ya hay un combate ACTIVE en la sesión, o si algún NPC no existe/no está `ALIVE`/no está en la localización actual de la sesión (`CombatNotAllowedException`). Crea un participante PLAYER a partir del `Character` de la sesión y un participante ENEMY por cada NPC, tira iniciativa (`d20 + modificador de Agilidad`, con `SecureRandom`, sin persistir semilla porque no es una tirada de acción auditable como las de `CheckResolver`) y ordena por iniciativa descendente para asignar `turn_order`. Añade evento `COMBAT_STARTED`.
- `performAttack(sessionId, combatId, attackerParticipantId, targetParticipantId)`: valida que el combate esté ACTIVE, que sea el turno del atacante (`current_turn_order`), que el atacante esté `ACTIVE` y tenga acciones, y que el objetivo esté en el equipo contrario y `ACTIVE` (`CombatNotAllowedException` en cualquier otro caso). Resuelve el ataque con `CheckResolver` (atributo Fuerza del atacante -de `Character` o `Npc` según corresponda-, dificultad fija `MODERATE`, igual de provisional que en `ActionServiceImpl` hasta que existan armas/`Item`). El daño sale de una tabla fija por `ResultGrade` (`GRAN_EXITO` 8, `EXITO` 5, `EXITO_CON_COSTE` 3, fracasos 0), ya que todavía no existe sistema de objetos/armas. Añade evento `COMBAT_ATTACK_RESOLVED`; si un bando queda sin participantes `ACTIVE`, marca el combate `COMPLETED` y añade `COMBAT_ENDED` con `outcome` VICTORY/DEFEAT; si no, cuando el atacante agota sus 2 acciones pasa el turno al siguiente participante `ACTIVE` en orden de iniciativa, y si se vuelve a la cabeza de la lista incrementa `round_number` y resetea las acciones de todos los participantes `ACTIVE`.
- `getActiveCombat(sessionId)`: lectura de solo consulta del combate ACTIVE de la sesión, si existe.

Errores nuevos: `pab.rpg.exception.CombatNotFoundException` → 404 `COMBAT_NOT_FOUND`; `pab.rpg.exception.CombatNotAllowedException` → 422 `COMBAT_NOT_ALLOWED` en `GlobalExceptionHandler`.

Endpoints nuevos en `pab.rpg.api.controller.CombatController` (mismo patrón que `QuestController`: valida la sesión con `GameSessionService.getSession` antes de delegar):

- `GET /api/v1/sessions/{sessionId}/combat?playerId={playerId}`: combate activo (404 si no hay ninguno).
- `POST /api/v1/sessions/{sessionId}/combat/start?playerId={playerId}` con body `StartCombatRequest` (`npcIds`).
- `POST /api/v1/sessions/{sessionId}/combat/{combatId}/attack?playerId={playerId}` con body `PerformAttackRequest` (`attackerParticipantId`, `targetParticipantId`).

Sin integración todavía con `ActionServiceImpl`/interpretación de texto: el combate se inicia y se juega con IDs explícitos de NPCs y participantes, igual que `actionType`/`targetNpcId`/`choiceKey` hoy, pendiente del punto 9 (interpretación real). Tampoco existen todavía "moverse", "defenderse", "usar objeto" ni huida explícita del GDD sección 11 (solo atacar), ni sistema de armas/objetos (el daño es una tabla fija provisional).

Test añadido: `CombatServiceImplTest` con Mockito (combate ya activo, NPC fuera de la localización, creación de participantes ordenados por iniciativa, ataque que reduce salud y termina el combate al derrotar al bando enemigo, ataque fuera de turno). También se actualizaron `NpcTargetRuleTest` y `NpcServiceImplTest` al nuevo constructor de `Npc` (con `attributes`/`healthMaximum`).

### Punto 8 implementado: narración conectada a MasterAdapter

`ActionServiceImpl` ya no usa un `narrationFor()` fijo por `ResultGrade`: inyecta `MasterAdapter` y `LocationRepository`, y llama a `masterAdapter.narrate(new NarrationRequest(sceneSummary, command.text(), resolution.grade(), eventsSummary))`, donde `sceneSummary` es `nombre — descripción` de la localización actual de la sesión y `eventsSummary` es la lista de eventos generados (`String.join(", ", events)`).

`CombatServiceImpl` hace lo mismo solo en `performAttack` (única operación con una tirada/`ResultGrade` que narrar): construye `actionText` como "`<atacante> ataca a <objetivo>`" y `eventsSummary` con el daño y el estado del objetivo. `startCombat`/`getActiveCombat` no generan narración (no hay tirada que narrar y evitar llamar a OpenAI en cada consulta de solo lectura). Para soportar esto, `CombatService.CombatView` y `CombatResponse` ganan un campo `narration` (nullable).

Con `openai.enabled=false` (perfil `test`, o `local` si se pone `OPENAI_ENABLED=false`) el resultado es idéntico a antes (el `StubMasterAdapter` devuelve las mismas frases fijas). Con `openai.enabled=true` (perfiles `local` y `cloud`) la narración la genera el modelo real.

Sigue sin interpretación de texto libre: `actionType`/`targetNpcId` en acciones, `choiceKey` en misiones y los IDs de combate se siguen pasando explícitos desde el cliente; eso queda para el punto 9.

### Punto 9 implementado: interpretación de texto libre (acciones)

Alcance decidido con el usuario: solo acciones (`actionType`/`targetNpcId`), dejando combate (`request_combat`) y misiones (`choiceKey`) con sus IDs explícitos por ahora — son integraciones más grandes que tocan `CombatService`/`QuestService` y quedan para más adelante. Contrato del endpoint elegido: compatible, no el exacto del TDD — `actionType`/`targetNpcId` en `SubmitActionRequest`/`SubmitActionCommand` pasan a ser **opcionales**; si el cliente los envía, se usan tal cual (así siguen funcionando los tests/clientes existentes); si se omiten, `ActionServiceImpl` los deriva del texto libre vía `MasterAdapter.interpret(...)`.

`MasterAdapter` gana `ActionIntent interpret(InterpretationRequest request)`, con `InterpretationRequest(sceneSummary, playerText, List<VisibleNpc> visibleNpcs)` (`VisibleNpc(id, name)`) y `ActionIntent(ActionType actionType, UUID targetNpcId)`. `ActionServiceImpl` construye `visibleNpcs` a partir de `npcService.getNpcsAtLocation(locationId)` (los NPCs de la localización actual) para que la IA/heurística solo pueda referenciar NPCs reales y visibles.

- `StubMasterAdapter.interpret()`: heurística de palabras clave simple y determinista (sin red) — "atac/golpe/pelea/lanz/empuj" → PHYSICAL, "habl/convenc/pregunt/negoci/salud" → SOCIAL, "busc/investig/examin/inspeccion" → INVESTIGATION, si no EXPLORATION; `targetNpcId` es el primer NPC visible cuyo nombre aparece en el texto (o `null`).
- `OpenAiMasterAdapter.interpret()`: usa **Structured Outputs** de la Responses API (`text.format = {type: "json_schema", name: "action_intent", strict: true, schema: {...}}` con `actionType` enum de los 4 valores y `targetNpcId` string-o-null) para forzar una respuesta JSON válida; el texto de `output[].content[].text` (mismo mecanismo de extracción que narrate()) es en este caso el JSON serializado, que se parsea con Jackson (`ObjectMapper` inyectado, el mismo bean de `JacksonConfig`) a un record interno `RawActionIntent(actionType, targetNpcId)`.

Manejo de referencias alucinadas (`ARCHITECTURE_CONTRACT.md` sección 25, "nunca crear automáticamente la entidad"): si `targetNpcId` no es un UUID válido, el adaptador lo descarta (`null`) sin lanzar error; si es un UUID válido pero de un NPC que no existe o no está en la localización actual, **no se valida en el adaptador** — se reutiliza `NpcTargetRule` (ya existente, ejecutado como parte de `gameRules.forEach(rule -> rule.check(context))`), que ya rechaza esa situación con `ActionNotAllowedException` → 422. Así se evita duplicar la validación en dos sitios. Un JSON inválido, un `actionType` desconocido o un fallo de red al interpretar lanzan `AiUnavailableException` → 503 `OPENAI_UNAVAILABLE`, sin mutar estado (igual que en `narrate()`).

Test añadido: `StubMasterAdapterTest` (heurística de `actionType`/`targetNpcId` por texto) y `OpenAiMasterAdapterTest` (parseo de la respuesta JSON Schema simulada, incluida una prueba de que un `targetNpcId` no-UUID como `"chair-999"` se descarta en vez de fallar); `ActionServiceImplTest` (nuevo caso con `actionType=null` que verifica la llamada a `masterAdapter.interpret(...)`).

### Punto 10 implementado: interpretación de texto libre (combate y misiones)

`MasterAdapter` gana un método genérico reutilizable por ambos casos: `String selectCandidate(CandidateSelectionRequest request)`, con `CandidateSelectionRequest(playerText, List<Candidate> candidates)` y `Candidate(id, label)` — "elige uno de estos" en vez de una clasificación fija como `interpret()`. Devuelve el `id` del candidato elegido o `null`; nunca inventa un id fuera de la lista.

- `StubMasterAdapter.selectCandidate()`: primer candidato cuyo `label` aparece en el texto (o `null`).
- `OpenAiMasterAdapter.selectCandidate()`: igual que `interpret()`, Structured Outputs con `text.format=json_schema`, pero aquí el `enum` del campo `candidateId` se **construye dinámicamente en cada llamada** a partir de `request.candidates()` (más los `null`) — más fuerte que el enum estático de `interpret()`, porque el modelo solo puede elegir un id real de *esa* llamada concreta.

**Combate**: `CombatService` gana `CombatView performAttack(UUID sessionId, UUID combatId, String playerText)` (sobrecarga junto a la versión con IDs explícitos, que se mantiene). El atacante se deriva automáticamente (el participante cuyo `turnOrder` coincide con `combat.getCurrentTurnOrder()`; si no es del equipo `PLAYER`, `CombatNotAllowedException` — "no es el turno del jugador"), así que el texto libre solo necesita identificar el objetivo. Los candidatos son los participantes `ENEMY` `ACTIVE` (id=`participantId`, label=nombre). El id elegido se parsea como UUID (si no lo es, o si `selectCandidate` devuelve `null`, `CombatNotAllowedException`: "No se identifica un objetivo claro para atacar") y se delega en el `performAttack(sessionId, combatId, attackerParticipantId, targetParticipantId)` ya existente, que revalida todo igual que antes (turno, equipo, estado). `PerformAttackRequest` gana un campo `text`; si `targetParticipantId` es `null` en el body, `CombatController` usa la ruta de texto.

**Misiones**: `QuestService` gana `QuestStateView advanceQuestFromText(UUID sessionId, String questCode, String playerText)`. Los candidatos son las `QuestStageTransition` disponibles desde el `currentStageId` (nueva query `QuestStageTransitionRepository.findAllByQuestIdAndFromStageId`), con `id=label=choiceKey` (no hay una descripción humana separada del `choiceKey` en el modelo actual). El `choiceKey` elegido se delega en `advanceQuest(sessionId, questCode, choiceKey)` ya existente (revalida la transición). Si `selectCandidate` devuelve `null`, `QuestTransitionNotAllowedException`: "No se identifica una decisión clara para esta etapa". `AdvanceQuestRequest` gana un campo `text`; si `choiceKey` es `null` en el body, `QuestController` usa la ruta de texto. `QuestServiceImpl` pasa a inyectar `MasterAdapter`.

Mismo patrón de manejo de alucinaciones que en el punto 9: el adaptador solo descarta ids con formato inválido; un id con formato válido pero que no corresponde a una entidad real de *ese* combate/*esa* misión sigue rechazado por la validación ya existente (`findParticipant`, `findByQuestIdAndFromStageIdAndChoiceKey`), sin duplicar lógica.

Test añadido: `CombatServiceImplTest` (resuelve el objetivo vía `masterAdapter.selectCandidate`; lanza `CombatNotAllowedException` si no hay coincidencia), `QuestServiceImplTest` (resuelve `choiceKey` vía `masterAdapter.selectCandidate`; lanza `QuestTransitionNotAllowedException` si no hay coincidencia), `StubMasterAdapterTest`/`OpenAiMasterAdapterTest` (nuevos casos para `selectCandidate`, incluida la respuesta `candidateId: null`).

### Punto 7 implementado: adaptador OpenAI con stub

Nueva interfaz `pab.rpg.service.MasterAdapter` (puerto del dominio hacia el proveedor de IA, con el método `narrate(NarrationRequest)`; `NarrationRequest` lleva escena, texto de la acción, `ResultGrade` ya resuelto y resumen de eventos) que aisla al dominio de OpenAI, igual que exige `ARCHITECTURE_CONTRACT.md`: la IA solo narra un resultado que el motor ya decidió, nunca lo decide ella.

Dos implementaciones en `service.impl`, seleccionadas por la propiedad `openai.enabled` (ya existía en `application-local.yml`/`application-test.yml`/`application-cloud.yml`):

- `StubMasterAdapter` (`@ConditionalOnProperty(openai.enabled=false, matchIfMissing=true)`, activo en el perfil `test` y en `local` solo si se fija `OPENAI_ENABLED=false`): devuelve las mismas frases fijas en español que ya usaba `ActionServiceImpl` por `ResultGrade`, sin llamadas de red.
- `OpenAiMasterAdapter` (`@ConditionalOnProperty(openai.enabled=true)`, activo por defecto en `local` y en `cloud`): único componente que conoce el protocolo HTTP de OpenAI (Responses API) mediante `RestClient`; construye la petición con `instructions` (system prompt fijo en español que prohibe inventar hechos/daño) e `input` (escena/acción/resultado/eventos), y extrae la narración de `output[].content[].text` (la API HTTP cruda no expone el campo de conveniencia `output_text` de los SDKs oficiales). Cualquier fallo de red o respuesta sin narración lanza `AiUnavailableException`.

Nueva clase `pab.rpg.config.OpenAiProperties` (`@ConfigurationProperties(prefix="openai")`, registrada vía `@ConfigurationPropertiesScan` en `Application`) con `enabled`/`apiKey`/`baseUrl`/`model`/`maxOutputTokens`/`temperature`. Error nuevo `pab.rpg.exception.AiUnavailableException` → 503 `OPENAI_UNAVAILABLE` en `GlobalExceptionHandler` (código ya prevista en TDD MVP v0.2 sección 9.5).

Todavía sin integrar en `ActionServiceImpl`/`CombatServiceImpl`: la narración de acciones y combate sigue generada por los métodos fijos existentes (`narrationFor` en `ActionServiceImpl`); conectar `MasterAdapter` a esos flujos es el punto 8 (ver más abajo), y la interpretación real de texto libre es el punto 9.

Test añadido: `StubMasterAdapterTest` (una frase por `ResultGrade`) y `OpenAiMasterAdapterTest` con `MockRestServiceServer` enlazado a `RestClient.Builder` (extrae narración de una respuesta simulada de la Responses API; verifica que un error 5xx lanza `AiUnavailableException`), sin necesidad de WireMock ni credenciales reales.

## Orden recomendado de trabajo

1. Implementar eventos e idempotencia. ✅
2. Implementar reglas y primera acción sin OpenAI. ✅
3. Añadir localizaciones (`Location`) y sustituir el atributo/dificultad fijos de `ActionServiceImpl` por un `ActionType` mínimo + `GameRule`s reales que usen la localización actual. ✅
4. Añadir NPCs, relaciones y memoria básica (requiere localizaciones del punto 3). ✅ (relaciones se leen y se escriben desde acciones `SOCIAL`; `NpcKnowledgeFact` sigue sin llamador real, pendiente de diálogo/investigación/misiones)
5. Añadir misiones. ✅ (máquina de estados con ramas, sin integrar todavía con `ActionServiceImpl`/interpretación de texto)
6. Implementar combate. ✅ (iniciativa, turnos con 2 acciones, ataque y daño por tabla fija, sin armas/objetos ni integración con `ActionServiceImpl`/interpretación de texto)
7. Añadir el adaptador OpenAI con stub para pruebas. ✅ (`MasterAdapter` + `StubMasterAdapter`/`OpenAiMasterAdapter` según `openai.enabled`)
8. Conectar `MasterAdapter` a `ActionServiceImpl`/`CombatServiceImpl` para narración real. ✅ (ver detalle abajo; sigue sin interpretación de texto libre)
9. Integrar interpretación de texto libre. ✅ (solo acciones: `actionType`/`targetNpcId`; combate y misiones siguen con IDs explícitos — ver detalle abajo)
10. Interpretación de texto libre para combate (ataque) y misiones (`choiceKey`). ✅ (ver detalle abajo)

Motivo del cambio de orden: `GameSession.currentLocationId` ya existe pero no apunta a ninguna entidad real, y `ActionServiceImpl` resuelve toda acción con un único camino fijo (Intelecto/MODERATE) sin usar reglas ni contexto. Introducir localizaciones y un motor mínimo de reglas por tipo de acción antes de los NPCs evita añadirlos "flotando" sin ubicación y acerca el motor al modelo de `ARCHITECTURE_CONTRACT.md` (entidades + reglas, no un único camino hardcodeado).

## Restricciones de colaboración

- El usuario ejecuta manualmente compilación y pruebas.
- No ejecutar Maven, compilación ni pruebas automáticamente salvo que el usuario lo solicite expresamente.
- Antes de editar archivos modificados recientemente por el usuario, leer su contenido actual.
- No usar Docker ni Docker Compose.
- No hacer commits ni crear ramas salvo petición explícita.
- Mantener los cambios pequeños y alineados con el TDD.

## Estado de la sesión

Los puntos 6 a 10 (combate, adaptador OpenAI con stub, narración conectada, e interpretación de texto libre para acciones/combate/misiones) están terminados y verificados: el proyecto compila y los 86 tests pasan. Los tres endpoints de intención del jugador (`POST .../actions`, `POST .../combat/{combatId}/attack`, `POST .../quests/{questCode}/advance`) aceptan ahora tanto IDs/claves explícitos (compatibilidad) como texto libre interpretado vía `MasterAdapter` (`interpret()` para acciones, `selectCandidate()` reutilizado para combate y misiones).

### Punto 13 implementado: observabilidad (correlationId, logs estructurados, métricas)

`pab.rpg.config.CorrelationIdFilter` (`OncePerRequestFilter`, orden `HIGHEST_PRECEDENCE`): añade `correlationId` (del header `X-Correlation-Id` si viene, si no un UUID nuevo) y, cuando la URL contiene `/sessions/{uuid}`, `sessionId`, ambos al MDC de SLF4J; devuelve el `correlationId` en la respuesta con el mismo header y limpia el MDC en el `finally`. `GlobalExceptionHandler.ApiError` gana el campo `correlationId` (leído del MDC), cumpliendo el formato de error de la sección 9.5 del TDD.

Logs estructurados en JSON: `logback-spring.xml` nuevo con `LogstashEncoder` (dependencia `net.logstash.logback:logstash-logback-encoder`), incluye `correlationId`/`sessionId` del MDC en cada línea. Los niveles por logger (`logging.level.pab.rpg`, etc.) se siguen controlando desde `application-*.yml` como antes: Spring Boot aplica esas propiedades después de parsear el XML, así que no hace falta duplicarlas en el XML.

Métricas Micrometer (expuestas ya en `/actuator/metrics` — `management.endpoints.web.exposure.include` ganó `metrics` en `application.yml` y en `application-local.yml`):

- `pab.rpg.action.duration` (timer, tag `outcome=success|error`) alrededor de `ActionServiceImpl.submitAction`.
- `pab.rpg.actions.resolved` (counter, tag `actionType`) cuando una acción pasa las `GameRule`s y se va a resolver.
- `pab.rpg.session.version.conflicts` (counter) en `GlobalExceptionHandler`, incrementado tanto para `StaleSessionVersionException` como para `ObjectOptimisticLockingFailureException`.
- `pab.rpg.validation.errors` (counter) en `GlobalExceptionHandler.handleInvalidRequest` (`INVALID_REQUEST`/400).
- `pab.rpg.sessions.created` / `pab.rpg.sessions.retrieved` (counters) en `GameSessionServiceImpl.createSession`/`getSession`.
- `pab.rpg.openai.duration` (timer, tags `operation=narrate|interpret|selectCandidate`, `outcome=success|error`) y `pab.rpg.openai.errors` (counter, tag `operation`) en `OpenAiMasterAdapter`, envolviendo las tres llamadas HTTP.
- `pab.rpg.openai.tokens` (counter, tags `operation`, `direction=input|output`) leídos del campo `usage.input_tokens`/`usage.output_tokens` de la Responses API (nuevo record `Usage` en `OpenAiMasterAdapter`, con `@JsonProperty` porque el `ObjectMapper` no usa snake_case).
- `pab.rpg.openai.estimated.cost.usd` (counter global, sin tags de alta cardinalidad) calculado a partir de `openai.cost-per-input-token-usd`/`openai.cost-per-output-token-usd` (nuevas propiedades en `OpenAiProperties`, por defecto `0` — si no se configuran, el coste estimado es 0). Decisión deliberada: el coste NO se etiqueta por `sessionId` en Micrometer (cardinalidad no acotada); en su lugar se loguea una línea `openai_cost_estimate` con tokens/coste, que ya lleva `sessionId` en el JSON gracias al MDC de `CorrelationIdFilter` — así el coste por sesión se puede extraer de los logs sin ensuciar las métricas.

Sin cambios en `StubMasterAdapter` (no llama a red, no genera coste real). Tests actualizados por los nuevos parámetros de constructor: `ActionServiceImplTest` y `OpenAiMasterAdapterTest` ahora instancian un `SimpleMeterRegistry` de test; `OpenAiMasterAdapterTest`/`ActionServiceImplTest` no verifican las métricas en sí (fuera de alcance de esos tests unitarios), solo que el código sigue compilando y comportándose igual.

Pendiente dentro de observabilidad (no implementado, fuera de alcance mínimo del TDD §14): dashboards/backend externo de métricas y logs (el TDD deja el backend sin decidir), rate limiting/cuotas por usuario (eso es más bien seguridad/límites, ver `MVP v0.3.md`).

### Higiene de secretos y variables de entorno locales

Se encontró y corrigió dos veces en la misma sesión una API key real de OpenAI hardcodeada como valor por defecto de `OPENAI_API_KEY` en `application-local.yml` (nunca llegó a `origin`, pero sí llegó a estar commiteada en local una vez). Ahora `${OPENAI_API_KEY}` no tiene default, igual que en `cloud`.

Se creó `Project GM/.env.local` (añadido a `.gitignore` junto con `.env*`) como fichero de almacenamiento puro (formato dotenv) de los valores locales actuales de todas las variables de entorno referenciadas en `application-local.yml`: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `OPENAI_ENABLED`, `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `OPENAI_MODEL`, `OPENAI_MAX_OUTPUT_TOKENS`, `OPENAI_TEMPERATURE`, `OPENAI_CONNECT_TIMEOUT`, `OPENAI_READ_TIMEOUT`. Este fichero NO se carga automáticamente por Spring Boot/Maven; hay que volcarlo a variables de entorno de la sesión de PowerShell a mano antes de `mvn spring-boot:run` si se quiere usar. `spring.datasource.username`/`password` en `application-local.yml` pasaron de estar hardcodeados (`admin`/`admin`) a `${DATABASE_USERNAME:admin}`/`${DATABASE_PASSWORD:admin}`, mismo patrón que el resto de variables del perfil local.

## Próximo paso cuando se retome

No queda ningún punto pendiente del "Orden recomendado de trabajo" original (1–10). El vertical slice MVP v0.2 tiene interpretación de texto libre en sus tres flujos principales, y observabilidad mínima (correlationId, logs JSON, métricas Micrometer) implementada (ver detalle arriba). Los pasos siguientes del TDD MVP v0.2 (no abordados todavía): 12) seguridad JWT, 14) pruebas end-to-end del vertical slice completo (más allá de lo ya cubierto en `Project GM automatics/`). También quedan mejoras de alcance dentro del combate (moverse/defenderse/usar objeto, sistema de armas/objetos — GDD sección 11) y de misiones/NPCs (diálogo real que use `NpcKnowledgeFact`, todavía sin llamador).
