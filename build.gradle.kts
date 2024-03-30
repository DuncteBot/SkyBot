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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea

    kotlin("jvm") version "1.9.23" apply false
    id("org.liquibase.gradle") version "2.0.4" apply false
    id("org.jmailen.kotlinter") version "4.3.0" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.github.breadmoirai.github-release") version "2.2.12" apply false
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
        gradleVersion = "8.5"
        distributionType = Wrapper.DistributionType.BIN
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}
