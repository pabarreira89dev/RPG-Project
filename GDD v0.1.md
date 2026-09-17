# GDD v0.1: RPG conversacional con IA

| Campo | Valor |
|---|---|
| **Nombre provisional** | Project GM |
| **Género** | RPG narrativo / aventura / fantasía |
| **Plataforma inicial** | Web |
| **Modo** | Solo inicialmente, arquitectura preparada para multijugador |
| **Interfaz** | Texto + mapa + elementos visuales |
| **Director de juego** | IA mediante la API de OpenAI |
| **Sistema de reglas** | Propio |
| **Filosofía del Master** | Imparcial |
| **Objetivo del MVP** | Una campaña de 5–10 horas con mundo persistente y consecuencias reales |

## 1. Visión del juego

### 1.1. Concepto

El jugador entra en un mundo de fantasía persistente dirigido por una inteligencia artificial que actúa como Master.

Pero el Master no es simplemente un chatbot que inventa una historia.

El sistema mantiene:

● reglas;

● personajes;

● inventario;

● economía;

● relaciones;

● facciones;

● localizaciones;

● misiones;

● combate;

● tiempo;

● consecuencias;

● acontecimientos del mundo;

● memoria histórica.

La IA interpreta ese mundo y decide cómo reaccionan sus habitantes, pero no puede modificar arbitrariamente las reglas.

**Principio fundamental**

> **La IA cuenta lo que ocurre. El Game Engine determina lo que realmente ocurre.**

Esto será una de las reglas arquitectónicas más importantes del proyecto.



## 2. Fantasía del jugador

La experiencia que buscamos es:

> “Puedo hacer cualquier cosa que tenga sentido, y el mundo reaccionará de forma coherente.”

No queremos:

> “Elige entre A, B o C.”

Queremos:

> **Jugador:** “Intento convencer al guardia de que soy un comerciante enviado por la casa Valen.”

El sistema determina:

● qué sabe el guardia;

● si conoce a la casa Valen;

● si el jugador tiene credibilidad;

● si existen documentos;

● cuál es la dificultad;

● qué consecuencias puede tener el engaño.



## 3. Pilares de diseño

El juego se construirá alrededor de seis pilares.

### 1. Agencia

El jugador puede intentar cualquier acción razonable.

### 2. Consecuencias

Las decisiones modifican realmente el mundo.

### 3. Persistencia

El mundo recuerda lo ocurrido.

### 4. Imparcialidad

El Master no protege al jugador artificialmente.

### 5. Coherencia

Los personajes, lugares y acontecimientos deben respetar el estado del mundo.

### 6. Descubrimiento

El jugador nunca debe tener acceso a toda la información.



## 4. Bucle principal de juego

El ciclo fundamental será:

```text
OBSERVAR
   ↓
DECIDIR
   ↓
DECLARAR ACCIÓN
   ↓
INTERPRETAR INTENCIÓN
   ↓
COMPROBAR REGLAS
   ↓
RESOLVER
   ↓
ACTUALIZAR MUNDO
   ↓
NARRAR RESULTADO
   ↓
OBSERVAR NUEVO ESTADO
```

**Ejemplo:**

**Jugador**

> “Entro en la taberna y pregunto discretamente si alguien ha visto a un hombre con una cicatriz en la cara.”

**El sistema:**

1. Identifica la intención: buscar información.

2. Comprueba dónde está el personaje.

3. Comprueba quién está presente.

4. Comprueba qué NPC conoce esa información.

5. Determina si requiere tirada.

6. Realiza la tirada.

7. Actualiza conocimiento del jugador.

8. Posiblemente modifica relaciones.

9. El Master narra el resultado.



## 5. Estados principales del juego

El jugador puede encontrarse en diferentes estados.

### Exploración

Viajar, investigar, buscar objetos, explorar lugares.

### Social

Conversaciones, negociación, engaño, intimidación, persuasión.

### Investigación

Buscar pistas, reconstruir acontecimientos, analizar objetos.

### Combate

Resolución estructurada por turnos.

### Viaje

Movimiento entre localizaciones y posibles encuentros.

### Descanso

Recuperación, fabricación, entrenamiento, relaciones.

### Mundo

Mientras el jugador actúa, determinadas cosas pueden suceder fuera de su vista.



## 6. Personaje

Cada jugador tendrá un personaje persistente.

### Atributos

Utilizaremos seis atributos:

|Atributo      |Representa                     |
|--------------|-------------------------------|
|**Fuerza**    |Potencia física                |
|**Agilidad**  |Velocidad y coordinación       |
|**Intelecto** |Conocimiento y razonamiento    |
|**Voluntad**  |Disciplina y resistencia mental|
|**Percepción**|Atención y sentidos            |
|**Presencia** |Personalidad e influencia      |

**Escala:** 1–20

El atributo genera un modificador:

```text
1–3     -4
4–5     -3
6–7     -2
8–9     -1
10–11    0
12–13   +1
14–15   +2
16–17   +3
18–19   +4
20      +5
```



## 7. Habilidades

Las habilidades representan entrenamiento.

### Físicas

- Atletismo
- Acrobacia
- Combate
- Sigilo
- Supervivencia

### Mentales

- Investigación
- Conocimiento
- Medicina
- Percepción
- Magia

### Sociales

- Persuasión
- Engaño
- Intimidación
- Negociación
- Liderazgo

No todas las acciones necesitan una habilidad.

El Game Engine determinará qué combinación es apropiada.



## 8. Sistema de resolución

La mecánica básica será:

```text
d20
+
modificador de atributo
+
bonificación de habilidad
+
modificadores circunstanciales
```

contra una dificultad.

### Dificultades

|DC|Dificultad |
|-:|-----------|
|8 |Muy fácil  |
|11|Fácil      |
|14|Normal     |
|17|Difícil    |
|20|Muy difícil|
|23|Extrema    |
|26|Excepcional|

**Ejemplo:**

> Intentar abrir una cerradura sencilla.

```text
d20 + Agilidad + Sigilo
vs DC 14
```



## 9. No todo requiere tirada

Esto es importante.

Si una acción es:

- trivial;
- segura;
- imposible;
- o determinada por el estado del mundo,

no se tira dado.

Ejemplo:

> “Abro la puerta de mi habitación.”

Si la puerta está abierta y tienes acceso:

éxito automático.

Pero:

> “Intento abrir la puerta cerrada con llave.”

→ tirada.

Esto evita convertir el juego en una sucesión absurda de tiradas.



## 10. Resultados de las acciones

No queremos únicamente:

éxito / fracaso.

Utilizaremos grados.

### Gran éxito

Supera ampliamente la dificultad.

### Éxito

Consigue el objetivo.

### Éxito con coste

Consigue el objetivo pero aparece una consecuencia.

### Fracaso

No consigue el objetivo.

### Fracaso grave

Además de fallar, genera una consecuencia importante.

Esto permite situaciones como:

> “Consigues abrir la cerradura, pero haces bastante ruido.”

En vez de:

> “Fallaste. No puedes abrirla.”



## 11. Combate

El combate será más estructurado que la exploración.

### Inicio

Se determina iniciativa mediante:

```text
d20 + Agilidad
```

Después se crea el orden de actuación.

### Turno

Cada personaje dispone inicialmente de:

**2 acciones por turno.**

Ejemplos:

- atacar;
- moverse;
- defenderse;
- usar objeto;
- lanzar magia;
- interactuar;
- ayudar;
- esconderse.

Esto permite decisiones sencillas sin copiar exactamente la estructura de D&D.



## 12. Salud

Cada personaje tendrá:

- Salud máxima
- Salud actual
- Heridas
- Estados

Llegar a 0 de salud no significa necesariamente muerte inmediata.

El personaje queda:

### Derribado

Puede realizar acciones limitadas para sobrevivir.

Otros personajes pueden:

- estabilizarlo;
- curarlo;
- transportarlo.

Si nadie interviene, existe riesgo de muerte.



## 13. Muerte

La muerte debe ser una posibilidad real.

Pero será configurable.

### Modo estándar

Muerte posible.

### Modo narrativo

El personaje puede sobrevivir mediante consecuencias graves.

### Modo hardcore

La muerte es permanente.

Para el MVP utilizaremos:

> **Modo estándar.**

El Master no puede salvar al jugador simplemente porque “sería mala historia”.



## 14. Magia

La magia será parte del sistema, pero no estará completamente definida en v0.1.

Conceptualmente:

```text
Hechizo
 ├── coste
 ├── alcance
 ├── duración
 ├── objetivo
 ├── efecto
 └── requisitos
```

El Game Engine determinará el efecto.

El Master describe cómo se manifiesta.



## 15. Inventario

Cada objeto será una entidad del mundo.

Ejemplo:

```text
ITEM
id: sword_iron_01
name: Espada de hierro
type: weapon
damage: 4
weight: 2.5
durability: 87
owner: player_01
location: inventory
```

El Master no puede decir:

> “Encuentras una espada.”

si el Game Engine no ha generado realmente ese objeto.



## 16. Economía

Existirá una economía básica:

- moneda;
- comerciantes;
- precios;
- inventario de tiendas;
- disponibilidad;
- recursos.

Los precios podrán variar según:

- región;
- demanda;
- escasez;
- acontecimientos;
- reputación;
- relaciones.

Esto será especialmente importante cuando implementemos el World Simulation.



## 17. NPC

Los NPC serán entidades persistentes.

Cada NPC tendrá:

```text
Identidad
Personalidad
Objetivos
Miedos
Relaciones
Conocimientos
Ubicación
Inventario
Estado
Facción
Memoria
```

Ejemplo:

Aron

- tabernero;
- 43 años;
- viudo;
- pertenece secretamente a una organización;
- odia a los soldados;
- debe dinero;
- conoce una información importante;
- no sabe quién es el jugador.

Si el jugador lo ayuda durante una partida, esa relación puede cambiar.



## 18. Memoria

Aquí tenemos uno de los elementos más importantes del producto.

El mundo tendrá memoria.

Pero no toda la memoria pertenece al jugador.

Existirán diferentes niveles.

### Memoria del jugador

Lo que el personaje sabe.

### Memoria del NPC

Lo que ese NPC ha visto o aprendido.

### Memoria del mundo

Lo que realmente ocurrió.

### Memoria histórica

Acontecimientos importantes del pasado.

Esto permite:

> El jugador cree que el comerciante murió.

Pero el mundo sabe que sobrevivió.



## 19. Conocimiento asimétrico

Este sistema será fundamental.

Ejemplo:

El jugador asesina a un noble durante la noche.

Tres NPC estaban presentes.

El resto del mundo no debería saber automáticamente quién lo hizo.

Los NPC pueden descubrirlo mediante:

- testigos;
- rumores;
- investigación;
- pistas;
- magia;
- vigilancia.

Esto crea auténtica jugabilidad emergente.



## 20. Facciones

Cada mundo tendrá diferentes facciones.

Ejemplo:

- Guardia Real
- Gremio de Comerciantes
- Culto de la Luna
- Ladrones
- Campesinos
- Nobles

Cada facción tendrá:

- reputación;
- objetivos;
- recursos;
- territorios;
- relaciones con otras facciones.

Las acciones del jugador pueden modificar ese equilibrio.



## 21. Sistema de reputación

La reputación no será simplemente:

```text
+10 bueno
-10 malo
```

Será contextual.

Ejemplo:

```text
Jugador
 ├── Guardia: -20
 ├── Ladrones: +35
 ├── Aldea Norte: +10
 └── Nobleza: -5
```

Incluso dentro de una facción podría existir reputación individual.



## 22. Mundo persistente

El mundo continuará avanzando.

Introducimos el concepto:

### World Tick

Un World Tick representa un avance temporal significativo.

Puede producir:

- cambios meteorológicos;
- movimiento de NPC;
- evolución de misiones;
- guerras;
- cambios políticos;
- comercio;
- ataques;
- desapariciones;
- nacimientos;
- muertes;
- cambios de precios.

Ejemplo:

El jugador ignora una petición:

> “Ayuda a proteger la aldea.”

Tres días después:

> La aldea ha sido atacada.

No porque el Master haya decidido improvisarlo, sino porque el sistema ejecutó una consecuencia previamente definida.



## 23. Misiones

Las misiones serán máquinas de estados.

Ejemplo:

```text
QUEST_STARTED
       ↓
FIND_MERCHANT
       ↓
DISCOVER_ATTACK
       ↓
CHOOSE_FACTION
       ↓
CONFRONT_BANDITS
       ↓
QUEST_COMPLETED
```

Pero pueden existir ramas.

```text
             ┌── ayudar comerciante
FIND MERCHANT
             └── ayudar bandidos
```

Cada decisión puede generar consecuencias diferentes.



## 24. El Master IA

El Master tendrá cinco funciones principales.

### 1. Narrador

Describe el mundo.

### 2. Director

Presenta situaciones y oportunidades.

### 3. Intérprete

Comprende las acciones expresadas libremente por el jugador.

### 4. Actor

Interpreta NPCs.

### 5. Adaptador

Ajusta la presentación a lo que está sucediendo.

Pero no controla directamente las reglas críticas.



## 25. Herramientas del Master

El modelo de OpenAI dispondrá de herramientas controladas por el sistema.

Por ejemplo:

```text
get_current_scene()
get_npc()
get_location()
check_action()
roll_check()
start_combat()
attack()
move_character()
use_item()
update_relationship()
advance_time()
get_quest_state()
```

La IA solicitará una operación.

El Game Engine la validará.

Solo después se ejecutará.



## 26. Flujo técnico de una acción

Ejemplo:

> “Ataco al guardia con mi espada.”

```text
PLAYER
  ↓
GAME API
  ↓
ORCHESTRATOR
  ↓
OPENAI API
  ↓
"attack(target=guard_01)"
  ↓
GAME ENGINE
  ↓
VALIDACIÓN
  ↓
DADOS
  ↓
CÁLCULO DE DAÑO
  ↓
ACTUALIZACIÓN STATE
  ↓
EVENTO
  ↓


## 27. Sistema de eventos

Todo acontecimiento importante genera un evento.

Ejemplos:
NPC_DIED
PLAYER_DIED
FACTION_REPUTATION_CHANGED
WORLD_TICK
```

Esto nos dará una enorme ventaja posteriormente:

podremos reconstruir qué ocurrió en el mundo.



## 28. Progresión

El personaje evolucionará mediante experiencias significativas.

No queremos:

> matar 500 ratas para subir de nivel.

El progreso estará asociado a:

- completar objetivos;
- descubrir lugares;
- superar desafíos;
- resolver conflictos;
- desarrollar habilidades;
- tomar decisiones importantes.

El sistema podrá otorgar:

Experiencia → niveles → mejoras.



## 29. Nivel

Propuesta inicial:

Nivel 1–20

Al subir:

- aumentan capacidades;
- desbloquean habilidades;
- mejoran atributos;
- aparecen nuevas posibilidades.

No será necesario que el jugador tenga que elegir una clase rígida.

En su lugar:

> **El personaje se construye mediante sus acciones.**

Si quieres convertirte en un guerrero, entrenas y combates.

Si quieres convertirte en diplomático, desarrollas habilidades sociales.



## 30. El mundo no gira alrededor del jugador

Esta es otra regla fundamental.

El mundo no debería decir:

> “El héroe ha llegado, por tanto todo espera.”

Si el jugador pasa 30 días en otro lugar:

el mundo sigue avanzando.

Una misión puede:

- expirar;
- cambiar;
- ser completada por otro;
- provocar una guerra;
- desaparecer;
- transformarse.

Esto diferencia al proyecto de un simple chatbot narrativo.



## 31. Información que recibe el jugador

El jugador no recibirá el estado interno completo.

Recibirá solamente:

```text
LO QUE VE
+
LO QUE OYE
+
LO QUE SABE
+
LO QUE PUEDE INFERIR
```

El resto permanece en el Game Engine.



## 32. Filosofía del Master

El Master seguirá esta regla:

> **“No estoy aquí para que ganes. Tampoco para que pierdas. Estoy aquí para representar honestamente el mundo.”**

Por tanto:

- no hará trampas para salvar al jugador;
- no matará al jugador artificialmente;
- no cambiará una tirada porque la historia lo necesite;
- no generará objetos arbitrarios;
- no hará que los NPC sepan cosas que no conocen;
- no ignorará consecuencias.



## 33. Experiencia de usuario

La pantalla principal podría dividirse en:

```text
┌───────────────────────────────────────┐
│               MAPA                    │
│                                       │
│          ● Jugador                    │
│       ● Aldea                         │
│                    ● Bosque           │
├───────────────────────────────────────┤
│                                       │
│ MASTER:                               │
│ El viento golpea las ventanas...      │
│                                       │
│ ¿Qué haces?                           │
│                                       │
├───────────────────────────────────────┤
│ [ Escribe tu acción...            ]   │
├───────────────────────────────────────┤
│ HP 24/24   Nivel 2   Oro: 37         │
└───────────────────────────────────────┘
```

La primera versión funcional puede ser únicamente:

chat + estado del personaje.

El mapa llegará después.




## 34. Límites de alcance

Para proteger ambas versiones:

- ❌ mundo generado infinitamente;
- ❌ cientos de clases;
- ❌ PvP;
- ❌ crafting complejo;
- ❌ árbol tecnológico;
- ❌ voz avanzada;
- ❌ generación de imágenes en cada turno;
- ❌ multijugador completo;
- ❌ editor de mundos;
- ❌ marketplace;
- ❌ cientos de NPCs.

Además, el MVP v0.2 no incluirá economía dinámica, magia compleja, múltiples facciones, simulación global del mundo ni mapa visual completo.

El MVP v0.1 podrá incorporar estos sistemas, pero solo después de validar el vertical slice del MVP v0.2.



## 35. Criterio de éxito del prototipo

La pregunta principal no será:

> “¿Funciona técnicamente?”

Será:

> **“Después de jugar dos horas, ¿quiero volver mañana para saber qué pasa?”**

Si la respuesta es sí, tenemos producto.



## 36. Arquitectura conceptual resultante

El GDD ya permite definir la arquitectura:

```text
                    ┌───────────────┐
                    │    CLIENT     │
                    │ Chat + UI     │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │   GAME API    │
                    └───────┬───────┘
                            │
                    ┌───────▼───────┐
                    │ ORCHESTRATOR  │
                    └───┬────────┬──┘
                        │        │
             ┌──────────▼──┐ ┌──▼──────────┐
             │ WORLD ENGINE│ │ OPENAI API  │
             │             │ │  AI MASTER  │
             └──────┬──────┘ └─────┬───────┘
                    │              │
             ┌──────▼──────────────▼──────┐
             │      WORLD STATE           │
             │                            │
             │ Characters                 │
             │ NPCs                       │
             │ Items                      │
             │ Quests                     │
             │ Factions                   │
             │ Relationships              │
             │ Events                     │
             │ Time                       │
             └────────────────────────────┘
```

**Regla arquitectónica**

OpenAI no es el Game Engine.

OpenAI interpreta, razona, narra y decide qué herramientas necesita.

El Game Engine decide qué es verdad.