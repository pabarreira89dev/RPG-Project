# language: es
Característica: Combate por turnos
  Como jugador
  Quiero iniciar y resolver combates contra NPCs
  Para que el motor calcule iniciativa, turnos y daño de forma determinista

  Escenario: Iniciar combate contra un NPC de la localización actual
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador inicia combate contra "village_guard"
    Entonces la operación responde con estado 200

  Escenario: Iniciar combate contra un NPC que no está en la localización actual
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador inicia combate contra "forest_hunter"
    Entonces la operación responde con estado 422
    Y el error devuelto tiene el código "COMBAT_NOT_ALLOWED"

  Escenario: Consultar el combate activo cuando no hay ninguno
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador consulta el combate activo
    Entonces la operación responde con estado 404
    Y el error devuelto tiene el código "COMBAT_NOT_FOUND"

  Escenario: Completar un combate atacando hasta el final
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha iniciado combate contra "village_guard"
    Cuando el jugador ataca hasta terminar el combate
    Entonces el combate aparece como completado
