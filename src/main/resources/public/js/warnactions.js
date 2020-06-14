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

const actionTypes = warnActionTypes.map(({id, name}) => `<option value="${id}">${name}</option>`);

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
    li.classList.add('row');

    li.innerHTML = `
        <div class="col s3">
            <div class="input-field">
                <select id="warningAction${size}"
                        name="warningAction${size}"
                        onchange="checkTempDuration(this)">
                    ${actionTypes}
                </select>
                <label for="warningAction${size}">Warning action</label>
            </div>
        </div>
        
        <div class="col s3" style="display: none">
            <div class="input-field">
                <input type="number" id="tempDays${size}"
                       name="tempDays${size}"
                       value="5"/>
                <label for="tempDays${size}">Duration</label>
            </div>
        </div>
        
        <div class="col s3">
            <div class="input-field">
                <input type="number" id="threshold${size}"
                       name="threshold${size}"
                       value="3"/>
                <label for="threshold${size}">Warning threshold</label>
            </div>
        </div>
        
        <div class="col s1">
            <button type="button" class="btn btn-danger"
                    data-remove-action="${size}" >remove</button>
        </div>
    `;

    actions.appendChild(li);

    M.FormSelect.init(li.querySelector('select'));
}

function removeWarnAction(itemId) {
    id(getLiId(itemId)).remove();
}
