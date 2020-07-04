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

eventBus.once('loaded', () => {
    fetch("/api/getUserGuilds", {
        credentials: "same-origin"
    })
        .then(response => response.json())
        .then(json => {
            id("guilds");
            const div = id("guilds");

            if (json.status === "error") {
                div.innerHTML = `<h1 class="center">Session not valid</h1>
                              <h5 class="center">Please refresh your browser or <a href="/logout">click here</a> to log out</h5>`;

                return;
            }

            if (json.guilds.length < 0) {
                div.innerHTML = `<h1 class="center">No servers found</h1>
                              <h5 class="center">Make sure that you have administrator permission in at least 1 server</h5>`;

                return;
            }

            div.innerHTML = "";

            for (const guild of json.guilds) {
                let members = "Bot not in server";
                let settingsLink = `<a href="https://discord.com/oauth2/authorize?client_id=210363111729790977&scope=bot&permissions=-1&guild_id=${guild.id}" target="_blank">Invite Bot</a>`;

                if (guild.members > -1) {
                    members = guild.members + " members";
                    settingsLink = `<a href="/server/${guild.id}/">Edit settings</a>`;
                }

                div.innerHTML += `<div class="col s12 m6 l4 xl3">
                            <div class="card horizontal hoverable">
                                <div class="card-image">
                                    <img src="${guild.iconUrl}?size=256">
                                </div>
                                <div class="card-stacked">
                                    <div class="card-content">
                                        <h6 class="truncate">${guild.name}</h6>
                                        <p>${members}</p>
                                    </div>
                                    <div class="card-action">
                                        ${settingsLink}
                                    </div>
                                </div>
                            </div>
                        </div>`;
            }

            div.innerHTML += `<div class="col s12 m6 l4 xl3">
                            <div class="card horizontal hoverable">
                                <div class="card-image">
                                    <img src="https://cdn.discordapp.com/embed/avatars/${Math.floor(Math.random() * 5)}.png?size=256" />
                                </div>
                                <div class="card-stacked">
                                    <div class="card-content">
                                        <h6 class="truncate">Your total server count:</h6>
                                        <p>${json.total} Servers</p>
                                    </div>
                                    <div class="card-action">
                                        <a href="https://patreon.com/DuncteBot" target="_blank">Become a patron</a>
                                    </div>
                                </div>
                            </div>
                        </div>`;

        })
        .catch(() =>
            id("guilds").innerHTML = "Your session has expired, please refresh your browser"
        );
});
