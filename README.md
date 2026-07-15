# NTM: Cleanroom Core

Core mod for Minecraft 1.12.2 (Forge, GTNH RetroFuturaGradle buildscript) that provides a small,
stable hook point for addons to register their own custom [Nuclear Tech Mod (hbm)](https://www.curseforge.com/minecraft/mc-mods/hbms-nuclear-tech-mod)
machines without each addon needing to reinvent tile entity registration and gui-id bookkeeping.

## Building

```
./gradlew build
```

Requires a JDK 25 toolchain (auto-provisioned by Gradle via the foojay resolver) since
`enableModernJavaSyntax` is on. Run the game with `./gradlew runClient`.

## Writing an addon

1. Depend on this mod at runtime and compile time, either via the published `api` artifact
   (`ntmcleanroom-<version>-api.jar`) or by depending on the full mod jar.
2. Declare the dependency in your `@Mod` annotation:
   ```java
   @Mod(modid = "myaddon", dependencies = "required-after:ntmcleanroom;required-after:hbm")
   ```
3. Register your machine(s), typically from your own `preInit` or `init`:
   ```java
   CleanroomMachineRegistry.registerMachine(
       new MachineDefinition("myaddon", "myaddon:my_machine", MyMachineTileEntity.class, myGuiProvider));
   ```
   This registers the tile entity and, if you passed an `IMachineGuiProvider`, wires it into the
   core's shared gui handler - no need to write your own `IGuiHandler` or manage a gui-id range.
4. Open the gui from your block/item code:
   ```java
   int guiId = CleanroomMachineRegistry.getGuiId("myaddon:my_machine");
   player.openGui(NTMCleanroomCore.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
   ```

See `com.ntmcleanroom.api.machine` for the full API surface (`MachineDefinition`,
`IMachineGuiProvider`, `CleanroomMachineRegistry`).

## Restored content

- **Schrabidium Transmutator** (`com.ntmcleanroom.content.transmutator`) - the machine and its
  input/output recipe table (`SchrabidiumTransmutatorRecipes`, Uranium -> Schraranium), ported from
  an older NTM-CE build since it was removed from the current upstream dependency. Registered as a
  regular addon machine through `CleanroomMachineRegistry`/`TransmutatorModule`, with its own crafting
  recipe added to `AssemblyMachineRecipes`. Registry names live under the `ntmcleanroom` domain
  (`ntmcleanroom:machine_schrabidium_transmutator`), since this mod - not hbm - now owns the block.
