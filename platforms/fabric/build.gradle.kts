plugins {
    id("platform-conventions")
    id("fabric-loom") version "0.10.+"
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
        archiveClassifier.set("dev")
    }

    shadowJar {
        archiveClassifier.set("dev-all")
    }

    remapJar {
        input.set(shadowJar.flatMap { it.archiveFile })
    }

    remapSourcesJar {
        // Workaround issue where loom doesn't tell Gradle that remapping sources depends on other submodules being built first
        inputs.files(configurations.named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME))
            .ignoreEmptyDirectories()
            .withPropertyName("remap-classpath")
    }
}

otgPlatform {
    productionJar.set(tasks.remapJar.flatMap { it.archiveFile })
}
