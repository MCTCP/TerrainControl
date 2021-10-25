plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.minecraftforge.net/")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    implementation("gradle.plugin.com.github.johnrengelman", "shadow", "7.1.0")
    implementation("net.minecraftforge.gradle", "ForgeGradle", "5.1.+")
    implementation("org.spongepowered", "mixingradle", "0.7-SNAPSHOT")
}
