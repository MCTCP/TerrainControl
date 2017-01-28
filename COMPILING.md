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
First of all, ForgeGradle and Eclipse are not the best friends. Sometimes, you'll
find out that things don't work anymore. When things do work however, you have a
nice test environment where you can quickly make changes to TerrainControl.

### Before you start
Normally, you'd just use the import option of Eclipse. However, ForgeGradle
requires us to run another command first. Run the command
`./gradlew setupDecompWorkspace` in the TerrainControl directory. This command
decompiles Minecraft, so that you can quickly jump to the Minecraft source code
from Eclipse.

Sometimes this command will fail if the Forge web servers are offline. In that
case, simply try running the command again.

### Importing the project
Make sure you have a recent version of Eclipse installed. Eclipse Neon should
work, and the slightly older Eclipse Mars.1 should work too. Older versions,
including the original Eclipse Mars release, will not work.

To import TerrainControl, use `File` -> `Import` -> `Gradle` -> `Gradle Project`
and import the TerrainControl directory.

**Please note:** As of Nov. 2016 there is a [bug] in the latest release of Buildship (the eclipse plugin that manages gradle) that may generate an `Unsupported method: HierarchicalEclipseProject.getIdentifier()` error when attempting to import the project into Eclipse. If this is a case, you need to manually update Buildship to a later version. To do so, click `Help` -> `Install New Software` and input this url: http://download.eclipse.org/buildship/updates/e46/milestones/2.x/.

### Running the Forge client or server
In the Forge version of TerrainControl two classes are included to help you run
the project from your IDE. Click `Run` -> `Edit Configurations...` -> `+`
-> `Application`. Use `forge` as the module (for the classpath) and use either
the `com.khorn.terraincontrol.forge.launch.TCLaunchForgeClient` or the
`com.khorn.terraincontrol.forge.launch.TCLaunchForgeServer` class and
press the Run button in the menu bar to run the Forge client or server.

### Formatting the source code
If you are familiar with the Eclipse code formatter, you can use our
[Eclipse formatting settings][] to automatically format the source code.

Don't worry too much about the code formatting; it is easy for us to correct
the code formatting when you submit a pull request. Focus on what the code
actually does!

### When you run into a problem with Eclipse
When you run into a problem that you cannot solve, there's always the option to
reset everything.

* Delete the projects in Eclipse (but don't delete the files on disk)
* Run `git clean -fdx` to remove all build files.
* Run `./gradlew setupDecompWorkspace` again.
* Import the project in Eclipse again.

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
[Eclipse formatting settings]: https://dl.dropboxusercontent.com/u/23288978/terraincontrol/TerrainControl.xml
[bug]: https://bugs.eclipse.org/bugs/show_bug.cgi?id=507423
