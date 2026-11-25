plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "dev.jsp"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // The IntelliJ Platform comes from a local, pre-downloaded IDE when the `idePath` Gradle
    // property (or IDE_PATH environment variable) is set; otherwise it is resolved from the
    // JetBrains repository. The local route exists for offline machines and CI mirrors.
    val idePath = providers.gradleProperty("idePath")
            .orElse(providers.environmentVariable("IDE_PATH"))

    intellijPlatform {
        if (idePath.isPresent) {
            local(idePath)
        } else {
            intellijIdeaCommunity("253.33813.120")
        }
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    buildSearchableOptions.set(false)
    instrumentCode.set(false)

    pluginConfiguration {
        id.set("dev.jsp.highlight")
        name.set("JSP Highlight")
        version.set(project.version.toString())

        ideaVersion {
            sinceBuild.set("253")
            untilBuild.set("263.*")
        }
    }
}

tasks.test {
    useJUnit()
    systemProperty("jsp.golden.update", providers.systemProperty("jsp.golden.update").getOrElse("false"))
    testLogging {
        events("passed", "failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}
