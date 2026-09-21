# language: es
Característica: Misiones ramificadas
  Como jugador
  Quiero iniciar misiones y avanzarlas eligiendo una rama
  Para que mis decisiones cambien el desenlace de la historia

  Escenario: Consultar las misiones visibles de una sesión nueva
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador consulta las misiones visibles
    Entonces la operación responde con estado 200
    Y la lista de misiones visibles contiene 0 misiones

  Escenario: Iniciar una misión disponible
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador inicia la misión "village_elder_history"
    Entonces la operación responde con estado 200
    Y la misión "village_elder_history" sigue en estado "ACTIVE"

  Escenario: Avanzar una misión con una decisión válida la completa
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha iniciado la misión "village_elder_history"
    Cuando el jugador avanza la misión "village_elder_history" con la decisión "listen"
    Entonces la operación responde con estado 200
    Y la misión "village_elder_history" sigue en estado "COMPLETED"

  Escenario: Avanzar una misión con una decisión que no existe
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha iniciado la misión "village_elder_history"
    Cuando el jugador avanza la misión "village_elder_history" con la decisión "opcion_inexistente"
    Entonces la operación responde con estado 422
    Y el error devuelto tiene el código "QUEST_TRANSITION_NOT_ALLOWED"
