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

package ml.duncte123.skybot.objects.config;

import javax.annotation.Nonnull;
import java.util.Arrays;

@SuppressWarnings("PMD.ExcessiveParameterList")
public class DunctebotConfig {
    public final Discord discord;
    public final Apis apis;
    public final Lavalink lavalink;
    public final Sentry sentry;
    public final Websocket websocket;
    public final String useDatabase;
    public final String jdbcURI;

    public DunctebotConfig(Discord discord, Apis apis, Lavalink lavalink, Sentry sentry, Websocket websocket, String useDatabase, String jdbcURI) {
        this.discord = discord;
        this.apis = apis;
        this.lavalink = lavalink;
        this.sentry = sentry;
        this.websocket = websocket;
        this.useDatabase = useDatabase;
        this.jdbcURI = jdbcURI;
    }

    public static class Discord {
        public final long[] constantSuperUserIds;
        public final String prefix;
        public final int totalShards;
        public final String token;

        public Discord(long[] constantSuperUserIds, String prefix, int totalShards, String token) {
            this.constantSuperUserIds = constantSuperUserIds;
            this.prefix = prefix;
            this.totalShards = totalShards;
            this.token = token;
        }
    }

    @SuppressWarnings("PMD.ShortClassName")
    public static class Apis {
        public final String alexflipnote;
        public final String googl;
        public final String shroten;
        public final String weebSh;
        public final String ksoft;
        public final String genius;
        public final String blargbot;
        public final String wolframalpha;
        public final String thecatapi;

        public Apis(
            String alexflipnote, String googl, String shorten, String weebSh, String ksoft,
            String genius, String blargbot, String wolframalpha, String thecatapi
        ) {
            this.alexflipnote = alexflipnote;
            this.googl = googl;
            this.shroten = shorten;
            this.weebSh = weebSh;
            this.ksoft = ksoft;
            this.genius = genius;
            this.blargbot = blargbot;
            this.wolframalpha = wolframalpha;
            this.thecatapi = thecatapi;
        }
    }

    public static class Lavalink {
        public final boolean enable;
        public final LavalinkNode[] nodes;

        public Lavalink(boolean enable, LavalinkNode[] nodes) {
            this.enable = enable;
            this.nodes = nodes;
        }

        public static class LavalinkNode {
            public final String name;
            public final String wsurl;
            public final String pass;
            public final String region;

            public LavalinkNode(String name, String wsurl, String pass, String region) {
                this.name = name;
                this.wsurl = wsurl;
                this.pass = pass;
                this.region = region;
            }
        }
    }

    public static class Sentry {
        public final boolean enabled;
        public final String dsn;

        public Sentry(boolean enabled, String dsn) {
            this.enabled = enabled;
            this.dsn = dsn;
        }
    }

    public static class Websocket {
        public final String url;
        public final String password;
        public final boolean enable;

        public Websocket(String url, String password, boolean enable) {
            this.url = url;
            this.password = password;
            this.enable = enable;
        }
    }

    // TODO: redis settings
    @Nonnull
    @SuppressWarnings("PMD.PrematureDeclaration") // fuck off <3
    public static DunctebotConfig fromEnv() {
        final long[] admins = Arrays.stream(System.getenv("BOT_ADMINS").split(","))
            .mapToLong(Long::parseLong)
            .toArray();
        final Discord discord = new Discord(
            admins,
            System.getenv("BOT_PREFIX"),
            Integer.parseInt(System.getenv("BOT_TOTAL_SHARDS")),
            System.getenv("BOT_TOKEN")
        );

        final Apis apis = new Apis(
            System.getenv("API_ALEXFLIPNOTE"),
            System.getenv("API_GOOGLE"),
            System.getenv("API_SHORTEN"),
            System.getenv("API_WEEBSH"),
            System.getenv("API_KSOFT"),
            System.getenv("API_GENIUS"),
            System.getenv("API_BLARGBOT"),
            System.getenv("API_WOLFRAMALPHA"),
            System.getenv("API_THECATAPI")
        );

        final boolean lavalinkEnable = Boolean.parseBoolean(System.getenv("LAVALINK_ENABLE"));
        final Lavalink lavalink;

        // Skip lavalink settings if not enabled
        if (lavalinkEnable) {
            final int count = Integer.parseInt(System.getenv("LAVALINK_NODE_COUNT"));
            final Lavalink.LavalinkNode[] nodes = new Lavalink.LavalinkNode[count];

            for (int i = 0; i < count; i++) {
                final String host = System.getenv("LAVALINK_NODE_" + i + "_HOST");

                if (host == null) {
                    throw new IllegalArgumentException("Missing configuration for LAVALINK_NODE_"+i+". Please check the config");
                }

                final String name = System.getenv("LAVALINK_NODE_" + i + "_NAME");

                if (name == null) {
                    throw new IllegalArgumentException("Missing name for LAVALINK_NODE_"+i+". Please check the config");
                }

                nodes[i] = new Lavalink.LavalinkNode(
                    name,
                    host,
                    System.getenv("LAVALINK_NODE_"+i+"_PASS"),
                    System.getenv("LAVALINK_NODE_"+i+"_REGION")
                );
            }

            lavalink = new Lavalink(true, nodes);
        } else {
            lavalink = new Lavalink(false, null);
        }

        final Sentry sentry = new Sentry(
            Boolean.parseBoolean(System.getenv("SENTRY_ENABLED")),
            System.getenv("SENTRY_DSN")
        );

        final Websocket websocket = new Websocket(
            System.getenv("WEBSOCKET_URL"),
            System.getenv("WEBSOCKET_PASSWORD"),
            Boolean.parseBoolean(System.getenv("WEBSOCKET_ENABLE"))
        );

        return new DunctebotConfig(
            discord,
            apis,
            lavalink,
            sentry,
            websocket,
            System.getenv("USE_DATABASE"),
            System.getenv("JDBC_URI")
        );
    }
}
