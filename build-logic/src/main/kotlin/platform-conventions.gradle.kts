import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("base-conventions")
    id("com.github.johnrengelman.shadow")
}

// Create otgPlatform extension
val otgPlatform = extensions.create<OTGPlatformExtension>("otgPlatform")

tasks {
    jar {
        manifest.attributes(
            "Specification-Title" to "openterraingenerator",
            "Specification-Vendor" to "Team OTG",
            "Specification-Version" to "1", // We are version 1 of ourselves
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Team OTG",
            // This will cause jar + any tasks dependent on it to be out-of-date every build (timestamp will be different run-to-run) // todo: Remove if not needed
            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
        )
    }

    shadowJar {
        // Specify dependencies to include in the shadowJar
        dependencyFilter.apply {
            include(project(":common:common-util"))
            include(project(":common:common-config"))
            include(project(":common:common-customobject"))
            include(project(":common:common-generator"))
            include(project(":common:common-core"))

            include(dependency("com.fasterxml.jackson.core:jackson-annotations"))
            include(dependency("com.fasterxml.jackson.core:jackson-core"))
            include(dependency("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml"))
            include(dependency("com.fasterxml.jackson.core:jackson-databind"))
            include(dependency("org.yaml:snakeyaml"))
        }

        // Apply relocations for shaded dependencies
        relocate("com.fasterxml.jackson", "com.pg85.otg.dependency.jackson")
        relocate("org.yaml.snakeyaml", "com.pg85.otg.dependency.snakeyaml")

        // Include resources from root project resources directory in shadowJar
        from(rootDir) {
            include("resources/**/*")
        }
    }

    // Task to copy production jar for platform to root project build/distributions directory
    val copyPlatformJar = register<CopyFile>("copyPlatformJar") {
        sourceFile.set(otgPlatform.productionJar)
        destination.set(rootProject.layout.buildDirectory.dir("distributions").map {
            it.file("OpenTerrainGenerator-${project.name.capitalize()}-${project.version}.jar")
        })
    }

    // Run copyPlatformJar on build
    build {
        dependsOn(copyPlatformJar)
    }
}
