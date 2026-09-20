# Project GM

RPG narrativo conversacional de fantasía para web. La IA actúa como Master (narra), pero el Game Engine es la única autoridad sobre el estado del mundo:

> La IA interpreta y narra. El Game Engine determina qué es verdad.

El proyecto empieza como experiencia individual, dejando margen para una futura evolución multijugador.

## Documentos de diseño

- [GDD v0.1.md](GDD%20v0.1.md): visión, pilares de diseño, reglas, mundo, NPCs, combate, memoria, eventos y arquitectura conceptual.
- [MVP v0.1.md](MVP%20v0.1.md): alcance completo previsto (campaña larga, múltiples regiones/localizaciones/NPCs/facciones).
- [MVP v0.2.md](MVP%20v0.2.md): vertical slice actual (1 escenario, 3 localizaciones, 5 NPCs, 1 facción, 3 misiones ramificadas, combate básico).
- [MVP v0.3.md](MVP%20v0.3.md): cierre de los puntos del TDD pendientes (seguridad, observabilidad, inventario, reglas de muerte, etc.), sin contenido narrativo nuevo.
- [TDD MVP v0.2.md](TDD%20MVP%20v0.2.md): especificación técnica del vertical slice.
- [ARCHITECTURE_CONTRACT.md](ARCHITECTURE_CONTRACT.md): contrato arquitectónico (límites entre dominio, IA y capas).
- [CONTEXTO_PROYECTO.md](CONTEXTO_PROYECTO.md): estado de implementación detallado y próximo paso, mantenido al día durante el desarrollo.

## Stack técnico

- Java 21, Spring Boot 4.1.1, Maven.
- MySQL + Spring Data JPA/Hibernate, Flyway para migraciones.
- MapStruct para mapeo DTO ↔ comando/entidad, Lombok.
- Monolito modular (sin microservicios ni Docker durante el MVP).
- IA: OpenAI Responses API con Structured Outputs, aislada detrás de la interfaz `MasterAdapter` (la IA solo interpreta texto y narra; nunca decide tiradas, reglas ni estado).

## Estructura del repositorio

- `Project GM/`: proyecto Spring Boot principal (API + dominio + persistencia).
- `Project GM automatics/`: proyecto Maven independiente con tests E2E/API (Cucumber + JUnit 5 + REST Assured).

## Estado actual

El vertical slice de `MVP v0.2` está funcionalmente completo: sesión/personaje, eventos e idempotencia, reglas de acción por localización, NPCs/relaciones, misiones ramificadas, combate por turnos, narración vía OpenAI (o stub) e interpretación de texto libre para acciones, combate y misiones.

Pendiente: seguridad JWT, observabilidad y un E2E completo del vertical slice.

Para el detalle punto por punto de lo implementado, ver [CONTEXTO_PROYECTO.md](CONTEXTO_PROYECTO.md).

## Ejecución local

Requiere MySQL disponible localmente (o externo). No se usa Docker.

```powershell
cd "Project GM"
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/project_gm` (perfil `local` por defecto).

El perfil `local` activa OpenAI real por defecto y requiere las variables de entorno `OPENAI_API_KEY` y `OPENAI_MODEL`; para usar el adaptador stub (sin red), fijar `OPENAI_ENABLED=false`.

## Tests

- Unitarios/integración del proyecto principal: `mvn test` dentro de `Project GM/`.
- E2E (requiere la API levantada en `localhost:8080/project_gm`): `mvn test` dentro de `Project GM automatics/`.

La compilación y ejecución de tests las realiza el usuario manualmente.