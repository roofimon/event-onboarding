import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    base
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.spring") version "2.2.0" apply false
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.sonarqube") version "7.3.1.8318"
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
