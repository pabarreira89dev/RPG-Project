# language: es
Característica: Gestión de sesiones de juego
  Como jugador
  Quiero crear, consultar y listar mis sesiones de juego
  Para poder continuar mi partida

  Escenario: Crear una sesión válida en la plaza de la aldea
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Cuando el jugador crea una sesión de juego en la localización "village_square"
    Entonces la operación responde con estado 201
    Y la sesión pertenece al personaje "Aldric"
    Y la localización actual de la sesión es "village_square"

  Escenario: Consultar una sesión existente
    Dado un nuevo personaje llamado "Elara" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "tavern"
    Cuando el jugador consulta esa sesión
    Entonces la operación responde con estado 200
    Y la sesión pertenece al personaje "Elara"
    Y la localización actual de la sesión es "tavern"

  Escenario: Consultar una sesión con un jugador que no es el dueño
    Dado un nuevo personaje llamado "Elara" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "tavern"
    Cuando otro jugador distinto consulta esa sesión
    Entonces la operación responde con estado 404
    Y el error devuelto tiene el código "SESSION_NOT_FOUND"

  Escenario: Consultar una sesión inexistente
    Dado un jugador cualquiera
    Cuando el jugador consulta una sesión con un identificador aleatorio
    Entonces la operación responde con estado 404
    Y el error devuelto tiene el código "SESSION_NOT_FOUND"

  Escenario: Listar las sesiones de un jugador con varias partidas
    Dado un nuevo personaje llamado "Bram" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha creado una sesión de juego en la localización "forest_edge"
    Cuando el jugador lista sus sesiones
    Entonces la operación responde con estado 200
    Y la lista devuelta contiene 2 sesiones

  Escenario: Listar las sesiones de un jugador sin partidas
    Dado un jugador cualquiera
    Cuando el jugador lista sus sesiones
    Entonces la operación responde con estado 200
    Y la lista devuelta contiene 0 sesiones
