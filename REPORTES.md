# Reportes pendientes

Anotaciones de cosas a revisar/corregir más adelante, una vez se complete `MVP v0.4.md`. No es una lista de
alcance nuevo, sino de comportamientos existentes que deben pulirse. Añadir entradas nuevas al final con fecha.

## Pendientes

- **(2026-09-26) Sesión nueva sin contexto inicial al entrar por primera vez.** Al crear una partida y abrir
  `NarrationScreen` por primera vez, el historial de turnos (`ConversationTurn`/Room) está vacío hasta que el
  jugador envía su primera acción — no hay ninguna narración de apertura que describa la escena/localización
  inicial. El jugador arranca con una pantalla de chat en blanco sin saber dónde está ni qué hay a su alrededor
  hasta que actúa a ciegas. Revisar si el motor debería generar una narración inicial (evento/turno sintético)
  al crear la sesión, o si el cliente debería mostrar una descripción de la localización actual como primer
  mensaje aunque no exista un `ConversationTurn` real todavía.

- **(2026-09-26) Solo las acciones SOCIAL afectan a la relación con NPCs.** `ActionServiceImpl` solo actualiza
  `Relationship` cuando `actionType == SOCIAL` y hay `targetNpcId` (ver `/memories/repo/rpg-project.md`, punto
  ya documentado). Cualquier otra acción dirigida a un NPC (p.ej. `PHYSICAL` — agredirlo) no toca la relación en
  absoluto, aunque narrativamente sí debería tener consecuencias. Ejemplo real visto analizando la sesión
  `27128c20-544f-49b4-96ea-6ecbccd42f9d`: acción `PHYSICAL` "me acerco al guardia local y le doy una bofetada"
  contra `village_guard`, resultado `FRACASO_GRAVE` — la narración describe al guardia agarrando al jugador con
  furia, pero no se creó ninguna fila en `relationship` ni se disparó combate. Revisar si `PHYSICAL`/otras
  acciones contra un NPC concreto deberían también mover su relación (y/o disparar reglas de
  hostilidad/combate), no solo `SOCIAL`.

- **(2026-09-28) El combate solo se puede iniciar de forma explícita, no desde la ventana de narración.** Hoy
  `CombatService.startCombat` solo acepta `npcIds` explícitos (`POST /combat/start`), y el clasificador general
  de acciones libres (`MasterAdapter.interpret`, usado por `POST /actions`) no tiene ningún `ActionType.ATTACK`
  — `PHYSICAL` (la acción más parecida a "atacar") se resuelve como una tirada narrativa cosmética en
  `ActionServiceImpl` sin tocar el subsistema de Combate en absoluto (sin crear `CombatParticipant`, sin aplicar
  daño). En el cliente Android, esto obliga a iniciar combate desde un botón explícito en `NpcsScreen`, en vez
  de poder escribir algo como "ataco al guardia" en la propia pantalla de Narración. Revisar en el futuro si el
  motor debería poder interpretar esa intención desde la ventana de narración (texto libre) y arrancar combate
  automáticamente contra el NPC objetivo, en vez de requerir siempre una acción explícita fuera del chat.

- **(2026-09-28) El orden de turnos no favorece a quien inicia el combate.** Hoy `CombatServiceImpl.startCombat`
  calcula la iniciativa de TODOS los participantes (jugador y NPCs) por igual, como `d20 + modificador de
  Agilidad` (`rollInitiative`, `SecureRandom` sin auditar) y ordena por ese valor descendente — quien inicia el
  ataque no tiene ninguna ventaja narrativa de "primer golpe", puede perfectamente perder la tirada y que el NPC
  actúe primero pese a haber sido sorprendido. Revisar si el orden de ataque debería establecerse en base a
  quién inicia la acción (ese participante golpea primero, sin tirada), y que el orden de turnos A PARTIR de ese
  primer golpe se derive de comparar los valores de "prisa"/"rapidez"/Agilidad del personaje del jugador y del
  NPC, en vez del `d20 + Agilidad` actual para todos por igual.
