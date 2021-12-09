import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin

// Workaround issue where loom doesn't tell Gradle that remapping sources depends on other submodules being built first
fun Task.fixRemapSourcesDependencies() {
    inputs.files(project.configurations.named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME))
        .ignoreEmptyDirectories()
        .withPropertyName("remap-classpath")
}
