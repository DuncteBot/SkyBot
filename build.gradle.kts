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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

    kotlin("jvm") version "2.3.10" apply false
    id("org.liquibase.gradle") version "3.1.0" apply false
    id("org.jmailen.kotlinter") version "5.4.2" apply false
    id("com.gradleup.shadow") version "9.3.1" apply false
    id("com.github.breadmoirai.github-release") version "2.2.12" apply false
}

buildscript {
    dependencies {
        classpath("org.liquibase:liquibase-core:5.0.1")
    }
}

allprojects {
    group = "skybot"

    repositories {
        mavenCentral()

        maven("https://m2.dv8tion.net/releases")
        maven("https://m2.duncte123.dev/releases")
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://repo.jenkins-ci.org/releases")
        maven("https://m2.duncte123.dev/snapshots")
        maven("https://jitpack.io")
    }

    tasks.withType<Wrapper> {
        gradleVersion = "9.3.1"
        distributionType = Wrapper.DistributionType.BIN
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}
