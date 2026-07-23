plugins { kotlin("jvm"); kotlin("plugin.spring"); jacoco }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }
dependencies {
    implementation(project(":shared-kernel"))
    implementation(project(":onboarding-email"))
    implementation(project(":onboarding-token-verification"))
    implementation(project(":account"))
    implementation(project(":onboarding-scoring"))
    implementation("org.springframework.boot:spring-boot-starter-amqp:3.5.3")
    implementation("org.springframework.security:spring-security-crypto:6.5.1")
    implementation("org.apache.avro:avro:1.11.3")
    implementation("io.apicurio:apicurio-registry-serdes-avro-serde:2.5.11.Final")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
    testImplementation("org.testcontainers:testcontainers:1.21.1")
    testImplementation("org.testcontainers:junit-jupiter:1.21.1")
    testImplementation("org.testcontainers:rabbitmq:1.21.1")
    testImplementation(kotlin("test"))
}
