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

window.eventBus = new EventEmitter();

// We had to rename this form _ to id because
// the fucking patreon button has lodash
function id(el) {
    return document.getElementById(el);
}

function hide(itemId) {
    id(itemId).style.display = 'none';
}

function unHide(itemId) {
    id(itemId).style.display = 'block';
}

document.addEventListener('DOMContentLoaded', () => {
    id('year').innerHTML = `${(new Date()).getFullYear()}`;
    M.Sidenav.init(document.querySelectorAll('.sidenav'));

    // M.AutoInit();

    eventBus.emit('loaded');
});

document.addEventListener('click', (event) => eventBus.emit('click', event));

function getMessage(m) {
    switch (m) {
        case 'missing_input':
            return 'Please fill in all fields';
        case 'no_user':
            return 'The specified user id did not resolve any users.';
        case 'no_guild':
            return 'The specified server id did not resolve any servers.';
        default:
            return m;
    }
}

