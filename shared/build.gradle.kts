plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Библиотека для сериализации Kotlin-объектов в JSON
    implementation(libs.kotlinx.serialization.json)
}
