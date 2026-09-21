# language: es
Característica: NPCs de la localización actual
  Como jugador
  Quiero ver los NPCs presentes en mi localización y mi relación con ellos
  Para decidir con quién interactuar

  Escenario: Consultar los NPCs de la localización actual
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador consulta los NPCs de la localización actual
    Entonces la operación responde con estado 200
    Y la lista de NPCs contiene "village_elder"

  Escenario: La relación inicial con un NPC es neutra
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Entonces la relación con "village_elder" es 0

  Escenario: Una acción social exitosa cambia la relación con el NPC
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador anota la relación actual con "village_elder"
    Cuando el jugador envía la acción "Hablo con la Anciana del pueblo"
    Entonces la operación responde con estado 200
    Y la acción registra un evento de cambio de relación
