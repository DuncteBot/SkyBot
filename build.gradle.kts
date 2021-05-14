/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
        classpath(kotlin("gradle-plugin", version = "1.5.0"))
    }
}

plugins {
    java
    idea
    application

    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.breadmoirai.github-release") version "2.2.12"
    id("org.jmailen.kotlinter") version "3.4.4"
    pmd
}

val numberVersion = "3.103.0"

project.group = "ml.duncte123.skybot"
project.version = "${numberVersion}_${getGitHash()}"


java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()

    // caches old bintray packages
    maven("https://duncte123.jfrog.io/artifactory/bintray-maven")
    maven("https://repo.jenkins-ci.org/releases")
    maven("https://duncte123.jfrog.io/artifactory/maven")
    maven("https://m2.dv8tion.net/releases")
    maven("https://maven.notfab.net/Hosted")
    maven("https://jitpack.io")
}

val devDependencies = arrayOf(
    // SQLite
    DependencyInfo(group = "org.xerial", name = "sqlite-jdbc", version = "3.32.3")
)

dependencies {
    implementation(group = "com.dunctebot", name = "dunctebot-models", version = "0.1.20")

    // loadingbar
    implementation(group = "me.duncte123", name = "loadingbar", version = "1.4.1_7")

    // Weeb api
    implementation(group = "me.duncte123", name = "weebJava", version = "3.0.1_3")

    // botCommons
    implementation(group = "me.duncte123", name = "botCommons", version = "2.1.3")

    // JDA (java discord api)
//     implementation(group = "com.github.dv8fromtheworld", name = "JDA", version = "4208971") {
    implementation(group = "net.dv8tion", name = "JDA", version = "4.2.1_262") {
        exclude(module = "opus-java")
    }

    implementation(group = "com.dunctebot", name = "sourcemanagers", version = "1.5.5")
//    implementation(group = "com.github.DuncteBot", name = "skybot-source-managers", version = "68b7277")
    // Lavaplayer/Lavalink
//    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.76")
    implementation(group = "com.github.duncte123", name = "lavaplayer", version = "2226fa8")
    implementation(group = "com.github.DuncteBot", name = "Lavalink-Client", version = "ade4a54") {
        exclude(module = "lavaplayer")
    }
//    implementation(project(":Lavalink-Client"))

    //groovy
    implementation(group = "org.codehaus.groovy", name = "groovy-jsr223", version = "3.0.7")

    // Logback classic
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    //Spotify API
    implementation(group = "se.michaelthelin.spotify", name = "spotify-web-api-java", version = "6.5.1")

    // Youtube Cache
    implementation(group = "net.notfab.cache", name = "cache-client", version = "2.2.1")

    // Youtube api
    implementation(group = "com.google.apis", name = "google-api-services-youtube", version = "v3-rev222-1.25.0")

    // kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.5.0-RC")

    // JDA utils
    implementation(group = "com.github.JDA-Applications", name = "JDA-Utilities", version = "804d58a") {
        // This is fine
        exclude(module = "jda-utilities-examples")
        exclude(module = "jda-utilities-doc")
        exclude(module = "jda-utilities-command")
        exclude(module = "jda-utilities-menu")
        exclude(module = "jda-utilities-oauth2")
    }

    // jagtag
    implementation(group = "com.github.jagrosh", name = "JagTag", version = "6dbe1ba")

    //Wolfaram alpha
    implementation(group = "com.github.DuncteBot", name = "wolfram-alpha-java-binding", version = "5c123ae")

    // time parsing
    implementation(group = "net.time4j", name = "time4j-base", version = "5.8")

    //Sentry
    implementation(group = "io.sentry", name = "sentry-logback", version = "4.3.0")

    // durationParser
    implementation(group = "me.duncte123", name = "durationParser", version = "1.1.3")

    // ExpiringMap
    implementation(group = "net.jodah", name = "expiringmap", version = "0.5.9")

    // okhttp
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.14.9")

    // trove maps
    implementation(group = "net.sf.trove4j", name = "trove4j", version = "3.0.3")

    // emoji-java
    implementation(group = "com.vdurmont", name = "emoji-java", version = "5.1.1")

    // dev deps
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
        jvmTarget = "15"
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
    mainClass.set("ml.duncte123.skybot.SkyBot")
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
    gradleVersion = "7.0.1"
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
        "experimental:spacing-between-declarations-with-annotations",
        "experimental:spacing-between-declarations-with-comments"
    )
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.34.0"
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
        // Ugly hacks 101 :D
        val hash = System.getenv("GIT_HASH") ?: "dev"

        return hash.substring(0, Math.min(8, hash.length))
    }
}

class DependencyInfo(val group: String, val name: String, val version: String)
