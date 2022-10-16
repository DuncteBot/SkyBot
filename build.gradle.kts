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

buildscript {
    repositories {
        mavenLocal()
        maven("https://m2.dv8tion.net/releases")
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo.spring.io/plugins-release")
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.3.61")
    }
}

allprojects {
    group = "skybot"

    repositories {
        mavenCentral()

        // caches old bintray packages
        maven("https://m2.dv8tion.net/releases")
        maven("https://repo.jenkins-ci.org/releases")
        maven("https://duncte123.jfrog.io/artifactory/maven")
        maven("https://m2.duncte123.dev/releases")
        maven("https://m2.duncte123.dev/snapshots")
        maven("https://jitpack.io")
    }

    /*java {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }*/
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}
