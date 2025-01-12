plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "9.0"
    id("org.flywaydb.flyway") version "11.1.0"
}

group = "myhan"
version = "1.0.0"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.1")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqGenerator("com.h2database:h2")
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

tasks.withType<Test> { useJUnitPlatform() }

val dbUrl = "jdbc:h2:file:${layout.buildDirectory.get().asFile.absolutePath}/tmp/localdb"

flyway {
    url = dbUrl
    user = "sa"
    password = "password"
    locations = arrayOf("classpath:db/migration")
    baselineOnMigrate = true
    cleanDisabled = false
}

tasks.flywayMigrate { dependsOn(tasks.processResources) }

// jOOQ 코드 생성 설정
jooq {
    version.set("3.19.16")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") { // main 설정
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = dbUrl
                    user = "sa"
                    password = "password"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "myhan.jooq.generated"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.named("generateJooq") { dependsOn("flywayMigrate") }
