/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

public class DunctebotConfig {

    public Discord discord;
    public Apis apis;
    public Genius genius;
    public Lavalink lavalink;
    public boolean use_database;
    public Sql sql;

    public static class Discord {
        public Game game;
        public long[] constantSuperUserIds;
        public String prefix;
        public String botOwnerId;
        public Oauth oauth;
        public String embedColour;
        public int totalShards;
        public boolean local;
        public String token;


        public static class Game {
            public String streamUrl;
            public String name;
            public int type;
        }

        public static class Oauth {
            public long clientId;
            public String redirUrl;
            public String clientSecret;
        }
    }

    public static class Apis {
        public Trello trello;
        public String github;
        public String googl;
        public WeebSh weebSh;
        public Chapta chapta;
        public Spotify spotify;
        public String blargbot;
        public String wolframalpha;
        public String thecatapi;
        public String discordbots_userToken;

        public static class Trello {
            public String key;
            public String token;
        }

        public static class WeebSh {
            public String wolketoken;
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
        }
    }

    public static class Sql {
        public String host;
        public int port;
        public String database;
        public String username;
        public String password;
    }

    public static class Genius {
        public String client_secret;
        public String client_id;
    }
}
