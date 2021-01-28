/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.21"))
    }
}

plugins {
    java
    idea
    application

    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.github.breadmoirai.github-release") version "2.2.12"
    id("org.jmailen.kotlinter") version "3.2.0"
    pmd
}

val numberVersion = "3.101.0"

project.group = "ml.duncte123.skybot"
project.version = "${numberVersion}_${getGitHash()}"


java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    jcenter()

    maven {
        url = uri("https://repo.jenkins-ci.org/releases/")
    }

    maven {
        url = uri("https://dl.bintray.com/duncte123/maven")
    }

    maven {
        url = uri("https://maven.notfab.net/Hosted")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

val devDependencies = arrayOf(
    // SQLite
    DependencyInfo(group = "org.xerial", name = "sqlite-jdbc", version = "3.32.3")
)

dependencies {
    implementation(group = "com.dunctebot", name = "dunctebot-models", version = "0.0.11")

    // loadingbar
    implementation(group = "me.duncte123", name = "loadingbar", version = "1.3.2_454301933")

    // Weeb api
//    implementation(group = "me.duncte123", name = "weebJava", version = "2.2.0_13")
    implementation(group = "com.github.duncte123", name = "weeb.java", version = "18ba8fc")

    // botCommons
    implementation(group = "me.duncte123", name = "botCommons", version = "2.0.122")

    // JDA (java discord api)
    implementation(group = "net.dv8tion", name = "JDA", version = "4.2.0_225") {
        exclude(module = "opus-java")
    }

    /*implementation(group = "com.github.dv8fromtheworld", name = "JDA", version = "4208971") {
        exclude(module = "opus-java")
    }*/

    implementation(group = "com.dunctebot", name = "sourcemanagers", version = "1.4.0")
    // Lavaplayer/Lavalink
    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.66")
    implementation(group = "com.github.dunctebot", name = "Lavalink-Client", version = "478fbea")
//    implementation(project(":Lavalink-Client"))

    // Logback classic
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    //Spotify API
    implementation(group = "se.michaelthelin.spotify", name = "spotify-web-api-java", version = "6.3.0")

    // Youtube Cache
    implementation(group = "net.notfab.cache", name = "cache-client", version = "2.2.1")

    // Youtube api
    implementation(group = "com.google.apis", name = "google-api-services-youtube", version = "v3-rev222-1.25.0")

    //groovy
    // TODO: convert logback to xml
    implementation(group = "org.codehaus.groovy", name = "groovy-jsr223", version = "3.0.6")

    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("scripting-jsr223"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.4.2")

    implementation(group = "com.jagrosh", name = "jda-utilities-commons", version = "3.0.4")

//    implementation(group = "com.jagrosh", name = "JagTag", version = "0.5")
    implementation(group = "com.github.jagrosh", name = "JagTag", version = "6dbe1ba")

    //Wolfaram alpha
    implementation(group = "com.github.DuncteBot", name = "wolfram-alpha-java-binding", version = "5c123ae")

    implementation(group = "net.time4j", name = "time4j-base", version = "5.7")

    //Sentry
    implementation(group = "io.sentry", name = "sentry-logback", version = "1.7.30")

    // durationParser
    implementation(group = "me.duncte123", name = "durationParser", version = "1.0.15")

    // Oshi
    implementation(group = "com.github.oshi", name = "oshi-core", version = "5.2.5")

    // ExpiringMap
    implementation(group = "net.jodah", name = "expiringmap", version = "0.5.9")

    // okhttp
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.14.9")
    implementation(group = "net.sf.trove4j", name = "trove4j", version = "3.0.3")

    devDependencies.forEach {
        implementation(group = it.group, name = it.name, version = it.version)
    }
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val clean: Task by tasks
val build: Task by tasks
val jar: Jar by tasks

val printVersion = task<Task>("printVersion") {
    println("CI: ${System.getenv("CI")}")
    println(project.version)
}

task<Exec>("botVersion") {
    executable = "echo"
    args("v:", numberVersion)
}

build.apply {
    dependsOn(printVersion)
    dependsOn(clean)
    dependsOn(jar)

    jar.mustRunAfter(clean)
}

compileKotlin.apply {
    kotlinOptions {
        jvmTarget = "11"
    }
}

val sourcesForRelease = task<Copy>("sourcesForRelease") {
    from("src/main/java") {
        include("**/Settings.java")

        val isCi = System.getenv("CI") == "true"

        filter {
            it.replaceFirst(
                """public static final boolean IS_LOCAL = VERSION.startsWith("@versionObj");""",
                """public static final boolean IS_LOCAL = ${!isCi};"""
            )
        }

        if (isCi) {
            val items = mapOf(
//                "versionObj" to project.version
                "versionObj" to numberVersion
            )

            filter<ReplaceTokens>(mapOf("tokens" to items))
        }
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
        "**/SQLiteTimers.class",
        "**/SqliteDatabaseAdapter**"
    )

    dependencies {
        devDependencies.forEach {
            exclude(dependency("${it.group}:${it.name}:${it.version}"))
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.8"
    distributionType = DistributionType.ALL
}

kotlinter {
    ignoreFailures = false
    indentSize = 4
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = true
    disabledRules = arrayOf(
        "no-wildcard-imports", "experimental:indent",
        "experimental:argument-list-wrapping",
        "experimental:spacing-between-declarations-with-annotations"
    )
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.28.0"
    rulesMinimumPriority.set(5)
    ruleSets = listOf()
    ruleSetFiles(File("linters/pmd.xml"))
}

task<Task>("lintAll") {
    dependsOn(tasks.lintKotlin)
    dependsOn(tasks.pmdMain)
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("DuncteBot")
    repo("SkyBot")
    tagName(numberVersion)
    overwrite(false)
    prerelease(false)
    body(changelog())
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

class DependencyInfo(val group: String, val name: String, val version: String)
