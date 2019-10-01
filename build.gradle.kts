/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
    }
}

plugins {
    java
    idea
    application

    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("com.github.breadmoirai.github-release") version "2.2.4"
}

project.group = "ml.duncte123.skybot"
project.version = "3.88.0_${getGitHash()}"


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    jcenter()

    maven {
        name = "Jenkins"
        url = uri("http://repo.jenkins-ci.org/releases/")
    }

    maven {
        name = "duncte123 bintray"
        url = uri("https://dl.bintray.com/duncte123/maven")
    }

    // Use JitPack if jcenter doesn"t find, not download everything from JitPack ;)
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(group = "me.duncte123", name = "loadingbar", version = "1.2.0_10")

    //Weeb api
    implementation(group = "me.duncte123", name = "weebJava", version = "2.2.0_13")

    //My little utils
    implementation(group = "me.duncte123", name = "botCommons", version = "JDA_4_1.0.39")

    //JDA (java discord api)
    implementation(group = "net.dv8tion", name = "JDA", version = "4.0.0_50") {
        exclude(module = "opus-java")
    }

    //Lavaplayer/Lavalink
    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.22")
    implementation(group = "com.github.DuncteBot", name = "Lavalink-Client", version = "0034c0b")
//    implementation(project(":Lavalink-Client"))

    // SQLite
    implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.25.2")

    //groovy
    implementation(group = "org.codehaus.groovy", name = "groovy-jsr223", version = "2.5.6")

    // Logback classic
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    // cp scraping stuffz
    implementation(group = "org.reflections", name = "reflections", version = "0.9.11")

    //Spotify API
    implementation(group = "se.michaelthelin.spotify", name = "spotify-web-api-java", version = "4.0.1")

    // Youtube api
    implementation(group = "com.google.apis", name = "google-api-services-youtube", version = "v3-rev206-1.25.0")

    //Add kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.1")

    //Spark for website
    implementation(group = "com.sparkjava", name = "spark-core", version = "2.8.0") // Override spark to the latest version
    implementation(group = "com.sparkjava", name = "spark-template-jtwig", version = "2.7.1")
    // Oauth
//    implementation(group = "com.github.JDA-Applications.JDA-Utilities", name = "jda-utilities-oauth2", version = "b98962c")
    implementation(group = "com.github.duncte123.JDA-Utilities", name = "jda-utilities-oauth2", version = "f6cdd8c")
    implementation(group = "com.github.duncte123.JDA-Utilities", name = "jda-utilities-commons", version = "f6cdd8c")

//    implementation(group = "com.jagrosh", name = "JagTag", version = "0.5")
    implementation(group = "com.github.jagrosh", name = "JagTag", version = "6dbe1ba")

    //Wolfaram alpha
    implementation(group = "com.github.DuncteBot", name = "wolfram-alpha-java-binding", version = "5c123ae")

    // https://mvnrepository.com/artifact/org.ocpsoft.prettytime/prettytime
    implementation(group = "org.ocpsoft.prettytime", name = "prettytime", version = "4.0.2.Final")

    //Sentry
    implementation(group = "io.sentry", name = "sentry-logback", version = "1.7.17")

    // Trove
    implementation(group = "net.sf.trove4j", name = "trove4j", version = "3.0.3")

    // A nice duration parser
    implementation(group = "me.duncte123", name = "durationParser", version = "1.0.15")
    implementation(group = "com.github.oshi", name = "oshi-core", version = "3.13.2")
    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "2.7.0")
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val clean: Task by tasks
val build: Task by tasks
val jar: Jar by tasks

task<Task>("printVersion") {
    println(project.version)
}

build.apply {
    dependsOn(clean)
    dependsOn(jar)

    jar.mustRunAfter(clean)
}

compileKotlin.apply {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val sourcesForRelease = task<Copy>("sourcesForRelease") {
    from("src/main/java") {
        include("**/Settings.java")

        val items = mapOf(
            "versionObj" to project.version
        )

        filter<ReplaceTokens>(mapOf("tokens" to items))
    }

    from("src/main/java") {
        include("**/VoteCommand.java")

        val regex = "\" \\+ link\\(\"(.*)\"\\)( \\+)?".toRegex()

        filter {
            it.replace(regex, "[\$1](\$1)\\\\n\"\$2")
        }
    }

    into("build/filteredSrc")

    includeEmptyDirs = false
}

val generateJavaSources = task<SourceTask>("generateJavaSources") {
    val javaSources = sourceSets["main"].allJava.filter {
        !arrayOf("Settings.java", "VoteCommand.java").contains(it.name)
    }.asFileTree

    source = javaSources + fileTree(sourcesForRelease.destinationDir)

    dependsOn(sourcesForRelease)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
}

compileJava.apply {
    source = generateJavaSources.source

    dependsOn(generateJavaSources)
}

jar.apply {
    exclude(
        "**/SQLiteDatabaseConnectionManager.class",
        "**/AudioPlayerSenderHandler.class",
        "**/SqliteDatabaseAdapter**"
    )
}

application {
    mainClassName = "ml.duncte123.skybot.SkyBot"
}

shadowJar.apply {
    archiveClassifier.set("")

    exclude(
        "**/SQLiteDatabaseConnectionManager.class",
        "**/SqliteDatabaseAdapter**"
    )

    dependencies {
        exclude(dependency("org.xerial:sqlite-jdbc:3.25.2"))
    }
}

tasks.withType<Wrapper> {
    distributionType = DistributionType.ALL
    gradleVersion = "5.6.2"
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("DuncteBot")
    repo("SkyBot")
    releaseAssets(shadowJar.outputs.files.toList())
    overwrite(true)
    prerelease(false)
}

fun getGitHash(): String {
    return try {
        val stdout = ByteArrayOutputStream()

        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }

        stdout.toString().trim()
    } catch (ignored: Throwable) {
        // Probably ramidzkh"s problem
        "DEV"
    }
}
