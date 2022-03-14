[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![Discord][discord-shield]][discord-url]

<br />
<div align="center">
  <a href="https://github.com/PG85/OpenTerrainGenerator">
    <img src="logo.png" alt="Logo" width="291" height="100">
  </a>

<h3 align="center">OpenTerrainGenerator by Team OTG</h3>

  <p align="center">
    Minecraft's Leading Data-Driven Worldgen Engine
    <br />
    <a href="https://openterraingen.fandom.com"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://www.openterraingenerator.org/presets.html">See Our Presets</a>
    ·
    <a href="https://github.com/othneildrew/Best-README-Template/issues">Report Bugs</a>
    ·
    <a href="https://discord.gg/YY2NECCBYN">Join our Discord</a>
  </p>
</div>

##  OpenTerrainGenerator by Team OTG

OpenTerrainGenerator for MC 1.16.x is under development, alpha builds are available in the dev-releases channel of the OTG Discord.

OTG 1.16.x 0.1.0 will be the first public beta release for 1.16.x, available via CurseForge. Once 0.1.0 is released, the 1.16.4 branch will be promoted to default/main. See the milestones for open issues/progress.

### Team OTG
* <a href="https://github.com/PG85">PG85</a>
* MCPitman
* <a href="https://github.com/authvin">Authvin</a>
* <a href="https://github.com/Coll1234567">Josh</a>
* <a href="https://github.com/SuperCoder7979">SuperCoder79</a>
* <a href="https://github.com/SXRWahrheit">Wahrheit</a>
* <a href="https://infotoast.org">Frank from Info Toast</a>

We're always looking for people to contribute or collaborate with. For OTG 1.16, we've completely cleaned up and overhauled the codebase with the aim of making things more modular, so developers and collaborators can implement new settings and modes such as terrain generation noise. If you'd like to contribute, collaborate or become part of Team OTG, join us on the OTG Discord!

## Installation / building

- As with Forge mods, download the repo, then run /gradlew genEclipseRuns, then /gradlew eclipse (for Eclipse IDE).
- To create a release jar in the build folder, update the build version in build.gradle, then run /gradlew createReleaseJar.
- If you're having problems, make a sacrifice to the gradle gods and/or run /gradlew clean and /gradlew --refresh-dependencies.

### IntelliJ Building Instructions

- Instead of running /gradlew genEclipseRuns, just run /gradlew genIntellijRuns.
- Then, all you have to do is Open IntelliJ and import the project folder, make sure you Trust the gradle project, and IntelliJ will do the rest :)
- Follow the same instructions that you do for Eclipse if you want to build -- do note that IntelliJ has a gradle GUI that you can use once you've imported the project. (Should be on the right of the code.)

## Dev Builds
There are dev builds available for 1.16.5 [here](https://github.com/PG85/OpenTerrainGenerator/actions?query=branch%3A1.16.4).

Dev builds for 1.17.1 are available [here](https://github.com/PG85/OpenTerrainGenerator/actions?query=branch%3A1.17.1).

Dev builds for 1.18.1 are available [here](https://github.com/PG85/OpenTerrainGenerator/actions?query=branch%3A1.18.1).

You will **NOT** get official support for dev builds, and they may break. Please use the mod jars available to you on CurseForge and the [OTG Discord](https://discord.gg/vTqe4zr5Hc).

## Links
* [CurseForge](https://minecraft.curseforge.com/projects/open-terrain-generator)
* [Wiki](http://openterraingen.wikia.com/wiki/Open_Terrain_Generator_Wiki)
* [Discord](https://discord.com/invite/UXzdVTH)
* [Installation](https://openterraingen.fandom.com/wiki/Installing_OTG) for Spigot and Forge

## Original developers

OpenTerrainGenerator is a fork of Terrain Control, which is the successor to <a href="http://www.minecraftforum.net/topic/313991-phoenixterrainmod/">PhoenixTerrainMod</a>, which was based on <a href="http://www.minecraftforum.net/topic/71565-biomemod/">BiomeTerrainMod</a>.

* Buycruss       - BiomeTerrainMod
* R-T-B          - PhoenixTerrainMod
* <a href="http://dev.bukkit.org/profiles/Khoorn/">Khoorn - TerrainControl</a> (known as <a href="https://github.com/Wickth">Wickth</a> on GitHub)
* <a href="https://github.com/oloflarsson">Oloflarsson/Cayorion - TerrainControl</a>
* <a href="https://github.com/Timethor">Timethor - TerrainControl</a>
* <a href="https://github.com/rutgerkok">Rutgerkok - TerrainControl</a>
* <a href="https://github.com/bloodmc">BloodMC - TerrainControl</a>

[contributors-shield]: https://img.shields.io/github/contributors/PG85/OpenTerrainGenerator.svg?style=for-the-badge
[contributors-url]: https://github.com/PG85/OpenTerrainGenerator/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/PG85/OpenTerrainGenerator.svg?style=for-the-badge
[forks-url]: https://github.com/PG85/OpenTerrainGenerator/network/members
[stars-shield]: https://img.shields.io/github/stars/PG85/OpenTerrainGenerator.svg?style=for-the-badge
[stars-url]: https://github.com/PG85/OpenTerrainGenerator/stargazers
[issues-shield]: https://img.shields.io/github/issues/PG85/OpenTerrainGenerator.svg?style=for-the-badge
[issues-url]: https://github.com/PG85/OpenTerrainGenerator/issues
[license-shield]: https://img.shields.io/github/license/PG85/OpenTerrainGenerator.svg?style=for-the-badge
[license-url]: https://github.com/PG85/OpenTerrainGenerator/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/linkedin_username
[discord-shield]: https://img.shields.io/discord/307111022257373185?style=for-the-badge
[discord-url]: https://discord.gg/YY2NECCBYN
[product-screenshot]: images/screenshot.png
