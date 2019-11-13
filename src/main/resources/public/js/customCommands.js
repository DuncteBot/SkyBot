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

function initModal() {
    window.storedCommands = {};
    window.editorRow = document.getElementById('editorRow');
}

function showEditor() {
    editorRow.style.display = "block";
    editorRow.scrollIntoView();
}

function hideEditor() {
    editorRow.style.display = "none";
}

function initEitor() {
    const el = document.getElementById('editor');
    window.editor = CodeMirror.fromTextArea(el, {
        mode: 'jagtag',
        lineNumbers: true,
        indentWithTabs: false,
        styleActiveLine: true,
        matchBrackets: true,
        smartIndent: true,
        autoCloseBrackets: false,
        theme: 'monokai',
        electricChars: true,
        lineWrapping: true,
        hintOptions: {
            words: window.wordList
        },
        tabMode: 'indent'
    });

    window.editor.on('inputRead', function (editor, change) {
        if (change.text[0] === '{') {
            editor.showHint();
        }
        editor.save();
        _("chars").innerHTML = editor.getValue().length;
        // localStorage.content = editor.getValue();
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
                                class="waves-effect waves-light btn valign-wrapper"><i class="left material-icons">create</i> Edit</a>
                            <a href="#" onclick="deleteCommand('${command.name}'); return false;" 
                                class="waves-effect waves-light red btn valign-wrapper"><i class="left material-icons">delete</i> Delete</a>
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

    showModal(name, command.message, `saveEdit("${name}")`, command.autoresponse);
}

function deleteCommand(name) {
    const conf = confirm("Are you sure that you want to delete this command?");

    if (!conf) {
        return;
    }

    toast(`Deleting "${name}"!`);

    fetch(`/api/customcommands/${guildId}`, {
        method: "DELETE",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            name
        })

    })
        .then((response) => response.json())
        .then((json) => {

            if (json.status === "success") {
                toast("Deleted!");
                hideEditor();
                _("chars").innerHTML = 0;
                setTimeout(() => window.location.reload(), 500);
                return
            }

            notSaveToast(json.message);
        })
        .catch((e) => {
            notSaveToast(e);
        });
}

function clearEditor() {
    _("chars").innerHTML = 0;
    editor.setValue("");
    editor.save();
    editor.refresh();
    _("commandName").value = '';
    hideEditor();
}

function saveEdit(name) {
    if (!name || !storedCommands[name]) {
        return;
    }

    toast("Saving...");

    const command = storedCommands[name];
    command.message = editor.getValue();
    command.autoresponse = _("autoresponse").checked;

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
                hideEditor();
                _("chars").innerHTML = 0;
                return
            }

            notSaveToast(json.message);
        })
        .catch((e) => {
            notSaveToast(e);
        });
}

function showModal(invoke, message, method, autoresponse) {
    editor.setValue(message);
    editor.save();
    _("commandName").value = invoke;
    _("autoresponse").checked = autoresponse;

    _("saveBtn").setAttribute("href", `javascript:${method};`);
    _("chars").innerHTML = message.length;

    showEditor();
    editor.refresh();
}

function prepareCreateNew() {
    _("chars").innerHTML = 0;
    _("commandName").value = '';
    editor.save();
    showModal("", "", "createNew()", false);
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
        guildId: guildId,
        autoresponse: _("autoresponse").checked,
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
                // modal.close();
                _("chars").innerHTML = 0;
                return
            }

            notSaveToast(json.message);
        })
        .catch((e) => {
            notSaveToast(e);
        });
}

function notSaveToast(m) {
    toast(`Could not save: ${m}`);
}

function toast(message) {
    M.toast({
        html: message,
    });
}
