/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.config;

import ml.duncte123.skybot.Author;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class DunctebotConfig {

    public Discord discord;
    public Apis apis;
    public Lavalink lavalink;
    public Sentry sentry;
    public boolean use_database;

    public static class Discord {
        public long[] constantSuperUserIds;
        public String prefix;
        public Oauth oauth;
        public int totalShards;
        public boolean local;
        public String token;

        public static class Oauth {
            public long clientId;
            public String redirUrl;
            public String clientSecret;
        }
    }

    public static class Apis {
        public Cache youtubeCache;
        public String googl;
        public String weebSh;
        public String ksoft;
        public Chapta chapta;
        public Spotify spotify;
        public String blargbot;
        public String wolframalpha;
        public String thecatapi;

        public static class Cache {
            public String endpoint;
            public String token;
        }

        public static class Chapta {
            public String sitekey;
            public String secret;
        }

        public static class Spotify {
            public String clientId;
            public String clientSecret;
        }
    }

    public static class Lavalink {
        public boolean enable;
        public LavalinkNode[] nodes;

        public static class LavalinkNode {
            public String wsurl;
            public String pass;
            public String region;
        }
    }

    public static class Sentry {
        public boolean enabled;
        public String dsn;
    }
}
