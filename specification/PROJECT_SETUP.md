# TarkovCraft Self-Interaction Crash Fix – Projekt-Einrichtung (CurseForge)

Texte in Codeblöcken sind zum Kopieren (öffentlich, daher Englisch).

## Eckdaten

| Feld | Wert |
|---|---|
| Name | `TarkovCraft Self-Interaction Crash Fix` |
| Mod-ID | `tarkov_selfinteract_fix` |
| Version | `1.0.0` |
| Minecraft / Loader | 1.21.1 / NeoForge |
| Seite | **Client und Server** (jede Seite wirkt für sich) |
| Abhängigkeit | TarkovCraft: Core ≥ 2.11.2 (CurseForge-Slug `tarkovcraft-core`, ID 1267847) |
| Lizenz | MIT |
| Jar | `build/libs/tarkov_selfinteract_fix-1.0.0.jar` |
| Logo | `icon.png` (512×512 Pixel-Art: Fadenkreuz auf dem eigenen Kopf, grüner Haken; Generator `modpack_pipeline/menu/gen_mod_icons.py`) |
| GitHub | `Malik-Aljazzar/tarkovcraft-selfinteract-fix` (privat, CI grün) |

## 1. CurseForge-Projekt anlegen

curseforge.com → *Create a Project* → **Minecraft** → **Mods**

| Formularfeld | Eingabe |
|---|---|
| Project name | `TarkovCraft Self-Interaction Crash Fix` |
| Summary | siehe unten |
| Description | Inhalt von `curseforge-description.md` (Markdown) |
| Logo / Avatar | `icon.png` |
| Main category | **Bug Fixes** |
| License | **MIT License** |

Summary:
```
Fixes TarkovCraft: Core crashing with "Player and target cannot be the same entity", e.g. when looking through an Exposure tripod camera.
```

Danach: „third-party distribution“ an lassen und die **Projekt-ID** in `gradle.properties` eintragen:
```
curseforge_project_id=<ID>
```

## 2. GitHub-Secret

Repo und CI/CD (`build.yml`, `release.yml` mit GameTests) existieren schon. **Erledigt
(2026-10-31):** Secret `CURSEFORGE_API_KEY` gesetzt (eigener Token, lokal in
`~/.zeta/curseforge_token_tarkov_fix`).

## 3. Veröffentlichen

```powershell
git commit -am "CurseForge project id"
git push
git tag v1.0.0
git push origin v1.0.0
```
`release.yml` prüft Tag, Changelog, Projekt-ID und Secret, baut, führt die GameTests gegen das echte
TarkovCraft Core aus und lädt hoch (Client + Server, „requires TarkovCraft: Core“), plus GitHub-Release.

## 4. Pack

Auf dem Server läuft der Fix schon (Pipeline-Seite `server`, nicht im Client-Pack). Sobald er auf
CurseForge freigegeben ist, kann er per CurseForge-App in die Instanz und mit Seite `both` ins Pack –
dann ist auch der Client-Absturz (Exposure-Stativ) für alle Spieler behoben:
```
python -m zeta_pack decide side tarkov_selfinteract_fix both --reason "on CurseForge now; fixes the client crash too"
```
