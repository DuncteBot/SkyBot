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

rootProject.name = "skybot"

include(":bot")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            common()
            bot()
            voice()
        }
    }
}

fun VersionCatalogBuilder.common() {
    library("logback", "ch.qos.logback", "logback-classic").version("1.2.10")
    library("sentry", "io.sentry", "sentry-logback").version("5.4.0")

    library("jda", "net.dv8tion", "JDA").version("4.4.0_350")

    library("trove", "net.sf.trove4j", "trove4j").version("3.0.3")
}

fun VersionCatalogBuilder.bot() {
    library("spotify", "se.michaelthelin.spotify", "spotify-web-api-java").version("7.2.0")
    library("youtube", "com.google.apis", "google-api-services-youtube").version("v3-rev222-1.25.0")
    // TODO: do we still need this?
    library("groovy", "org.codehaus.groovy", "groovy-jsr223").version("3.0.7")

    library("weebjava", "me.duncte123", "weebJava").version("3.0.1_3")
    library("loadingBar", "me.duncte123", "loadingbar").version("1.4.1_7")

    bundle("weirdLibs", listOf("weebjava", "loadingBar"))
}

fun VersionCatalogBuilder.voice() {
    library("sourceManagers", "com.dunctebot", "sourcemanagers").version("1.8.0")
    library("lavaplayer", "com.github.walkyst", "lavaplayer-fork").version("1.3.98.4")
}
