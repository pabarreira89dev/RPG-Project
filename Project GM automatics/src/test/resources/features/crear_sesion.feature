# language: es
Característica: Crear sesión de juego
  Como jugador
  Quiero crear una nueva sesión de juego
  Para poder empezar a jugar con mi personaje

  Escenario: Crear una sesión válida en la plaza de la aldea
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Cuando el jugador crea una sesión de juego en la localización "village_square"
    Entonces la sesión se crea correctamente
    Y la sesión pertenece al personaje "Aldric"
    Y la localización actual de la sesión es "village_square"
