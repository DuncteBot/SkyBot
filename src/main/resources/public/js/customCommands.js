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

function initModal() {
    window.storedCommands = {};
    window.modal = M.Modal.init(document.querySelectorAll('.modal'))[0];
}

function initEitor() {
    const jagTagCompleter = {
        getCompletions: function (editor, session, pos, prefix, callback) {

            const firstChar = session.getTokenAt(pos.row, pos.column - 1).value;

            callback(null, wordList.map((word) => {
                return {
                    caption: word,
                    value: firstChar === "{" ? word.substr(1) : word,
                    meta: "JagTag"
                };
            }));

        }
    };

    ace.require("ace/ext/language_tools");
    window.editor = ace.edit("editor", {
        theme: "ace/theme/monokai",
        mode: "ace/mode/perl",
        maxLines: 18,
        minLines: 10,
        wrap: false,
        autoScrollEditorIntoView: true,
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: true,
    });

    editor.completers = [jagTagCompleter];

    editor.getSession().on('change', () => {
        _("chars").innerHTML = editor.getSession().getValue().length;
    });
}

function loadCommands() {
    fetch(`/api/customcommands/${guildId}`, {
        credentials: "same-origin"
    })
        .then((response) => response.json())
        .then((json) => {

            const div = _("commands");

            if (json.status === "error") {

                div.innerHTML = `<h1 class="center">Session not valid</h1>
                              <h5 class="center">Please refresh your browser</h5>`;

                return;
            }

            if (json.commands.length < 0) {

                div.innerHTML = `<h1 class="center">No commands here</h1>`;

                return;
            }

            div.innerHTML = "";

            for (const command of json.commands) {
                storedCommands[command.name] = command;

                div.innerHTML += `
                    <li class="collection-item">
                        <h6 class="left">${command.name}</h6>

                        <div class="right">
                            <a href="#" onclick="showCommand('${command.name}'); return false;"
                                class="waves-effect waves-light btn"><i class="material-icons">create</i> Edit</a>
                            <a href="#" onclick="toast('Not implemented yet, use \\'\\'${prefix}cc delete ${command.name}\\'\\''); return false" 
                                class="waves-effect waves-light red btn"><i class="material-icons">delete</i> Delete</a>
                        </div>

                        <div class="clearfix"></div>
                    </li>`;
            }

        })
        .catch(
            () => _("commands").innerHTML = "Your session has expired, please refresh your browser"
        );
}

function showCommand(name) {

    const command = storedCommands[name];

    showModal(name, command.message, `saveEdit("${name}")`);
}

function clearEditor() {
    _("chars").innerHTML = 0;
    editor.setValue("");
    modal.close();
}

function saveEdit(name) {
    toast("Saving...");

    const command = storedCommands[name];
    command.message = editor.getValue();

    fetch(`/api/customcommands/${guildId}`, {
        method: "PATCH",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(command)
    })
        .then((response) => response.json())
        .then((json) => {

            if (json.status === "success") {
                toast("Saved!");
                modal.close();
                _("chars").innerHTML = 0;
                return
            }

            toast(`Could not save: ${json.message}`);
        })
        .catch((e) => {
            toast(`Could not save: ${e}`);
        });
}

function showModal(invoke, message, method) {
    editor.setValue(message);
    _("commandName").value = invoke;

    _("saveBtn").setAttribute("onclick", `${method}; return false;`);

    modal.open();
    editor.resize();
    editor.clearSelection();
    _("chars").innerHTML = message.length;
}

function prepareCreateNew() {
    _("chars").innerHTML = 0;
    showModal("", "", "createNew()");
}

function createNew() {
    let name = _("commandName").value;
    name = name.replace(/\s+/g, '');

    if (name === "") {
        toast("Please give a name");
        return
    }

    if (name.length > 25) {
        toast("Name must be less than 25 characters");
        return
    }

    const action = editor.getValue();

    if (action === "") {
        toast("Message cannot be empty");
        return
    }

    if (action.length > 4000) {
        toast("Message cannot greater than 4000 characters");
        return
    }

    const command = {
        name: name,
        message: action,
        guildId: guildId
    };

    storedCommands[name] = command;

    toast("Adding command....");

    fetch(`/api/customcommands/${guildId}`, {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(command)
    })
        .then((response) => response.json())
        .then((json) => {

            if (json.status === "success") {
                toast("Command added");
                setTimeout(() => window.location.reload(), 500);
                modal.close();
                _("chars").innerHTML = 0;
                return
            }

            toast(`Could not save: ${json.message}`);
        })
        .catch((e) => {
            toast(`Could not save: ${e}`);
        });
}

function toast(message) {
    M.toast({
        html: message,
    });
}
