# Compiling
Thanks for your interest in compiling OpenTerrainGenerator.

## Before you start
First of all, you need to obtain a copy of the source of OpenTerrainGenerator. Clone
it using Git, or download it as a [ZIP file][].

You also need to have the [JDK][JDK 7] installed. You will need version 7 or
newer.

## Setting up the project

Run:
1. gradle setupDecompWorkspace
2. gradle eclipse
3. gradle build

- For Forge, copy META-INF.jar to run/mods, this will enable OTG-Core, the core-mod part of OTG.
- If you're using an OTG jar in-dev, be sure to copy the accesstransformer rules from \platforms\forge\src\main\resources\META-INF\openterraingenerator_at.cfg and run gradle setupDecompWorkspace.

## Creating JAR files

Run:
1. gradle build
2. gradle createreleasejar

You will be left with a Spigot plugin in `platforms/bukkit/build/distributions/`,
a Forge mod in `platforms/forge/build/distributions` and a file that runs
on both in `releases/build/distributions`.

Some commands will fail if the Forge web servers are offline. In that
case, simply try running the command again.

### Importing the project
Import existing project -> Point to OTG root folder.

### Running the Forge client or server
Create run/debug configurations for Forge and Spigot as for any mod/plugin.