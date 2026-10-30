# TarkovCraft Self-Interaction Crash Fix - Specification

## Observed crash

Client crash on the Teachings of Zeta server (2026-10-30) while using an Exposure camera on a tripod:

```
java.lang.IllegalArgumentException: Player and target cannot be the same entity
    at tnt.tarkovcraft.core.api.EntityInteraction$Context.<init>(EntityInteraction.java:51)
    at tnt.tarkovcraft.core.client.hint.EntityInteractHint.tick(EntityInteractHint.java:24)
    at tnt.tarkovcraft.core.client.hint.OnScreenHintManager.tickHints(OnScreenHintManager.java:62)
    ...
```

TarkovCraft: Core 2.11.2+1.21.1 (latest 1.21.1 release as of 2026-10-30, Modrinth `6JVxqmLW`,
CurseForge project 1267847). All Rights Reserved, so this is a mixin patch, not a fork.

## Root cause (verified via javap against the deployed jar)

`EntityInteraction.Context(Player, LivingEntity)` compares player and target by identity and throws
`IllegalArgumentException` when they are the same object. It is constructed in exactly four places,
none of which checks for that first:

| # | Call site | Side | Where the target comes from |
|---|-----------|------|-----------------------------|
| 1 | `client.hint.EntityInteractHint.tick(Minecraft)` | client, every tick | `Minecraft.crosshairPickEntity` |
| 2 | `common.TarkovCraftCoreEventHandler.onEntityInteract(EntityInteractSpecific)` | client + server | sneak + right-click target |
| 3 | `client.screen.EntityInteractScreen.init()` | client | entity passed to `TarkovCraftCoreClient.openEntityInteractionScreen` (only caller: #2) |
| 4 | `network.message.C2S_RequestInteractionState.handleMessage(IPayloadContext)` | server | entity id sent by the client |

`crosshairPickEntity` is picked from the camera entity, not the player. With a detached camera
(Exposure tripod, any freecam-like view) the local player's own body can be under the crosshair, so
#1 throws on every client tick → client crash. The same view lets the player sneak-right-click
themselves; vanilla's interact packet handling accepts a self target, so #2 throws on the server too
(reproduced by the GameTests). #4 lets any client make the server handler throw by sending its own
entity id.

The `hintDisplayMode` client config does not help: `OnScreenHintManager.tick` ticks every active
hint regardless of the display mode; the mode only affects rendering.

## Fix

One rule (`SelfInteraction.isSelf`, same identity check as the Context constructor): wherever
TarkovCraft is about to build a Context, the player themselves counts as "no target", and
TarkovCraft's own "nothing to interact with" path runs.

| # | Mixin | Side | Change |
|---|-------|------|--------|
| 1 | `EntityInteractHintMixin` | client | `@Redirect` of the `crosshairPickEntity` read: self → `null`, so the original's `instanceof LivingEntity` early return hides the hint |
| 2 | `TarkovCraftCoreEventHandlerMixin` | both | `@Inject` HEAD, cancel the handler for a self target; the event itself stays uncanceled, so vanilla continues as for any entity without TarkovCraft interactions |
| 3 | `TarkovCraftCoreClientMixin` | client | `@Inject` HEAD of `openEntityInteractionScreen`: never open the screen for the local player (defence in depth, #2 already prevents it) |
| 4 | `RequestInteractionStateMixin` | both (handler only runs on the server) | `@Inject` HEAD of `handleMessage`: drop a request whose entity id resolves to the sender, like the handler already drops ids that don't resolve to a living entity |

Interactions with any other entity are untouched. Every injector uses `require = 1` (mixin config
`defaultRequire`), so a TarkovCraft update that moves these call sites fails loudly at startup
instead of silently leaving the crash in place - re-verify against the new jar then.

Installing it on only one side is fine: the mod registers no network payloads. Clients need it for
#1/#3, the server for #2/#4.

## Tests

- `./gradlew runGameTestServer` (CI on every push and before every release) runs against the real
  TarkovCraft 2.11.2 jar from Modrinth's maven:
  - `sneakInteractWithSelfDoesNotThrow` (#2), `interactionRequestForSelfIsDropped` (#4): both fail
    with the original exception when the mixins are removed (verified 2026-10-30).
  - `sneakInteractWithOtherEntityStillHandled`: TarkovCraft's normal path still runs for other entities.
- Client paths #1/#3 need a client. Manual test: place an Exposure camera on a tripod, look through it
  at your own character (and sneak + right-click it) - no crash, no interaction hint/screen for
  yourself; looking at another mob through it still shows its hint.
