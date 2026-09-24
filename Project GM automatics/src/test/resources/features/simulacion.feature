# language: es
Característica: Simulación de partida
  Como jugador
  Quiero completar varias acciones sociales con un NPC, en una sesión de juego existente, con un personaje que ya existe
  Para confirmar que se puede tener relación con un NPC

  Escenario: Completar varias acciones sociales con un NPC visible
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Cuando el jugador tiene una sesión de juego existente en la localización "village_square"
    Entonces la operación responde con estado 201
    Y el jugador ha enviado la acción "Saludo en voz alta a los aldeanos"


  Esquema del escenario: Continuar la sesión y la relación con el NPC visible
    Dado un personaje cuyo identificador es "<player_id>"
    Y una sesión cuyo identificador es "<session_id>"
    Cuando el jugador consulta esa sesión
    Entonces la operación responde con estado 200
    Y el jugador ha enviado la acción "Le digo al anciano que mejor se esté callado sino quiere problemas"
    Ejemplos:
      | player_id                            | session_id                           |
      | 7d6afa27-6396-4176-b156-047361c68ae0 | 9392c495-5887-4d4a-b979-6cddeff6c14f |

