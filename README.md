# FullOffhand (Fabric + Geyser) — experimental starter

Target: Minecraft Java 26.3, Fabric server, Geyser-Fabric.

This project is a **server-side Fabric mod that uses the Geyser API and Cumulus forms**. It does not need a separate Geyser extension JAR: Geyser's official API is designed to be consumed by Fabric mods, and it can send Bedrock forms to Bedrock connections.

## What this build implements

- `/offhand` and `/offhandui` open a Bedrock form for Geyser-connected Bedrock players.
- Buttons 1–9 move the selected hotbar stack into the offhand; the previous offhand stack is moved into that hotbar slot.
- A final button swaps main hand and offhand.
- Java players are not given the menu and retain vanilla gameplay.

## What it does NOT implement yet

This is a first functional slice, **not the complete Java-equivalent offhand system** requested.

- Bedrock forms are modal forms, not a persistent HUD overlay or an inventory drag-and-drop replacement.
- The form can select/equip an offhand item, but does not inject a new Bedrock attack input.
- Offhand block placement, food use, shield timing/blocking, bow/crossbow use, trident use, map use, and arbitrary item interactions are not yet implemented.
- Java's normal attack input is main-hand-only; dual-wielding attacks is not a vanilla Java mechanic either.
- A form action cannot reliably know the Bedrock player's current targeted block/entity. Those actions need a separate, validated interaction pipeline (and in some cases Geyser packet translation hooks or client resource-pack/UI work).

Do not advertise this starter as full dual-wielding.

## Requirements

- Java 25 JDK
- Minecraft 26.3 Fabric server
- Fabric Loader 0.20.1 or newer
- Fabric API for 26.3
- Geyser-Fabric for Java 26.3

Geyser-Fabric is a required mod dependency. This mod uses Geyser's API at compile time and expects Geyser to provide the API and Cumulus classes at runtime.

## Build

1. Install a Java 25 JDK and make sure `java -version` reports 25.
2. Open this folder in IntelliJ IDEA, or use a terminal here.
3. Run:

   Linux/macOS:
   ```bash
   ./gradlew build
   ```

   Windows:
   ```bat
   gradlew.bat build
   ```

   This archive does not include the Gradle wrapper. If you don't have one, install Gradle 9.6+ and run:
   ```bash
   gradle build
   ```

4. The mod JAR should be in:
   `build/libs/fulloffhand-0.1.0.jar`

## Install

1. Stop the server and back up the world.
2. Put `fulloffhand-0.1.0.jar` into the server `mods/` directory.
3. Keep Geyser-Fabric and Fabric API in `mods/`.
4. Start the server and inspect the log for dependency or API errors.
5. Join from Bedrock through Geyser and run `/offhand`.

No client mod is required.

## Version alignment note

The Geyser API dependency is set to `2.11.2-SNAPSHOT`, matching the current Geyser API getting-started documentation at the time this project was prepared. If your installed Geyser build exposes a different API version, align `geyser_api_version` in `gradle.properties` with that build. Geyser API is `compileOnly`; do not shade or bundle Geyser classes into this mod.

## Safety and behaviour

- The server chooses the item by hotbar index; it never accepts a client-supplied item ID or arbitrary item stack.
- The callback re-fetches the online player by UUID before changing inventory.
- Test first on a copy of your world. This is source code for an experimental integration, not a precompiled, verified production release.

## Next development milestones for the full feature set

1. Add Bedrock-specific action routing and cooldown/rate limits.
2. Add offhand `use` dispatch for supported item categories using the server's normal item interaction methods.
3. Add block/entity target validation and ray tracing server-side.
4. Add shield/block-use state handling and correct animation/sound/inventory sync.
5. Add a Bedrock-side UI affordance for selecting the offhand action without disrupting normal controls.
6. Test every vanilla item class and common modded item class against Bedrock and Java clients.

## References

- Geyser API setup: https://geysermc.org/wiki/geyser/getting-started-with-the-api/
- Geyser forms / Cumulus: https://geysermc.org/wiki/geyser/forms/
- Geyser Fabric setup: https://geysermc.org/wiki/geyser/setup/
- Fabric 26.3: https://www.fabricmc.net/2026/09/15/263.html
- Fabric networking: https://docs.fabricmc.net/develop/networking


## Build with GitHub Actions (no Gradle on the game host)

1. Create a GitHub repository (for example `FullOffhand`).
2. Upload the contents of this project to the repository root. Make sure `.github/workflows/build.yml` is included.
3. Open the repository's **Actions** tab.
4. Select **Build FullOffhand** and click **Run workflow**, or push a commit to `main`.
5. Wait for the workflow to finish. A green check means the build command completed.
6. Open the completed workflow run. Under **Artifacts**, download `FullOffhand-Fabric-26.3`.
7. Extract the artifact ZIP and upload the mod JAR from it to the server's `mods/` folder.

The workflow uses GitHub-hosted Ubuntu runners, Temurin Java 25, and Gradle 9.6. It does not install anything on your Minecraft host.

If the workflow fails, open the failed **Build mod** step and copy the first compiler/dependency error. A failed workflow does not produce an installable mod.
