plugins {
//    java
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "com.dunctebot"
version = "1.0"


dependencies {
    implementation(projects.shared)
    implementation(libs.logback.newSlf4j)
    implementation(libs.bundles.json)
    implementation(libs.bundles.dashWeb)
    implementation(libs.expiringmap)
    implementation(libs.trove)
    implementation(libs.jda)  {
        exclude(module = "opus-java")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.dunctebot.dashboard.MainKt")
}

tasks {
    shadowJar {
        archiveClassifier.set("-all")
    }
}
