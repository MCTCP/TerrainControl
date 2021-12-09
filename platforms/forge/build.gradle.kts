plugins {
    id("platform-conventions")
    id("dev.architectury.loom") version "0.10.0-SNAPSHOT"
}

val mcVersion = "1.17.1"
val forgeVersion = "37.1.0"

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("otg.refmap.json")
    }

    forge {
        mixinConfigs.add("otg.mixins.json")

        dataGen {
            mod("otg")
        }
    }
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

val otg: Configuration by configurations.creating
configurations {
    implementation {
        extendsFrom(otg)
    }
    forgeDependencies {
        extendsFrom(otg)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:$mcVersion-$forgeVersion")

    otg(project(":common:common-core"))

    val worldeditVersion = "7.2.7"
    compileOnly("com.sk89q.worldedit:worldedit-core:$worldeditVersion") {
        exclude("org.yaml")
    }
    modCompileOnly("com.sk89q.worldedit:worldedit-forge-mc$mcVersion:$worldeditVersion")
}

tasks {
    processResources {
        val replacements = mapOf("version" to project.version)
        inputs.properties(replacements)

        filesMatching("META-INF/mods.toml") {
            expand(replacements)
        }
    }

    jar {
        archiveClassifier.set("deobf")
    }

    shadowJar {
        configurations = listOf(otg) as List<FileCollection>
        archiveClassifier.set("deobf-all")
    }

    remapJar {
        input.set(shadowJar.flatMap { it.archiveFile })
    }

    remapSourcesJar {
        fixRemapSourcesDependencies()
    }
}

otgPlatform {
    productionJar.set(tasks.remapJar.flatMap { it.archiveFile })
}
