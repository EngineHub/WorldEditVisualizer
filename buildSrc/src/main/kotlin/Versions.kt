import org.gradle.api.Project

object Versions {
    const val AUTO_VALUE = "1.7"
    const val WORLD_EDIT = "7.2.0-SNAPSHOT"
    const val JUNIT = "5.6.1"
}

// Properties that need a project reference to resolve:
class ProjectVersions(project: Project) {
    val loom = project.rootProject.property("loom.version")
    val mixin = project.rootProject.property("mixin.version")
}

val Project.versions
    get() = ProjectVersions(this)
