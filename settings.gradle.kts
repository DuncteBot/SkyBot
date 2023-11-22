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
include("dashboard")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            common()
            bot()
            database()
            voice()
            dashboard()
        }
    }
}

fun VersionCatalogBuilder.common() {
    library("logback", "ch.qos.logback", "logback-classic").version("1.2.10")
    library("logback-newSlf4j", "ch.qos.logback", "logback-classic").version("1.4.5")
    library("sentry", "io.sentry", "sentry-logback").version("5.4.0")
    library("org-json", "org.json", "json").version("20220924")

    library("jda", "net.dv8tion", "JDA").version("5.0.0-beta.18")

    library("trove", "net.sf.trove4j", "trove4j").version("3.0.3")

    library("okhttp", "com.squareup.okhttp3", "okhttp").version("4.9.3")

    version("jackson", "2.12.3")

    library("jackson-core", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
    library("jackson-datatype", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef("jackson")
    library("jackson-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef("jackson")

    bundle("json", listOf("jackson-core", "jackson-datatype", "jackson-kotlin"))

    library("findbugs", "com.google.code.findbugs", "jsr305").version("3.0.2")
}

fun VersionCatalogBuilder.bot() {
    // time parsing
    // implementation(group = "net.time4j", name = "time4j-base", version = "5.8")

    library("spotify", "se.michaelthelin.spotify", "spotify-web-api-java").version("8.0.0")
    library("youtube", "com.google.apis", "google-api-services-youtube").version("v3-rev222-1.25.0")
    library("expiringmap", "net.jodah", "expiringmap").version("0.5.9")

    library("weebjava", "me.duncte123", "weebJava").version("3.0.1_7")
    library("loadingBar", "me.duncte123", "loadingbar").version("1.4.1_7")
    library("jagTag", "com.github.jagrosh", "JagTag").version("6dbe1ba")
    library("wolfram-alpha", "com.github.DuncteBot", "wolfram-alpha-java-binding").version("5c123ae")
    library("duration-parser", "me.duncte123", "durationParser").version("1.1.3")
    library("emoji-java", "com.github.minndevelopment", "emoji-java").version("master-SNAPSHOT")
    library("botCommons", "me.duncte123", "botCommons").version("3.0.16")

    bundle("featureLibs", listOf("botCommons", "weebjava", "loadingBar", "jagTag", "wolfram-alpha", "duration-parser", "emoji-java"))
}

fun VersionCatalogBuilder.database() {
    library("redis", "redis.clients", "jedis").version("3.7.0")

    library("hikari", "com.zaxxer", "HikariCP").version("5.0.1")
    library("mariadb", "org.mariadb.jdbc", "mariadb-java-client").version("3.1.3")
    library("psql", "org.postgresql", "postgresql").version("42.5.0")
    library("liquibase", "org.liquibase", "liquibase-core").version("4.8.0")
    library("liquibase-slf4j", "com.mattbertolini", "liquibase-slf4j").version("4.1.0") // TODO: make this runtime only

    bundle("database", listOf("hikari", "psql", "liquibase", "liquibase-slf4j"))
}

fun VersionCatalogBuilder.voice() {
    library("sourceManagers", "com.dunctebot", "sourcemanagers").version("1.8.5")
//    implementation(group = "com.sedmelluq", name = "lavaplayer", version = "1.3.78")
    library("lavaplayer", "com.github.walkyst", "lavaplayer-fork").version("1.4.2")
    library("lavalink-client", "dev.arbjerg", "lavalink-client").version("0.0.1")
}

fun VersionCatalogBuilder.dashboard() {
    library("webjar-vue", "org.webjars.npm", "vue").version("2.6.14")

    library("javalin", "io.javalin", "javalin").version("5.3.2")


    bundle("dashWeb", listOf("javalin", "webjar-vue"))
}
