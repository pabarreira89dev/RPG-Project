# PROJECT GM — PROMPT MAESTRO DE ARQUITECTURA E IMPLEMENTACIÓN

## 1. Objetivo del proyecto

Estoy desarrollando **Project GM**, un RPG conversacional persistente en el que una IA actúa como Game Master.

El jugador interactúa mediante lenguaje natural y puede intentar cualquier acción razonable.

La arquitectura debe garantizar una separación estricta entre:

* **IA / Game Master:** interpreta intenciones y narra resultados.
* **Game Engine:** determina qué puede ocurrir y qué ha ocurrido realmente.
* **World State:** representa el estado persistente y autoritativo del mundo.

Principio fundamental:

> **La IA cuenta lo que ocurre. El Game Engine determina lo que realmente ocurre.**

El LLM nunca debe ser la fuente de verdad de:

* HP
* daño
* inventario
* objetos existentes
* posiciones
* economía
* estadísticas
* tiradas
* resultados de combate
* relaciones
* quests
* muerte
* tiempo
* estado del mundo

---

# 2. Stack tecnológico inicial

La implementación inicial debe utilizar:

* Java 21
* Spring Boot 4
* API de OpenAI para el LLM
* MySQL (Spring Data JPA/Hibernate + Flyway) para persistencia
* JUnit para tests
* Arquitectura preparada para evolución a multiplayer

El dominio debe ser independiente de OpenAI y de MySQL.

No introducir dependencias de infraestructura dentro de `domain`.

---

# 3. Arquitectura general

La arquitectura debe seguir este flujo:

```text
PLAYER
   │
   │ lenguaje natural
   ▼
GAME API
   │
   ▼
APPLICATION / ORCHESTRATOR
   │
   ▼
OPENAI
   │
   │ interpreta intención
   ▼
ActionIntent
   │
   ▼
ACTION RESOLVER
   │
   ├── World State
   ├── Entities
   ├── Properties
   ├── Capabilities
   └── Game Rules
   │
   ▼
ACTION HANDLER
   │
   ▼
ActionResult
   │
   ▼
WorldEvent
   │
   ▼
WORLD STATE
   │
   ▼
NARRATION SERVICE
   │
   ▼
OPENAI
   │
   ▼
PLAYER
```

La IA está fuera del núcleo determinista del Game Engine.

---

# 4. Separación de responsabilidades

## LLM / OpenAI

El LLM puede:

* interpretar lenguaje natural;
* identificar intención;
* identificar entidades conocidas;
* seleccionar una acción;
* proponer parámetros;
* interpretar conversaciones;
* interpretar comportamiento de NPCs;
* generar narración;
* generar diálogos;
* adaptar la presentación al contexto.

El LLM NO puede:

* modificar directamente el World State;
* crear arbitrariamente objetos;
* inventar entidades;
* decidir resultados de dados;
* decidir daño;
* decidir HP;
* decidir si una acción es físicamente posible;
* conceder objetos;
* modificar dinero;
* matar personajes;
* completar quests;
* modificar reputación directamente.

---

# 5. Game Engine

El Game Engine es la autoridad absoluta sobre el estado del juego.

Debe encargarse de:

* validar acciones;
* comprobar entidades;
* comprobar propiedades;
* comprobar capabilities;
* comprobar accesibilidad;
* comprobar distancia;
* comprobar estado;
* ejecutar reglas;
* realizar tiradas;
* calcular resultados;
* calcular daño;
* modificar estado;
* generar eventos;
* actualizar el mundo.

El Game Engine no debe generar narrativa.

---

# 6. Modelo de dominio

La implementación debe diferenciar:

```text
EntityDefinition
        │
        ▼
WorldEntity
        │
        ▼
EntityState
```

## EntityDefinition

Describe qué es un tipo de entidad.

Ejemplo:

```java
public record EntityDefinition(
    String id,
    EntityType type,
    String name,
    Set<String> tags,
    PhysicalProperties physicalProperties,
    Map<String, Object> properties,
    Set<CapabilityType> capabilities
) {}
```

Ejemplo conceptual:

```json
{
  "id": "wooden_chair",
  "type": "OBJECT",
  "name": "Silla de madera",
  "tags": [
    "chair",
    "furniture",
    "wood",
    "movable"
  ],
  "capabilities": [
    "MOVABLE",
    "THROWABLE",
    "BLUNT_WEAPON",
    "FLAMMABLE"
  ],
  "properties": {
    "weight": 4.5,
    "flammability": 0.8
  }
}
```

---

# 7. WorldEntity

Representa una instancia concreta de una definición.

Ejemplo:

```java
public record WorldEntity(
    UUID id,
    String definitionId,
    EntityState state,
    UUID locationId,
    UUID ownerId,
    Map<String, Object> properties
) {}
```

Ejemplo:

```text
EntityDefinition:
    wooden_chair

WorldEntity:
    chair-784
```

No crear clases específicas como:

```text
Chair
WoodenChair
IronChair
BrokenChair
MagicChair
```

para cada objeto del mundo.

Utilizar composición mediante:

* tags;
* properties;
* capabilities;
* state.

---

# 8. EntityState

El estado actual de una entidad debe estar separado de su definición.

Ejemplo:

```java
public record EntityState(
    boolean destroyed,
    Map<String, Object> values
) {}
```

Los estados críticos deben utilizar estructuras tipadas cuando sea necesario.

Por ejemplo:

```java
public record HealthState(
    int current,
    int maximum,
    boolean conscious,
    boolean alive
) {}
```

No convertir todo el dominio en `Map<String,Object>`.

Los mapas se reservan principalmente para propiedades dinámicas.

---

# 9. Properties

Las entidades deben tener propiedades que permitan al motor razonar sobre interacciones.

Ejemplos:

```text
weight
size
durability
hardness
flammability
value
temperature
material
strength
```

No se debe crear una acción específica para cada combinación posible.

No:

```text
ThrowChairAtGuard
ThrowBottleAtGuard
ThrowStoneAtGuard
```

Sí:

```text
THROW
```

y el motor utiliza:

* peso;
* tamaño;
* material;
* capabilities;
* distancia;
* atributos del actor;
* reglas.

---

# 10. Capabilities

Las capacidades representan lo que una entidad puede hacer o soportar.

Ejemplo:

```java
public enum CapabilityType {

    MOVABLE,
    THROWABLE,
    OPENABLE,
    LOCKABLE,
    BREAKABLE,
    FLAMMABLE,
    CLIMBABLE,
    CONTAINER,
    WEAPON,
    BLUNT_WEAPON,
    CUTTING_WEAPON,
    USABLE,
    EQUIPPABLE
}
```

Ejemplo:

```text
CHAIR
 ├── MOVABLE
 ├── THROWABLE
 ├── BLUNT_WEAPON
 └── FLAMMABLE

DOOR
 ├── OPENABLE
 ├── LOCKABLE
 └── BREAKABLE
```

Las capabilities deben ser preferentemente datos/configuración del dominio y no una proliferación de clases Java.

---

# 11. ActionIntent

OpenAI debe convertir lenguaje natural en una estructura determinista.

Ejemplo:

```java
public record ActionIntent(
    ActionType action,
    UUID actorId,
    List<UUID> targetIds,
    List<UUID> objectIds,
    Map<String, Object> parameters
) {}
```

Ejemplo:

Jugador:

> "Cojo la silla y se la lanzo al guardia."

OpenAI:

```json
{
  "action": "THROW",
  "actorId": "player-01",
  "targetIds": ["guard-01"],
  "objectIds": ["chair-784"],
  "parameters": {
    "technique": "throw"
  }
}
```

OpenAI no debe determinar el resultado.

---

# 12. ActionType

Las acciones básicas iniciales deben incluir:

```java
public enum ActionType {

    MOVE,
    TAKE,
    DROP,
    OPEN,
    CLOSE,
    SEARCH,
    TALK,
    ATTACK,
    DEFEND,
    USE_ITEM,
    EQUIP,
    UNEQUIP,
    CLIMB,
    PUSH,
    PULL,
    THROW,
    REST,
    EMERGENT_INTERACTION
}
```

El sistema debe permitir añadir acciones sin modificar indiscriminadamente el núcleo.

---

# 13. ActionContext

Una acción debe resolverse contra un contexto coherente del mundo.

```java
public record ActionContext(
    WorldState world,
    WorldEntity actor,
    List<WorldEntity> targets,
    List<WorldEntity> objects,
    ActionIntent intent
) {}
```

El contexto debe contener toda la información necesaria para resolver la acción.

Las reglas no deben realizar llamadas repetitivas a MySQL.

---

# 14. GameRule

Las reglas deben ser independientes y componibles.

```java
public interface GameRule {

    RuleResult evaluate(ActionContext context);

}
```

Ejemplos:

```text
ActorExistsRule
ActorCanActRule
TargetExistsRule
ObjectExistsRule
ObjectAccessibleRule
TargetAccessibleRule
DistanceRule
CapabilityRule
LocationRule
StateRule
CombatRule
InventoryRule
```

Una regla debe tener una responsabilidad concreta.

Evitar una clase gigante `GameRules`.

---

# 15. RuleResult

```java
public record RuleResult(
    boolean passed,
    String code,
    String reason,
    Map<String, Object> data
) {}
```

Ejemplo:

```text
passed = false
code = OBJECT_NOT_ACCESSIBLE
reason = "The player cannot reach the chair."
```

Los códigos deben ser estables y aptos para lógica interna.

---

# 16. ActionResolver

El `ActionResolver` coordina la resolución.

Responsabilidades:

1. Construir `ActionContext`.
2. Ejecutar validaciones comunes.
3. Seleccionar el `ActionHandler`.
4. Ejecutar la acción.
5. Obtener `ActionResult`.

No debe contener cientos de `if/else` por tipo de acción.

Evitar:

```java
if (action == THROW) ...
else if (action == ATTACK) ...
else if (action == OPEN) ...
```

---

# 17. ActionHandler

Cada familia de acciones complejas debe tener su handler.

```java
public interface ActionHandler {

    boolean supports(ActionType actionType);

    ActionResult execute(ActionContext context);

}
```

Ejemplos:

```text
MoveActionHandler
TakeActionHandler
OpenActionHandler
AttackActionHandler
ThrowActionHandler
UseItemActionHandler
EmergentInteractionHandler
```

---

# 18. ActionResult

El resultado de una acción no debe ser texto narrativo.

```java
public record ActionResult(
    boolean successful,
    String outcomeCode,
    List<WorldEvent> events,
    Map<String, Object> data
) {}
```

Ejemplo:

```json
{
  "successful": true,
  "outcomeCode": "SUCCESS",
  "events": [
    {
      "type": "ITEM_THROWN"
    },
    {
      "type": "DAMAGE_DEALT"
    }
  ],
  "data": {
    "damage": 5
  }
}
```

---

# 19. WorldEvent

Los cambios importantes del mundo deben representarse mediante eventos.

```java
public record WorldEvent(
    UUID id,
    EventType type,
    Instant timestamp,
    UUID actorId,
    List<UUID> targetIds,
    Map<String, Object> data
) {}
```

Eventos iniciales:

```text
ITEM_ACQUIRED
ITEM_DROPPED
ITEM_THROWN
ITEM_USED
DAMAGE_DEALT
ENTITY_MOVED
DOOR_OPENED
DOOR_CLOSED
DOOR_BROKEN
NPC_TALKED
RELATIONSHIP_CHANGED
QUEST_STARTED
QUEST_COMPLETED
COMBAT_STARTED
COMBAT_ENDED
ENTITY_DIED
WORLD_TICK
```

Los eventos deben permitir que otros sistemas reaccionen sin acoplarse al handler que los produjo.

Por ejemplo:

```text
ENTITY_DIED
    ↓
Faction System
Memory System
Quest System
Reputation System
World Simulation
```

---

# 20. WorldState

Representa el estado autoritativo del mundo.

Inicialmente:

```java
public record WorldState(
    UUID worldId,
    Map<UUID, WorldEntity> entities,
    Map<UUID, Location> locations,
    GameTime gameTime
) {}
```

La implementación debe evolucionar posteriormente hacia agregados/repositorios especializados si el tamaño del mundo lo requiere.

---

# 21. Resolución de acciones

El motor debe distinguir:

### Acciones deterministas

Ejemplos:

```text
TAKE
DROP
MOVE
OPEN
CLOSE
EQUIP
```

cuando no existe incertidumbre.

### Skill checks

Ejemplos:

```text
ATTACK
THROW
CLIMB
SEARCH
PICK_LOCK
PERSUADE
BREAK
```

Resolución:

```text
d20
+
atributo
+
skill
+
circunstancias
vs
DC
```

### Acciones emergentes

Ejemplos:

```text
BLOCK
IMPROVISE
COMBINE
ENVIRONMENT_INTERACTION
```

Se resuelven utilizando:

```text
Entities
+
Properties
+
Capabilities
+
State
+
Rules
```

---

# 22. Principio imposible vs difícil

El sistema debe distinguir:

```text
IMPOSIBLE
DIFÍCIL
POSIBLE
AUTOMÁTICO
```

No toda acción debe producir una tirada.

Ejemplo:

> "Cojo una piedra que está a mis pies."

Puede ser automático.

> "Rompo una puerta de madera de una patada."

Puede requerir un check.

> "Intento respirar bajo el agua sin ayuda."

Debe fallar por imposibilidad, no mediante una tirada arbitraria.

---

# 23. Acciones emergentes

El jugador puede intentar interacciones que no estén explícitamente programadas.

Ejemplo:

> "Pongo la mesa delante de la puerta para bloquearla."

OpenAI:

```json
{
  "action": "EMERGENT_INTERACTION",
  "actorId": "player-01",
  "objectIds": [
    "table-01",
    "door-01"
  ],
  "parameters": {
    "relationship": "BLOCK"
  }
}
```

El engine evalúa:

```text
¿Existe la mesa?
¿Existe la puerta?
¿Son accesibles?
¿La mesa es movable?
¿Tiene suficiente peso/tamaño?
¿La puerta puede bloquearse?
¿La posición es válida?
```

Si todo es compatible:

```text
ENTITY_POSITION_CHANGED
DOOR_BLOCKED
```

No se crea una clase:

```text
BlockDoorWithTableAction
```

---

# 24. Control de acciones emergentes

Nunca permitir que el LLM convierta una interacción absurda en válida simplemente porque la ha propuesto.

Ejemplo:

> "Uso mi sombra para cortar la puerta."

El engine debe comprobar:

```text
entidades
capabilities
properties
rules
```

y devolver:

```text
INTERACTION_NOT_SUPPORTED
```

El LLM posteriormente convierte ese resultado en una narración natural.

---

# 25. Referencias alucinadas

Si OpenAI genera:

```json
{
  "objectId": "chair-999"
}
```

y esa entidad no existe:

```text
INVALID_REFERENCE
```

Nunca crear automáticamente la entidad.

Igualmente:

* entidad inexistente;
* entidad fuera del conocimiento del jugador;
* entidad inaccesible;
* entidad destruida;
* capability inexistente.

Deben ser validadas por el backend.

---

# 26. Asymmetric Knowledge

El LLM no debe recibir automáticamente todo el World State.

Debe recibir únicamente la información que el actor puede conocer.

Ejemplo:

```text
World State
     ↓
Knowledge Filter
     ↓
Player-visible State
     ↓
OpenAI
```

El jugador no debe conocer:

* NPCs ocultos;
* quests secretas;
* posiciones desconocidas;
* estadísticas internas;
* planes de facciones;
* eventos futuros.

La IA tampoco debe revelar información que el personaje no puede conocer.

---

# 27. OpenAI Tools

OpenAI debe interactuar con el backend mediante herramientas controladas (function calling).

Ejemplos:

```text
get_current_scene
get_visible_entities
get_entity
get_location
propose_action
```

La IA no debe disponer de una herramienta genérica del tipo:

```text
update_world(...)
```

No debe existir una vía por la que el LLM pueda saltarse el Game Engine.

---

# 28. Flujo completo de una acción

Ejemplo:

```text
Jugador:
"Cojo la silla y se la lanzo al guardia."
```

### Paso 1

API recibe:

```http
POST /api/games/{gameId}/actions
```

```json
{
  "message": "Cojo la silla y se la lanzo al guardia"
}
```

### Paso 2

OpenAI interpreta:

```text
ActionIntent
```

### Paso 3

Backend carga el contexto necesario.

### Paso 4

Game Engine valida:

```text
ActorExists
TargetExists
ObjectExists
Accessible
Distance
Capability
State
```

### Paso 5

`ThrowActionHandler`.

### Paso 6

Se realiza la resolución:

```text
d20
+
modificadores
+
circunstancias
```

### Paso 7

Se calcula daño.

### Paso 8

Se generan eventos.

### Paso 9

Los eventos actualizan el World State.

### Paso 10

El resultado determinista se envía al sistema de narración.

### Paso 11

OpenAI genera la narración.

---

# 29. Persistencia

Utilizar MySQL (Spring Data JPA/Hibernate + Flyway) inicialmente.

Separar conceptualmente:

```text
World
Entity
Location
Quest
Faction
Relationship
Event
```

El dominio no debe depender directamente de MySQL/JPA.

Utilizar interfaces:

```java
WorldRepository
EntityRepository
EventRepository
QuestRepository
```

y sus implementaciones en:

```text
infrastructure.persistence.jpa
```

---

# 30. Eventos y persistencia

Los eventos importantes deben conservarse.

Ejemplo:

```json
{
  "type": "DAMAGE_DEALT",
  "actorId": "player-01",
  "targetId": "guard-01",
  "data": {
    "amount": 5
  }
}
```

Esto permitirá posteriormente:

* historial;
* memoria de NPCs;
* quests;
* reputación;
* estadísticas;
* reconstrucción;
* auditoría;
* debugging;
* simulación del mundo.

No es necesario implementar Event Sourcing puro en el MVP.

Utilizar inicialmente un enfoque híbrido:

```text
Current State
+
Important Event Log
```

---

# 31. Arquitectura de paquetes

Debe mantenerse aproximadamente esta separación:

```text
com.projectgm
│
├── api
│   ├── GameController
│   └── dto
│
├── application
│   ├── GameService
│   ├── ActionApplicationService
│   └── NarrationService
│
├── domain
│   ├── entity
│   ├── action
│   ├── capability
│   ├── rule
│   ├── event
│   └── world
│
└── infrastructure
    ├── ai
    │   └── openai
    │
    └── persistence
        └── jpa
```

Regla:

```text
domain
   ↓
NO conoce infraestructura
```

---

# 32. API inicial

Endpoint:

```http
POST /api/games/{gameId}/actions
```

Request:

```json
{
  "message": "Intento abrir la puerta"
}
```

Response conceptual:

```json
{
  "narration": "La puerta permanece cerrada...",
  "outcome": "FAILED",
  "visibleState": {}
}
```

El cliente no debe recibir información interna del motor que el jugador no pueda conocer.

---

# 33. MVP del Game Engine

Antes de integrar completamente OpenAI, debe existir un motor determinista capaz de probar:

```text
TAKE chair

THROW chair at guard

OPEN door

OPEN locked door

BREAK door

ATTACK guard

BLOCK door with table

BURN curtain with oil

USE invalid object

INTERACT with inaccessible object

REFERENCE nonexistent entity
```

El engine debe funcionar correctamente sin LLM.

Después se conecta OpenAI.

---

# 34. Tests obligatorios

Debe existir una batería de tests JUnit para verificar como mínimo:

### Inventario

```text
player takes accessible item
player cannot take nonexistent item
player cannot take inaccessible item
```

### Puertas

```text
open unlocked door
cannot open locked door without key
break breakable door
cannot break impossible object
```

### Combate

```text
valid attack
invalid target
out of range attack
damage calculation
death at zero HP
```

### Objetos

```text
throw throwable object
cannot throw non-throwable object
```

### Emergentes

```text
table can block compatible door
invalid object cannot block door
oil can interact with flammable curtain
invalid interaction is rejected
```

### Seguridad del LLM

```text
LLM cannot modify HP
LLM cannot create entities
LLM cannot modify inventory directly
LLM cannot bypass rules
LLM cannot invent successful outcomes
```

---

# 35. Regla arquitectónica crítica

El siguiente flujo debe ser imposible:

```text
PLAYER
  ↓
OPENAI
  ↓
WORLD STATE
```

El único flujo permitido es:

```text
PLAYER
  ↓
OPENAI
  ↓
ACTION INTENT
  ↓
GAME ENGINE
  ↓
WORLD EVENT
  ↓
WORLD STATE
```

Y para narración:

```text
WORLD EVENT / RESULT
  ↓
OPENAI
  ↓
NARRATION
```

---

# 36. Escalabilidad futura

La arquitectura debe permitir posteriormente:

* multiplayer;
* múltiples mundos;
* campañas;
* NPCs persistentes;
* facciones;
* economía dinámica;
* quests complejas;
* world simulation;
* memoria;
* mapas;
* imágenes;
* voz;
* diferentes géneros;
* diferentes sistemas de reglas.

No diseñar el núcleo específicamente para fantasía.

El contenido debe estar desacoplado del engine.

---

# 37. Principios que NO deben romperse

Durante cualquier implementación, revisar siempre:

### 1. El LLM no es autoridad

El LLM interpreta y narra.

### 2. El Engine es determinista

Mismo estado + misma acción + misma semilla de aleatoriedad = mismo resultado.

### 3. No programar todas las acciones posibles

Programar:

```text
entities
properties
capabilities
rules
```

### 4. Composición antes que herencia

Preferir:

```text
tags + properties + capabilities
```

frente a cientos de clases.

### 5. Eventos para cambios importantes

Los sistemas secundarios deben reaccionar a eventos.

### 6. Conocimiento limitado

La IA debe recibir únicamente la información correspondiente al conocimiento del actor.

### 7. Ninguna mutación directa desde OpenAI

Todas las mutaciones pasan por el Game Engine.

### 8. El dominio no depende de infraestructura externa

OpenAI y MySQL pertenecen a `infrastructure`.

### 9. El motor debe funcionar sin IA

Debe poder probarse completamente de forma determinista.

### 10. Añadir contenido no debe requerir modificar el núcleo

Añadir una nueva espada, puerta, criatura o NPC debería ser principalmente una cuestión de datos/configuración.

---

# 38. Criterio para auditar una implementación

Cuando se revise una implementación de Project GM, comprobar:

```text
[ ] ¿El LLM está separado del Game Engine?
[ ] ¿El LLM únicamente propone ActionIntent?
[ ] ¿El LLM puede modificar directamente el estado?
[ ] ¿Existe una fuente autoritativa de World State?
[ ] ¿Las acciones se validan mediante reglas?
[ ] ¿Existen EntityDefinition y WorldEntity?
[ ] ¿Las entidades utilizan properties/capabilities?
[ ] ¿Se evita una clase por cada objeto/interacción?
[ ] ¿Existe ActionResolver?
[ ] ¿Existen ActionHandlers?
[ ] ¿Existe ActionContext?
[ ] ¿Existe ActionResult?
[ ] ¿Existen WorldEvents?
[ ] ¿Las acciones emergentes pasan por validación?
[ ] ¿Se comprueban referencias inexistentes?
[ ] ¿Se controla el conocimiento visible para el jugador?
[ ] ¿El dominio es independiente de OpenAI y MySQL?
[ ] ¿MySQL está aislado en infrastructure?
[ ] ¿El engine puede ejecutarse sin OpenAI?
[ ] ¿Existen tests de las reglas?
[ ] ¿El resultado de una acción depende del estado real?
[ ] ¿Se evita que OpenAI invente objetos/resultados?
[ ] ¿Los cambios importantes generan eventos?
```

Si alguna de estas reglas se incumple, identificar explícitamente:

1. **qué principio arquitectónico se rompe;**
2. **por qué supone un problema;**
3. **qué componente debe cambiar;**
4. **cómo corregirlo sin romper el resto de la arquitectura.**

---

# 39. Objetivo final

La arquitectura debe conseguir que el jugador pueda escribir:

> "Cojo la botella de aceite, la rompo contra el suelo y acerco una antorcha."

sin que el sistema necesite tener una acción programada llamada:

```text
BREAK_OIL_BOTTLE_AND_IGNITE_OIL
```

En su lugar:

```text
LLM
 ↓
ActionIntent
 ↓
Entities
 ↓
Properties
 ↓
Capabilities
 ↓
Rules
 ↓
Action Resolution
 ↓
World Events
 ↓
World State
 ↓
Narration
```

La complejidad emergente debe surgir de la combinación de **entidades + propiedades + capabilities + reglas**, no de una lista infinita de acciones preprogramadas.

## PRINCIPIO FINAL

> **No construir un sistema que sepa todas las acciones que puede realizar el jugador. Construir un mundo que sepa qué son sus entidades, qué propiedades tienen, qué capacidades poseen y cuáles son las reglas que gobiernan sus interacciones.**

Ese es el núcleo técnico de Project GM.
