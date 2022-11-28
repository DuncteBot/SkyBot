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
    implementation(libs.logback)
    implementation(libs.bundles.json)
    implementation(libs.bundles.dashWeb)

    // TODO: replace with expiringmap
    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "2.8.5")
    implementation(libs.expiringmap)
    implementation(libs.trove)
    // TODO: remove jda-utils and just pull in oauth impl
    implementation(libs.jda.utils) {
        // This is fine
        exclude(module = "jda-utilities-examples")
        exclude(module = "jda-utilities-doc")
        exclude(module = "jda-utilities-command")
        exclude(module = "jda-utilities-menu")
    }
    implementation(libs.jda)  {
        exclude(module = "opus-java")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

application {
    mainClass.set("com.dunctebot.dashboard.MainKt")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
}
