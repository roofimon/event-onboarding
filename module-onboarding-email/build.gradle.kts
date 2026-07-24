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
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.5.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
    testImplementation(kotlin("test"))
}
