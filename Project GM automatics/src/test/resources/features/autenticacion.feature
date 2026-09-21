# language: es
Característica: Identidad del jugador
  Como responsable de la plataforma
  Quiero que la identidad del jugador salga siempre del contexto de seguridad, nunca del cliente
  Para que un jugador no pueda suplantar a otro

  Escenario: Sin cabecera de identidad se usa el jugador de desarrollo por defecto
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Cuando el jugador crea una sesión sin indicar identidad en la localización "village_square"
    Entonces la operación responde con estado 201
    Cuando el jugador consulta esa sesión sin indicar identidad
    Entonces la operación responde con estado 200

  Escenario: Un jugador no puede consultar la sesión de otro jugador
    Dado un nuevo personaje llamado "Elara" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "tavern"
    Cuando otro jugador distinto consulta esa sesión
    Entonces la operación responde con estado 404
    Y el error devuelto tiene el código "SESSION_NOT_FOUND"
