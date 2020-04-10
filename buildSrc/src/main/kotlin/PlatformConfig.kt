import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

fun Project.applyPlatformAndCoreConfiguration() {
    applyCommonConfiguration()
    apply(plugin = "java")
    apply(plugin = "eclipse")
    apply(plugin = "idea")
    apply(plugin = "maven")
    apply(plugin = "checkstyle")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "net.minecrell.licenser")

    ext["internalVersion"] = "$version+${rootProject.ext["gitCommitHash"]}"

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        toolVersion = "8.31"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        // only bring the annotations, no checking right now
        "compileOnly"("org.checkerframework:checker-qual:3.3.0")

        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}")
        "testRuntime"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
    }

    // Java 8 turns on doclint which we fail
    tasks.withType<Javadoc>().configureEach {
        (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.register<Jar>("javadocJar") {
        dependsOn("javadoc")
        archiveClassifier.set("javadoc")
        from(tasks.getByName<Javadoc>("javadoc").destinationDir)
    }

    tasks.named("assemble").configure {
        dependsOn("javadocJar")
    }

    artifacts {
        add("archives", tasks.named("jar"))
        add("archives", tasks.named("javadocJar"))
    }

    if (name == "weviz-core") {
        tasks.register<Jar>("sourcesJar") {
            dependsOn("classes")
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        artifacts {
            add("archives", tasks.named("sourcesJar"))
        }
        tasks.named("assemble").configure {
            dependsOn("sourcesJar")
        }
    }

    tasks.named("check").configure {
        dependsOn("checkstyleMain", "checkstyleTest")
    }

    applyCommonArtifactoryConfig()

    configure<LicenseExtension> {
        header = rootProject.file("HEADER.txt")
        include("**/*.java")
    }
}
