# language: es
Característica: Envío de acciones de juego
  Como jugador
  Quiero enviar acciones en texto libre durante una sesión
  Para que el motor las interprete, valide y resuelva

  Escenario: Enviar una acción de exploración con texto libre
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador envía la acción "Miro a mi alrededor buscando algo interesante"
    Entonces la operación responde con estado 200
    Y la acción devuelve un resultado con tirada de dados
    Y la acción registra al menos un evento

  Escenario: Repetir la misma acción con la misma clave de idempotencia
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha enviado la acción "Miro a mi alrededor buscando algo interesante"
    Cuando el jugador repite la misma acción con la misma clave de idempotencia
    Entonces la operación responde con estado 200
    Y la acción devuelve el mismo identificador que la primera vez

  Escenario: Enviar una acción con una versión de sesión desfasada
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador envía una acción con una versión de sesión desfasada
    Entonces la operación responde con estado 409
    Y el error devuelto tiene el código "STALE_SESSION_VERSION"

  Escenario: Enviar una acción social dirigida a un NPC visible
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Cuando el jugador envía la acción "Hablo con la Anciana del pueblo"
    Entonces la operación responde con estado 200
    Y la acción registra un evento de cambio de relación

  Escenario: Enviar una acción social dirigida a un NPC visible con personaje y sesión existentes
    Dado un personaje que ya existe
    Y el jugador tiene una sesión de juego existente en la localización "village_square"
    Cuando el jugador envía la acción "Hablo con la Anciana del pueblo"
    Entonces la operación responde con estado 200
    Y la acción registra un evento de cambio de relación
