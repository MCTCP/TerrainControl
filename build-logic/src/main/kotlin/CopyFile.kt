import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CopyFile : DefaultTask() {
    @InputFile
    val sourceFile: RegularFileProperty = project.objects.fileProperty()

    @OutputFile
    val destination: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    private fun copyFile() {
        destination.get().asFile.parentFile.mkdirs()
        sourceFile.get().asFile.copyTo(destination.get().asFile, overwrite = true)
    }
}
