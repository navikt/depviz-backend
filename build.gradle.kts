import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version ("2.0.20")
    kotlin("plugin.serialization") version "2.0.20"
    application
    id("idea")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

configurations {
    this.all {
        exclude(group = "ch.qos.logback")
    }
}

val junitVersion = "5.10.1"
val ktorVersion = "2.3.12"
val log4jVersion = "2.24.1"
val assertJVersion = "3.26.3"
val prometheusVersion = "0.16.0"
val micrometerVersion = "1.10.0"


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4jVersion")

    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

    implementation("com.jayway.jsonpath:json-path:2.9.0")
    implementation("org.kohsuke:github-api:1.326")
    implementation("com.google.cloud:google-cloud-bigquery:2.43.0") {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
    testImplementation("org.wiremock:wiremock:3.9.1"){
        exclude(group = "com.github.jknack.handlebars.java")
    }

}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("app")

    manifest {
        attributes["Main-Class"] = "io.nais.depviz.AppKt"
        attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
        }
    }

    doLast {
        configurations.runtimeClasspath.get().forEach {
            val file = layout.buildDirectory.file("libs/${it.name}").get().asFile
            if (!file.exists())
                it.copyTo(file)
        }
    }
}

application {
    mainClass.set("io.nais.depviz.AppKt")
}
