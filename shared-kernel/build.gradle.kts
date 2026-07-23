plugins {
    kotlin("jvm")
    jacoco
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

dependencies {
    testImplementation(kotlin("test"))
}
