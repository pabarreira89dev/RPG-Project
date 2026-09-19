# language: es
Característica: Recorrido completo del vertical slice
  Como jugador
  Quiero completar una acción, un combate y una misión, y recuperar la partida tras un reinicio
  Para confirmar que las consecuencias persisten de verdad y no solo en memoria

  Escenario: El estado sobrevive a un reinicio de la aplicación
    Dado un nuevo personaje llamado "Aldric" de nivel 1
    Y el jugador ha creado una sesión de juego en la localización "village_square"
    Y el jugador ha enviado la acción "Hablo con la Anciana del pueblo"
    Y el jugador anota la relación actual con "village_elder"
    Y el jugador ha iniciado combate contra "village_guard"
    Y el jugador ataca hasta terminar el combate
    Y el jugador ha iniciado la misión "village_elder_history"
    Y el jugador avanza la misión "village_elder_history" con la decisión "listen"
    Cuando se reinicia la aplicación
    Y el jugador consulta su sesión de nuevo
    Entonces la operación responde con estado 200
    Y la relación con "village_elder" sigue siendo la misma que antes del reinicio
    Y el combate aparece como completado
    Y la misión "village_elder_history" sigue en estado "COMPLETED"
