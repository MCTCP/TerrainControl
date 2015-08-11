# Compiling
Thanks for your interest in compiling TerrainControl.

## Before you start
First of all, you need to obtain a copy of the source of TerrainControl. Clone
it using Git, or download it as a [ZIP file][].

You also need to have the [JDK][JDK 7] installed. You will need version 7 or
newer.

## Creating JAR files
Run the command `./gradlew install` in the TerrainControl directory. If that
doesn't work, you probably need to make the file executable by running
`chmod +x gradlew`. You can then try the above command again.

You will be left with a Spigot plugin in `platforms/bukkit/build/distributions/`,
a Forge mod in `platforms/forge/build/distributions` and a file that runs
on both in `releases/build/distributions`.

## Eclipse
First, we need a decompiled version of Minecraft, this makes it much easier to
create mods. Run `./gradlew setupDecompWorkspace` in the TerrainControl
directory. You will need to rerun this command every time the Forge version
TerrainControl is built against changes.

Now there are two ways to open TerrainControl in Eclipse:

* Manually: run `./gradlew eclipse`. In Eclipse, choose `File` -> `Import`
  -> `General` -> `Existing Projects into Workspace` and select the
  TerrainControl directory. You need to rerun the `./gradlew eclipse` whenever
  any of our dependencies (Spigot, Forge, etc.) are updated.
* Using the Buildship plugin: first, make sure you have Eclipse Mars or newer
  installed. If not, download it from eclipse.org. Then make sure the Buildship
  plugin is installed. If not, choose `Help` -> `Eclipse Marketplace` and
  install Buildship. Then choose `File` -> `Import` -> `Gradle` ->
  `Gradle Project` and import the TerrainControl directory.

In the Forge version of TerrainControl two classes are included to help you run
the project from your IDE. Click `Run` -> `Edit Configurations...` -> `+`
-> `Application`. Use `forge` as the module (for the classpath) and use either
the 

`com.khorn.terraincontrol.forge.launch.TCLaunchForgeClient` or the
`com.khorn.terraincontrol.forge.launch.TCLaunchForgeServer` class and
press the Run button in the menu bar to run the Forge client or server.

## IntelliJ
First, we need a decompiled version of Minecraft, this makes it much easier to
create mods. Run `./gradlew setupDecompWorkspace` in the TerrainControl
directory. You will need to rerun this command every time the Forge version
TerrainControl is built against changes.

Now import the project as a Gradle project into IntelliJ.

To launch the Forge client version of TerrainControl from your IDE, click `Run`
-> `Edit Configurations...` -> `+` -> `Application`. Choose a name, use
`com.khorn.terraincontrol.forge.launch.TCLaunchForgeClient` as the main
class and select `forge` for the option `Use classpath of module`.

To launch the server version, repeat the above steps, but use the main class
`com.khorn.terraincontrol.forge.launch.TCLaunchForgeServer`.


[ZIP file]: https://github.com/MCTCP/TerrainControl/archive/master.zip
[JDK 7]: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html