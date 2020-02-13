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

function submitForm(token) {

    let userId = id("user_id").value;
    let guildId = id("guild_id").value;

    id("btn").disabled = true;
    id("btn").classList.add("disabled");
    id("msg").innerHTML = "Checking ids.....";

    fetch("/api/checkUserAndGuild", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `user_id=${userId}&guild_id=${guildId}`
    })
        .then((blob) => blob.json())
        .then((json) => {
            reset("");

            if (json.code !== 200) {
                id("confirm").innerHTML = `ERROR: <b>${getMessage(json.message)}</b>`;
                return;
            }

            id("confirm").innerHTML = `
                    <div class="row">
                    <div class="col s12 m6">
                        <div class="card small indigo">
                            <div class="card-content white-text">
                                <span class="card-title">Confirm your selection</span>
                                <p>To make sure that the patron perks get added to the correct user and server,
                                    please confirm your input</p>
                                <br>

                                <p>User: <i>${json.user.formatted}</i></p>
                                <p>Server: <i>${json.guild.name}</i></p>
                                <br>

                                <p>If this is not correct please change the ids in the form and press submit again.</p>
                            </div>
                            <div class="card-action ">
                                <a href="#" class="btn green white-text text-lighten-4" onclick="id('patrons').submit(); return false;">This is correct</a>
                            </div>
                        </div>
                    </div>
                </div>
                    `;
        })
        .catch((e) => {
            reset(e.message);
            console.log(e);
            console.error(e)
        });
}

function reset(message) {
    window.scrollTo(0, 0);
    id("btn").disabled = false;
    id("btn").classList.remove("disabled");
    id("msg").innerHTML = message;
}
