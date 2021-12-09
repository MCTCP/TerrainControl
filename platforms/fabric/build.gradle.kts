plugins {
    id("platform-conventions")
    id("dev.architectury.loom") version "0.10.0-SNAPSHOT"
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:1.17.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.11.6")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.0+1.17")

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
