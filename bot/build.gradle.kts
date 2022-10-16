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
import kotlin.math.min

plugins {
    pmd
    java
    idea
    application

    kotlin("jvm")
    id("org.liquibase.gradle")
    id("org.jmailen.kotlinter")
    id("com.github.johnrengelman.shadow")
    id("com.github.breadmoirai.github-release")
}

val numberVersion = "3.106.9"

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
    maven("https://m2.duncte123.dev/releases")
    maven("https://m2.duncte123.dev/snapshots")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    // TODO: include in project
    implementation(group = "com.dunctebot", name = "dunctebot-models", version = "0.1.22")

    implementation(libs.bundles.weirdLibs)

    // botCommons
    // TODO: include in project
    implementation(group = "me.duncte123", name = "botCommons", version = "2.3.11")

    // JDA (java discord api)
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }

    implementation(libs.sourceManagers)
//    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.78")
    implementation(libs.lavaplayer)
    implementation(group = "com.github.DuncteBot", name = "Lavalink-Client", version = "c1d8b73") {
        exclude(module = "lavaplayer")
    }

    implementation(libs.groovy)
    implementation(libs.logback)
    implementation(libs.spotify)
    implementation(libs.youtube)

    // kotlin
    implementation(kotlin("stdlib-jdk8"))

    // JDA utils
    // TODO: see if we can remove this
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
    // implementation(group = "net.time4j", name = "time4j-base", version = "5.8")

    //Sentry
    implementation(libs.sentry)

    // durationParser
    implementation(group = "me.duncte123", name = "durationParser", version = "1.1.3")

    // ExpiringMap
    implementation(group = "net.jodah", name = "expiringmap", version = "0.5.9")

    // okhttp
    // TODO: this is gonna be upgraded to v4
    implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "3.14.9")

    // trove maps
    implementation(libs.trove)

    // emoji-java
    implementation(group = "com.github.minndevelopment", name = "emoji-java", version = "master-SNAPSHOT")

    // jackson
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.3")
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.12.3")

    // redis
    implementation(group = "redis.clients", name = "jedis", version = "3.7.0")

    implementation(group = "com.zaxxer", name = "HikariCP", version = "5.0.0")
    // TODO: replace with official? https://jdbc.postgresql.org/
    implementation(group = "com.impossibl.pgjdbc-ng", name = "pgjdbc-ng", version = "0.8.9")
    implementation(group = "org.liquibase", name = "liquibase-core", version = "4.8.0")
    runtimeOnly(group = "com.mattbertolini", name = "liquibase-slf4j", version = "4.0.0")
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
        jvmTarget = "16"
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

application {
    mainClass.set("ml.duncte123.skybot.SkyBot")
}

shadowJar.apply {
    archiveClassifier.set("prod")
}

tasks.withType<Wrapper> {
    gradleVersion = "7.5"
    distributionType = DistributionType.ALL
}

kotlinter {
    ignoreFailures = false
    reporters = arrayOf("checkstyle", "plain")
    experimentalRules = true
    disabledRules = arrayOf(
        "filename", "no-wildcard-imports", "experimental:indent",
        "argument-list-wrapping",
        "experimental:spacing-between-declarations-with-annotations",
        "experimental:spacing-between-declarations-with-comments",
        "experimental:comment-wrapping"
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
//    dependsOn(tasks.lintKotlin)
//    dependsOn(tasks.pmdMain)
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("DuncteBot")
    repo("SkyBot")
    tagName("v$numberVersion")
    targetCommitish("main")
    releaseAssets(shadowJar.outputs.files.toList())
    overwrite(false)
    prerelease(false)
    dryRun(false)
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

        return hash.substring(0, min(8, hash.length))
    }
}

class DependencyInfo(val group: String, val name: String, val version: String)
