import org.ajoberstar.grgit.Grgit

plugins {
    id("org.enginehub.codecov")
    jacoco
}

applyCommonConfiguration()
applyRootArtifactoryConfig()

val totalReport = tasks.register<JacocoReport>("jacocoTotalReport") {
    for (proj in subprojects) {
        proj.apply(plugin = "jacoco")
        proj.plugins.withId("java") {
            executionData(
                    fileTree(proj.buildDir.absolutePath).include("**/jacoco/*.exec")
            )
            sourceSets(proj.the<JavaPluginConvention>().sourceSets["main"])
            reports {
                xml.isEnabled = true
                xml.destination = rootProject.buildDir.resolve("reports/jacoco/report.xml")
                html.isEnabled = true
            }
            dependsOn(proj.tasks.named("test"))
        }
    }
}

codecov {
    reportTask.set(totalReport)
}

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        (ext["grgit"] as Grgit?)?.head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}
