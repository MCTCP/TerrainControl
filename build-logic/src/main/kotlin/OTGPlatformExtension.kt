import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty

abstract class OTGPlatformExtension(project: Project) {
    val productionJar: RegularFileProperty = project.objects.fileProperty()
}
