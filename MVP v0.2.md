# MVP v0.2: vertical slice jugable

Esta versión reduce el alcance para validar rápidamente si el bucle principal resulta divertido y si las decisiones producen consecuencias comprensibles y persistentes.

|Elemento         |MVP v0.2                                      |
|-----------------|----------------------------------------------:|
|Mundo            |1 escenario cerrado                           |
|Regiones         |1                                             |
|Localizaciones   |3                                             |
|NPCs             |5                                             |
|Facciones        |1                                             |
|Enemigos         |3 tipos                                       |
|Objetos          |Inventario básico                             |
|Misiones         |3 misiones relacionadas y ramificadas         |
|Jugadores        |1                                             |
|Niveles          |Progresión mínima                             |
|Duración campaña |1–2 h                                         |
|Combate          |1 sistema de combate sencillo                 |
|Inventario       |Sí, versión básica                            |
|Economía         |No, salvo recompensas y recursos esenciales   |
|Relaciones       |Sí, con NPCs principales                       |
|Memoria          |Persistencia básica de hechos relevantes      |
|Tiempo           |Avance entre acciones importantes              |
|World Tick       |No como simulación global; solo consecuencias |
|Eventos          |Registro auditable de acciones y resultados    |
|Multiplayer      |No                                            |

## Objetivos funcionales

El escenario debe permitir que el jugador:

- explore una localización;
- hable con varios NPCs;
- declare acciones libres;
- investigue un conflicto;
- resuelva al menos una acción mediante reglas;
- participe en un combate;
- elija entre ramas de una misión;
- modifique una relación o estado del mundo;
- cierre y recupere la partida conservando las consecuencias.

El objetivo de esta versión no es ofrecer una campaña completa, sino validar el siguiente ciclo:

```text
OBSERVAR → DECIDIR → ACTUAR → RESOLVER → ACTUALIZAR EL MUNDO → OBSERVAR LAS CONSECUENCIAS
```

## Exclusiones

Esta versión no incluirá:

- economía dinámica;
- magia compleja;
- múltiples facciones;
- simulación global del mundo;
- mapa visual completo;
- mundo generado infinitamente;
- PvP;
- crafting complejo;
- árbol tecnológico;
- voz avanzada;
- generación de imágenes en cada turno;
- multijugador completo;
- editor de mundos;
- marketplace;
- cientos de NPCs.

## Criterio de validación

El vertical slice será satisfactorio si, después de jugar aproximadamente dos horas, el jugador quiere volver para descubrir qué ocurre como consecuencia de sus decisiones.
