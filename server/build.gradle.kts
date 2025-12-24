plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.example.mazeball"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.mazeball.server.ServerKt")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    implementation("org.xerial:sqlite-jdbc:3.44.1.0")

    implementation(libs.logback.classic)
}
