/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

    let userId = _("user_id").value;
    let guildId = _("guild_id").value;

    _("btn").disabled = true;
    _("btn").classList.add("disabled");
    _("msg").innerHTML = "Checking ids.....";

    fetch("/api/checkUserAndGuild", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `user_id=${userId}&guild_id=${guildId}`
    })
        .then((blob) => blob.json())
        .then((json) => {

            window.scrollTo(0, 0);
            _("btn").disabled = false;
            _("btn").classList.remove("disabled");
            _("msg").innerHTML = "";

            if (json.code !== 200) {
                _("confirm").innerHTML = `ERROR: <b>${getMessage(json.message)}</b>`;
                return;
            }

            _("confirm").innerHTML = `
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
                                <a href="#" class="btn green white-text text-lighten-4" onclick="_('patrons').submit(); return false;">This is correct</a>
                            </div>
                        </div>
                    </div>
                </div>
                    `;
        })
        .catch((e) => {
            window.scrollTo(0, 0);
            _("btn").disabled = false;
            _("btn").classList.remove("disabled");
            _("msg").innerHTML = e.message;
            console.log(e);
            console.error(e)
        });
}
