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

const actions = id('actions');

function getLiId(id) {
    return `warningAction${id}-li`;
}

eventBus.once('loaded', () => {
    if (actions.children.length >= 3) {
        id('add_warn_action').remove();
    }
});

function addWarnAction() {
    const li = document.createElement('li');
    const size = actions.children.length + 1;

    if (size >= 3) {
        return;
    }

    li.id = getLiId(size);

    li.innerHTML = `
        <div class="input-field">
            <select id="warningAction${size}" name="warningAction${size}">
                <option value="mute">Mute</option>
                <option value="tempmute">Temp Mute</option>
                <option value="kick">Kick</option>
                <option value="tempban">Temp Ban</option>
                <option value="ban">Ban</option>
            </select>
            <label for="warningAction${size}">Warning action</label>
        </div>
        <button data-remove-action="warningAction${size}" type="button">remove action</button>
    `;

    actions.appendChild(li);
}

function removeWarnAction(itemId) {
    id(getLiId(itemId)).remove();
}
