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

const mapActionTypes = (selectedType) => warnActionTypes.map(
    ({id, name}) => `<option value="${id}" ${selectedType === id ? 'selected' : ''}>${name}</option>`
);

eventBus.once('loaded', () => {
    for (const action of warnActions) {
        const li = buildLi(action);

        actions.appendChild(li);
    }

    if (actions.children.length >= 3) {
        id('add_warn_action').remove();
    }
});

function addWarnAction() {
    const size = actions.children.length + 1;

    if (size > 3) {
        id('add_warn_action').remove();
        return;
    }

    const li = buildLi(null);

    actions.appendChild(li);

    M.FormSelect.init(li.querySelector('select'));
    M.updateTextFields();
}

function buildLi(warnAction) {
    const li = document.createElement('li');
    const size = actions.children.length + 1;

    li.id = getLiId(size);
    li.classList.add('row');

    li.innerHTML = buildTemplate(warnAction, size);

    return li;
}

function buildTemplate(warnAction, num) {
    warnAction = warnAction || {
        type: {id: null, temp: false},
        threshold: 5,
        duration: 3
    };

    return `
        <div class="col s3">
            <div class="input-field">
                <select id="warningAction${num}"
                        name="warningAction${num}"
                        onchange="checkTempDuration(this)">
                    ${mapActionTypes(warnAction.type.id)}
                </select>
                <label for="warningAction${num}">Warning action</label>
            </div>
        </div>
        
        <div class="col s3" style="display: ${warnAction.type.temp ? 'block' : 'none'}">
            <div class="input-field">
                <input type="number" id="tempDays${num}"
                       name="tempDays${num}"
                       value="${warnAction.duration}"/>
                <label for="tempDays${num}">Duration</label>
            </div>
        </div>
        
        <div class="col s3">
            <div class="input-field">
                <input type="number" id="threshold${num}"
                       name="threshold${num}"
                       value="${warnAction.threshold}"/>
                <label for="threshold${num}">Warning threshold</label>
            </div>
        </div>
        
        <div class="col s1">
            <button type="button" class="btn red"
                    data-remove-action="${num}">remove</button>
        </div>
    `;
}

function removeWarnAction(itemId) {
    id(getLiId(itemId)).remove();

    if (actions.children.length < 3) {
        // somehow add the button back
    }
}
