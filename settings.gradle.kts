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

include("bot")
include("shared")

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            futureIncluded()
            common()
            bot()
            database()
            voice()
        }
    }
}

// TODO: include in project
fun VersionCatalogBuilder.futureIncluded() {
//    library("models", "com.dunctebot", "dunctebot-models").version("0.1.22")
    library("botCommons", "me.duncte123", "botCommons").version("2.3.11")

    bundle("soonIncluded", listOf("botCommons"))
}

fun VersionCatalogBuilder.common() {
    library("logback", "ch.qos.logback", "logback-classic").version("1.2.10")
    library("sentry", "io.sentry", "sentry-logback").version("5.4.0")

    library("jda", "net.dv8tion", "JDA").version("4.4.0_350")

    library("trove", "net.sf.trove4j", "trove4j").version("3.0.3")

    // TODO: this is gonna be upgraded to v4
    library("okhttp", "com.squareup.okhttp3", "okhttp").version("3.14.9")

    version("jackson", "2.12.3")

    library("jackson-core", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
    library("jackson-datatype", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef("jackson")

    bundle("json", listOf("jackson-core", "jackson-datatype"))

    library("findbugs", "com.google.code.findbugs", "jsr305").version("3.0.2")
}

fun VersionCatalogBuilder.bot() {
    // time parsing
    // implementation(group = "net.time4j", name = "time4j-base", version = "5.8")

    library("spotify", "se.michaelthelin.spotify", "spotify-web-api-java").version("7.2.0")
    library("youtube", "com.google.apis", "google-api-services-youtube").version("v3-rev222-1.25.0")
    // TODO: do we still need this?
    library("groovy", "org.codehaus.groovy", "groovy-jsr223").version("3.0.7")
    library("expiringmap", "net.jodah", "expiringmap").version("0.5.9")

    library("jda-utils", "com.github.JDA-Applications", "JDA-Utilities").version("804d58a")

    library("weebjava", "me.duncte123", "weebJava").version("3.0.1_4")
    library("loadingBar", "me.duncte123", "loadingbar").version("1.4.1_7")
    library("jagTag", "com.github.jagrosh", "JagTag").version("6dbe1ba")
    library("wolfram-alpha", "com.github.DuncteBot", "wolfram-alpha-java-binding").version("5c123ae")
    library("duration-parser", "me.duncte123", "durationParser").version("1.1.3")
    library("emoji-java", "com.github.minndevelopment", "emoji-java").version("master-SNAPSHOT")

    bundle("featureLibs", listOf("weebjava", "loadingBar", "jagTag", "wolfram-alpha", "duration-parser", "emoji-java"))
}

fun VersionCatalogBuilder.database() {
    library("redis", "redis.clients", "jedis").version("3.7.0")

    library("hikari", "com.zaxxer", "HikariCP").version("5.0.0")
    // TODO: replace with official? https://jdbc.postgresql.org/
    library("psql", "com.impossibl.pgjdbc-ng", "pgjdbc-ng").version("0.8.9")
    library("liquibase", "org.liquibase", "liquibase-core").version("4.8.0")
    library("liquibase-slf4j", "com.mattbertolini", "liquibase-slf4j").version("4.1.0") // TODO: make this runtime only

    bundle("database", listOf("hikari", "psql", "liquibase", "liquibase-slf4j"))
}

fun VersionCatalogBuilder.voice() {
    library("sourceManagers", "com.dunctebot", "sourcemanagers").version("1.8.0")
//    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.78")
    library("lavaplayer", "com.github.walkyst", "lavaplayer-fork").version("1.3.98.4")
    library("lavalink-client", "com.github.DuncteBot", "Lavalink-Client").version("c1d8b73")
}
