# MVP v0.4: cliente Android del vertical slice

El vertical slice de [MVP v0.2](MVP%20v0.2.md) es jugable de extremo a extremo, y [MVP v0.3](MVP%20v0.3.md) cerró la mayor parte del alcance técnico del TDD sobre el motor (`Project GM`). Hasta ahora el único "cliente" real era la suite E2E (`Project GM automatics`, Cucumber/REST Assured) y herramientas HTTP manuales (Postman/curl/Invoke-RestMethod) — no existía ninguna interfaz de usuario. Esta versión no añade alcance nuevo al motor: introduce un primer cliente propio, una app Android (`Project GM Android/`), que consume la API HTTP ya existente de `Project GM` sin modificarla.

Ver [`Project GM/docs-agent/api-contracts.md`](Project%20GM/docs-agent/api-contracts.md) para el contrato exacto de cada endpoint referenciado abajo.

**Estado (2026-09-25)**: proyecto Android andamiado y compilando (`Project GM Android/`, Kotlin/Jetpack Compose/Material 3, capa de red con Retrofit + kotlinx.serialization); primera vertical slice mínima de sesiones (listar/crear) implementada pero todavía sin ejecutar contra el backend real en emulador/dispositivo. Autenticación (flavor `cloud`) verificada de extremo a extremo en dispositivo físico real (login, registro y refresh). El resto de pantallas (narración, NPCs, inventario, misiones, combate) sigue sin empezar.

## Alcance

|Elemento                     |Estado antes de v0.4                              |Objetivo v0.4                                                                                   |Referencia API|
|------------------------------|---------------------------------------------------|--------------------------------------------------------------------------------------------------|---|
|Proyecto Android             |✅ Hecho (2026-09-24) — módulo `Project GM Android/` (Gradle Kotlin DSL, AGP con built-in Kotlin, Jetpack Compose, Material 3, arquitectura MVVM); sincroniza y compila (`Make Project` verificado por el usuario) |Nuevo módulo `Project GM Android/` (Kotlin, Jetpack Compose, Material 3, arquitectura MVVM)       |—|
|Autenticación                |✅ Hecho (2026-09-25) — flavors `local`/`cloud`; `local` sigue igual (`DevIdentityInterceptor` con `X-Dev-Player-Id` opcional); `cloud` implementa login/registro 100% nativos (formularios `LoginScreen`/`RegisterScreen`, sin navegador/Custom Tabs) contra un grant type OAuth2 custom `password` del servicio `Project GM Auth` (pivote deliberado: la versión inicial usaba Authorization Code+PKCE vía AppAuth/Custom Tabs, abandonada porque el navegador retenía su propia cookie de sesión y esta versión de Spring AS no soporta `prompt=login` para forzar relogin). `AuthSessionManager`/`CloudAuthSessionManager` (OkHttp plano, token en `EncryptedSharedPreferences`, refresh automático vía `AuthAuthenticator`). Verificado end-to-end en dispositivo físico real (login, registro y refresh, ambos flavors) |La app obtiene/adjunta el token (o el header `X-Dev-Player-Id` en desarrollo) en cada petición    |Nota de autenticación en cabecera del contrato|
|Gestión de partidas          |🔶 Parcial (2026-09-24) — `SessionRepository`/`SessionListViewModel`/`SessionListScreen` implementados (listar + crear con payload por defecto), compilan; sin ejecutar todavía contra el backend real en emulador/dispositivo |Pantalla de lista de partidas del jugador + creación de partida nueva                             |Sessions|
|Narración y acción libre     |Endpoint existe, sin cliente                       |Pantalla principal tipo chat: texto libre del jugador + narración de la IA, con historial visible de turnos |Actions|
|NPCs y relaciones            |Endpoint existe, sin cliente                       |Pantalla de NPCs de la localización actual con su valor de relación                                |NPCs|
|Inventario                   |Endpoint existe, sin cliente                       |Pantalla de inventario: objetos portados y objetos en el lugar, con acción de recoger              |Items|
|Misiones                     |Endpoint existe, sin cliente                       |Pantalla de misiones visibles, con inicio y avance por rama (`choiceKey`) o texto libre            |Quests|
|Combate                      |Endpoint existe, sin cliente                       |Pantalla de combate: orden de turnos, participantes, vida, atacar por selección o texto libre       |Combat|
|Manejo de errores            |🔶 Parcial (2026-09-24) — `ApiError`/`ApiException`/`toApiExceptionOrNull()` parsean el shape uniforme del backend y `SessionListViewModel` lo traduce a mensaje de usuario; solo cubre el flujo de sesiones, falta el resto de pantallas y el caso 401/403 (shape distinto, sin tratar) |La app traduce cada código (`ACTION_NOT_ALLOWED`, `STALE_SESSION_VERSION`, `COMBAT_NOT_ALLOWED`, `QUEST_TRANSITION_NOT_ALLOWED`, `ITEM_NOT_ALLOWED`, `OPENAI_UNAVAILABLE`, etc.) a un mensaje entendible, sin exponer el `code` crudo al jugador |Errors|
|Concurrencia optimista       |Backend usa `expectedVersion` por sesión           |La app guarda la versión local, la envía en cada acción y, ante 409, refresca la sesión y reintenta |Actions/Errors|
|Idempotencia                 |Backend acepta `idempotencyKey`                    |La app genera una clave por intento de envío y la reutiliza en reintentos, para no duplicar acciones |Actions|

## Objetivos funcionales

Al terminar esta versión, un jugador debe poder, **usando únicamente el móvil**:

- crear una partida nueva o continuar una existente;
- leer la narración y responder con texto libre desde una única pantalla principal, sin formularios de "elige actionType";
- consultar los NPCs de su localización actual y su relación con cada uno;
- recoger objetos del lugar y ver su inventario;
- iniciar y avanzar misiones, incluyendo ramas de decisión;
- entrar en combate por turnos y resolver ataques;
- cerrar la app y recuperar exactamente el mismo estado de partida al volver a abrirla;
- recibir un mensaje claro (no un código de error crudo) ante cualquier fallo de red, versión desactualizada o acción no permitida.

## Exclusiones

Esta versión no incluirá:

- ningún cambio de alcance o contrato en el backend `Project GM` (solo consumo de la API existente);
- mapa visual o representación gráfica del mundo (arte propio, sprites, tiles);
- soporte iOS o multiplataforma (Android únicamente);
- modo offline o sincronización diferida;
- notificaciones push;
- creación de personaje avanzada (nombre/atributos iniciales fijos o mínimos, sin editor visual);
- voz (entrada o salida por audio) o generación de imágenes;
- animaciones de combate elaboradas (indicadores simples de daño/turno son suficientes);
- localización a otros idiomas además del español;
- publicación en Google Play (solo APK/instalación manual para pruebas).

## Criterio de validación

MVP v0.4 estará completo cuando el recorrido equivalente al de `recorrido_completo.feature` (crear partida → acción libre → acción social con NPC → combate → misión con rama → cerrar y reabrir la partida con las consecuencias persistidas) pueda completarse íntegramente desde la interfaz Android, sin recurrir a Postman/curl/Invoke-RestMethod ni a la suite `Project GM automatics`.
