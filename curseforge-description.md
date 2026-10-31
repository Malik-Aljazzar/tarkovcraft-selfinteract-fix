# TarkovCraft Self-Interaction Crash Fix

Fixes [TarkovCraft: Core](https://www.curseforge.com/minecraft/mc-mods/tarkovcraft-core) crashing with "Player and target cannot be the same entity".

## What it fixes

The crash happens when your own character becomes the target of TarkovCraft's interaction system, e.g. when you look through an Exposure camera on a tripod and see yourself.

- Client: the interaction hint no longer crashes when you are under your own crosshair.
- Server: sneak + right-click on your own character no longer throws.
- Server: interaction requests a client sends for itself are ignored.

Interactions with other players and mobs are unchanged.

## Requirements

- Minecraft 1.21.1, NeoForge
- TarkovCraft: Core 2.11.2 or newer

Install on clients and the server. Each side works on its own.

Made for the [Teachings of Zeta](https://www.curseforge.com/minecraft/modpacks/teachings-of-zeta) modpack. MIT license. Unofficial fix, not affiliated with TarkovCraft.
