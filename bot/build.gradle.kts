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

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import kotlin.math.min

plugins {
    pmd
    application

    kotlin("jvm")
    id("org.liquibase.gradle")
    id("org.jmailen.kotlinter")
    id("com.gradleup.shadow")
    id("com.github.breadmoirai.github-release")
}

val pmdVersion = "7.21.0"

val numberVersion = "3.110.0"

project.group = "me.duncte123.skybot"
project.version = "${numberVersion}_${getGitHash()}"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(projects.shared)
    implementation(libs.jda) {
        exclude(module = "opus-java")
    }
    implementation(libs.lavalink.client) {
        exclude(module = "lavaplayer")
    }
    implementation(libs.logback.newSlf4j)
    implementation(libs.spotify)
    implementation(libs.youtube)
    implementation(libs.javaLyrics)
    implementation(libs.bundles.featureLibs)

    implementation(kotlin("stdlib"))
    implementation(kotlin("scripting-jsr223"))

    implementation(libs.sentry)
    implementation(libs.expiringmap)
    implementation(libs.okhttp)
    implementation(libs.trove)
    implementation(libs.bundles.json)
    implementation(libs.redis)
    implementation(libs.mariadb)
    implementation(libs.bundles.database)

    pmd("net.sourceforge.pmd:pmd-ant:$pmdVersion")
    pmd("net.sourceforge.pmd:pmd-java:$pmdVersion")
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
val shadowJar: ShadowJar by tasks
val githubRelease: GithubReleaseTask by tasks
val clean: Task by tasks
val build: Task by tasks
val jar: Jar by tasks

val getGitHashTask = tasks.register<Exec>("getGitHash") {
    val envHash = System.getenv("GIT_HASH")

    if (envHash != null) {
        val getter: () -> String = {
            envHash.substring(0, min(8, envHash.length))
        }

        ext.set("gitVersionOut", getter)

        return@register
    }

    val stdout = ByteArrayOutputStream()

    commandLine("git", "rev-parse", "--short", "HEAD")
    standardOutput = stdout

    val getter: () -> String = {
        standardOutput.toString().trim()
    }

    ext.set("gitVersionOut", getter)
}

val printVersion = tasks.register<Task>("printVersion") {
    dependsOn(getGitHashTask)

    doLast {
        @Suppress("UNCHECKED_CAST") val versionGetter = project.ext.get("gitVersionOut") as () -> String

        project.version = "${numberVersion}_${versionGetter()}"

        println("CI: ${System.getenv("CI")}")
        println(project.version)
    }
}

tasks.register<Exec>("botVersion") {
    executable = "echo"
    args("v:", numberVersion)
}

build.apply {
    dependsOn(printVersion)
    dependsOn(clean)
    dependsOn(jar)

    jar.mustRunAfter(clean)
}

githubRelease.apply {
    dependsOn(shadowJar)
}

// TODO: remove, should be done from main build file
compileKotlin.apply {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

val sourcesForRelease = tasks.register<Copy>("sourcesForRelease") {
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

val generateJavaSources = tasks.register<SourceTask>("generateJavaSources") {
    val javaSources = sourceSets["main"].allJava.filter {
        !arrayOf("Settings.java", "VoteCommand.java").contains(it.name)
    }.asFileTree

    source = javaSources + fileTree(sourcesForRelease.get().destinationDir)

    dependsOn(sourcesForRelease)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
}

compileJava.apply {
    source = generateJavaSources.get().source

    dependsOn(generateJavaSources)
}

application {
    mainClass.set("me.duncte123.skybot.SkyBot")
}

shadowJar.apply {
    archiveClassifier.set("prod")
}

kotlinter {
    ignoreFormatFailures = false
    ignoreLintFailures = false
    reporters = arrayOf("checkstyle", "plain")
}

pmd {
    isConsoleOutput = true
    toolVersion = pmdVersion
    rulesMinimumPriority.set(5)
    ruleSets = listOf()
    ruleSetFiles(File("linters/pmd.xml"))
}

tasks.register<Task>("lintAll") {
//    dependsOn(tasks.lintKotlin)
    dependsOn(tasks.pmdMain)
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
    return "NOPE"
}
