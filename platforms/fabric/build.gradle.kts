plugins {
    id("platform-conventions")
    id("dev.architectury.loom") version "0.10.0-SNAPSHOT"
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:1.18")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.12.8")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.44.0+1.18")

    implementation(project(":common:common-core"))
}

tasks {
    processResources {
        val replacements = mapOf(
            "version" to project.version
        )
        inputs.properties(replacements)

        filesMatching("fabric.mod.json") {
            expand(replacements)
        }
    }

    jar {
        archiveClassifier.set("deobf")
    }

    shadowJar {
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
