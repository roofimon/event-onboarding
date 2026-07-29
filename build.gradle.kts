import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    base
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.sonarqube)
    jacoco
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        listOf("DOCKER_HOST", "DOCKER_TLS_VERIFY", "DOCKER_CERT_PATH", "DOCKER_API_VERSION", "TESTCONTAINERS_RYUK_DISABLED").forEach { key ->
            System.getenv(key)?.let { environment(key, it) }
        }
    }
}

val allTests = subprojects.map { "${it.path}:test" }

tasks.register<JacocoReport>("jacocoRootReport") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Generates aggregate test coverage reports for all backend modules."
    dependsOn(allTests)
    executionData.from(fileTree(rootDir) { include("**/build/jacoco/test.exec") })
    sourceDirectories.from(subprojects.map { it.file("src/main/kotlin") })
    classDirectories.from(subprojects.map { it.layout.buildDirectory.dir("classes/kotlin/main") })
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Verifies aggregate test coverage across all backend modules."
    dependsOn("jacocoRootReport")
    executionData.from(fileTree(rootDir) { include("**/build/jacoco/test.exec") })
    sourceDirectories.from(subprojects.map { it.file("src/main/kotlin") })
    classDirectories.from(subprojects.map { it.layout.buildDirectory.dir("classes/kotlin/main") })
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoRootCoverageVerification")
}

sonar {
    properties {
        property("sonar.projectKey", "event-onboarding")
        property("sonar.projectName", "Event Onboarding")
        property(
            "sonar.host.url",
            providers.environmentVariable("SONAR_HOST_URL").orElse("http://localhost:9000").get(),
        )
        property(
            "sonar.coverage.jacoco.aggregateXmlReportPaths",
            layout.buildDirectory
                .file("reports/jacoco/jacocoRootReport/jacocoRootReport.xml")
                .get()
                .asFile
                .absolutePath,
        )
    }
}

tasks.named("sonar") {
    dependsOn("jacocoRootReport")
}
