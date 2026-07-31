plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    jacoco
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

dependencies {
    implementation(project(":module-shared-kernel"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(kotlin("test"))
}
