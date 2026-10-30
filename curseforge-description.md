# TarkovCraft Self-Interaction Crash Fix

A small fix for [TarkovCraft: Core](https://www.curseforge.com/minecraft/mc-mods/tarkovcraft-core) on Minecraft 1.21.1 (NeoForge).

## What it fixes

TarkovCraft: Core crashes with **"Player and target cannot be the same entity"** whenever your own character becomes the target of its entity interaction system. This happens as soon as the camera is no longer inside your head, for example when you look through an **Exposure** camera on a tripod and see yourself.

- **Client crash:** TarkovCraft's interaction hint checks the entity under your crosshair every tick. If that entity is you, the game crashes.
- **Server crash:** Sneaking and right-clicking your own character from such a view throws an exception on the server.
- **Server hardening:** Interaction requests a client sends for itself are ignored instead of throwing.

With this mod your own character simply counts as "nothing to interact with". Interactions with other players and mobs are unchanged.

## Requirements

- Minecraft **1.21.1**, **NeoForge**
- **TarkovCraft: Core** 2.11.2 or newer

Install it on **clients and the server**. Each side works on its own: clients need it for the client crash, the server for the server-side fixes.
