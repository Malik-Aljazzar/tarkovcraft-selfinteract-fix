# Changelog

All notable changes to TarkovCraft Self-Interaction Crash Fix are documented in this file.

## [1.0.0]

### Fixes
- Fixed the game crashing with "Player and target cannot be the same entity" when your own character ends up under the crosshair, e.g. while looking through an Exposure camera on a tripod. TarkovCraft: Core's interaction hint checks the entity under the crosshair every tick and crashed as soon as that entity was you.
- Fixed the same crash on the server when a player sneaks and right-clicks their own character (possible whenever the camera is detached from the player, as with the tripod).
- A client can no longer make the server throw by requesting a TarkovCraft interaction with itself; such requests are ignored.
- Interactions with other players and mobs work exactly as before.
