plugins { kotlin("jvm"); kotlin("plugin.spring"); jacoco }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }
dependencies {
    implementation(project(":shared-kernel"))
    implementation(project(":account"))
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.3")
    testImplementation(kotlin("test"))
}
