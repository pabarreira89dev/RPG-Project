# MVP v0.3: cierre del alcance técnico del TDD

El vertical slice de [MVP v0.2](MVP%20v0.2.md) ya es jugable de extremo a extremo (sesión, acciones libres, NPCs/relaciones, misiones ramificadas, combate, narración por IA e interpretación de texto libre). Esta versión no añade contenido narrativo nuevo: cierra los puntos de [TDD MVP v0.2.md](TDD%20MVP%20v0.2.md) que quedaron pendientes, sin importar el orden original de implementación (sección 17 del TDD).

Ver [CONTEXTO_PROYECTO.md](CONTEXTO_PROYECTO.md) para el detalle de lo ya implementado.

## Alcance

|Elemento                          |Estado antes de v0.3          |Objetivo v0.3                                          |Referencia TDD|
|-----------------------------------|-------------------------------|--------------------------------------------------------|---|
|Seguridad                         |`playerId` en body/query, sin autenticación |JWT validado por Spring Security; `playerId` sale del token |§12|
|Observabilidad                    |Sin correlationId ni métricas |Actuator, logs estructurados con `correlationId`, métricas Micrometer |§14|
|Inventario / objetos              |No implementado               |Entidad `Item`, eventos `ITEM_ACQUIRED`, objetos con propietario/ubicación |§3.1, §7.2, §15|
|Habilidades y modificadores       |Solo atributo base (sin `skillBonus`/circunstancial) |`SkillSet` por personaje y modificadores circunstanciales -3..+3 |§8.2|
|Combate: acciones más allá de atacar |Solo "atacar"                |Moverse, defenderse, usar objeto, huida (GDD §11)       |§8.5|
|Combate: armas reales             |Daño por tabla fija de `ResultGrade` |Daño derivado de la definición del arma (`Item`)         |§8.5|
|Salud y muerte                    |`DOWNED` sin seguimiento; `PLAYER_DEAD` sin usar |Estabilizar/curar/transportar a un `DOWNED`; riesgo de muerte si nadie interviene; modo estándar (GDD §12-13) |§8.5, §15|
|Memoria de conversación            |Cada acción se interpreta sin historial |`conversation_turn`: resumen de conversación reciente enviado a OpenAI |§7.1, §10, §11|
|Resiliencia ante OpenAI            |Fallo → 503 sin reintento     |Reintento con backoff limitado solo en llamadas idempotentes ante throttling |§13|
|Límites por usuario                |Sin límites                   |Límite de tamaño de texto ya existe (2000); añadir límite de acciones/llamadas a OpenAI por usuario |§12|
|E2E completo del vertical slice   |Cubre sesiones y acciones     |Añadir combate, misión con rama, reinicio de la aplicación y recuperación de partida con consecuencias persistentes |§15|

## Objetivos funcionales

Al terminar esta versión:

- ningún cliente puede suplantar a otro jugador ni forzar un `playerId` ajeno;
- toda petición es trazable mediante `correlationId` en logs y métricas;
- el jugador puede recoger, portar y usar objetos básicos, y el arma equipada influye en el daño de combate;
- una tirada puede beneficiarse de una habilidad entrenada o un modificador circunstancial válido, no solo del atributo;
- el combate ofrece más de una acción táctica por turno además de atacar;
- un personaje derribado puede ser estabilizado, curado o puede morir si nadie interviene;
- la IA recibe un resumen de la conversación reciente, no solo la última acción;
- un fallo transitorio de OpenAI se reintenta antes de devolver `OPENAI_UNAVAILABLE`;
- existe una prueba end-to-end que reinicia la aplicación y confirma que las consecuencias sobreviven.

## Exclusiones

Esta versión sigue sin incluir el alcance descartado en MVP v0.2 (economía dinámica, magia compleja, múltiples facciones, simulación global, mapa visual, multijugador, crafting, voz, generación de imágenes, editor de mundos, marketplace) ni contenido nuevo de escenario, NPCs o misiones.

## Criterio de validación

MVP v0.3 estará completo cuando se cumplan los criterios de aceptación técnico de la sección 18 del TDD que seguían abiertos: seguridad JWT operativa, observabilidad mínima disponible, inventario básico funcionando, reglas de muerte aplicadas y una suite end-to-end que verifique el vertical slice completo, incluyendo persistencia tras reiniciar la aplicación.
