plugins {
    id("parent-logic")
}

defaultTasks = arrayListOf("build", "publishToMavenLocal")

allprojects {
    group = "com.pg85.otg"
    version = "1.17.1-0.0.22"
    description = "Open Terrain Generator: Generate anything!"
}

subprojects {
    apply(plugin = "base-conventions")

    repositories {
        mavenCentral()
    }
}

val universalJar = tasks.register<Jar>("universalJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(buildDir.resolve("distributions"))
    archiveFileName.set("OpenTerrainGenerator-Universal-" + project.version + ".jar")
}

tasks.build {
    dependsOn(universalJar)
}

listOf(
    project(":platforms:paper"),
    project(":platforms:forge"),
    // project(":platforms:fabric"),
).forEach { proj ->
    proj.afterEvaluate {
        universalJar {
            manifest.from(proj.tasks.jar.get().manifest) // include all manifest entries from jar tasks
            from(zipTree(proj.the<OTGPlatformExtension>().productionJar))
        }
    }
}
