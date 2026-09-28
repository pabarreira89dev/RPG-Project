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
