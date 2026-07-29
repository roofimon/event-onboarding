plugins { kotlin("jvm"); kotlin("plugin.spring"); jacoco }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }
dependencies {
    implementation(project(":module-shared-kernel"))
    implementation(project(":module-onboarding-email"))
    implementation(project(":module-onboarding-token-verification"))
    implementation(project(":module-account"))
    implementation(project(":module-onboarding-scoring"))
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.security.crypto)
    implementation(libs.avro)
    implementation(libs.apicurio.avro.serde)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.rabbitmq)
    testImplementation(kotlin("test"))
}
