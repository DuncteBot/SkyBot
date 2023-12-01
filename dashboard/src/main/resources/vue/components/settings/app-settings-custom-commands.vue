<template id="custom-command-settings">
    <div class="row">
        <div class="right">
            <a href="#" class="btn btn-large waves-effect waves-light valign-wrapper"
               @click.prevent="prepareCreate">
                <i class="left tiny material-icons">add</i> Create new command
            </a>
        </div>

        <div class="clearfix"></div>
        <br/>

        <div v-if="commands.loading">
            <p class="center flow-text">
                Loading...<br /><br />
                <span class="progress">
                    <span class="indeterminate"></span>
                </span>
            </p>
        </div>
        <div v-if="commands.loadError">
            <h1 class="center">Session not valid</h1>
            <h5 class="center">Please refresh your browser</h5>
            <small>({{ commands.loadError.text }})</small>
        </div>
        <template v-if="commands.loaded">
            <ul v-if="commandsList.length" class="collection">
                <li class="collection-item" v-for="command in commandsList" :key="command.id">
                    <h6 class="left">{{ command.invoke }}</h6>

                    <div class="right">
                        <a href="#" @click.prevent="showCommand(command.invoke)"
                           class="waves-effect waves-light btn valign-wrapper"><i class="left material-icons">create</i> Edit</a>
                        <a href="#" @click.prevent="deleteCommand(command.invoke)"
                           class="waves-effect waves-light red btn valign-wrapper"><i class="left material-icons">delete</i> Delete</a>
                    </div>

                    <div class="clearfix"></div>
                </li>
            </ul>
            <h1 v-else class="center">
                No custom commands have been created yet
            </h1>
        </template>

        <div class="row" ref="editorRow" style="display: none">
            <br><br><br><br>
            <div class="col s12">
                <div class="card discord not-black">
                    <div class="card-content white-text">
                        <span>{{ prefix }}</span>
                        <div class="input-field inline">
                            <input type="text" ref="commandName" name="commandName" placeholder="" maxlength="25">
                            <label for="commandName">Command name:</label>
                        </div>

                        <p>Command content:</p>
                        <textarea ref="editor"></textarea>
                    </div>
                    <div class="card-action">
                        <div class="right">{{ charCount }}/4000</div>
                        <br/>
                        <div class="switch right">
                            <label>
                                <input type="checkbox" name="autoresponse" ref="autoresponse">
                                <span class="lever"></span>
                                <abbr class="white-text"
                                      title="Autoresponses are being called when the name of the custom command appears in a message">Autoresponse</abbr>
                            </label>
                        </div>

                        <p>Need help with the JagTag syntax? <a href="https://github.com/DuncteBot/SkyBot/wiki/JagTag"
                                                                target="_blank">Click here!</a></p>
                        <a href="#" @click.prevent="saveInternally"
                           class="waves-effect waves-green btn green white-text">Save</a>
                        <a href="#" @click.prevent="hideEditor"
                           class="waves-effect waves-red btn red white-text">Discard</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script src="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/codemirror.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/mode/javascript/javascript.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/addon/mode/simple.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/addon/hint/show-hint.min.js"></script>
<script src="/js/hinting.js"></script>
<script src="/js/wordlist.js"></script>

<script>
Vue.component('app-settings-custom-commands', {
    template: '#custom-command-settings',
    props: {
        prefix: {
            type: String,
            required: true,
        },
        guildId: {
            type: String,
            required: true,
        },
    },
    data () {
        const commandsUrl = `/api/guilds/${this.guildId}/custom-commands`;

        return {
            commandsUrl,
            commands: new LoadableData(commandsUrl, false),
            createNewCommand: false,
            curCmdName: null,
        };
    },
    mounted () {
        this.editor = CodeMirror.fromTextArea(this.$refs.editor, {
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

        this.editor.on('inputRead', function (editor, change) {
            console.log(change);
            if (change.text[0] === '{') {
                editor.showHint();
            }
        });
    },
    computed: {
        charCount () {
            return this.editor?.getValue()?.length ?? '0';
        },
        commandsList () {
            return this.commands.data.commands;
        },
    },
    methods: {
        reloadCommands () {
            this.commands.refresh(false);
        },
        showCommand (name) {
            const cmdIndex = this.commandsList.findIndex(cmd => cmd.invoke === name);

            if (cmdIndex === -1) {
                return;
            }

            const command = this.commandsList[cmdIndex];

            this.showModal(command.invoke, command.message, false, command.autoresponse);
        },
        prepareCreate () {
            this.$refs.commandName.value = '';
            this.editor.save();
            this.showModal('', '', true, false);
        },
        showModal (name, message, createNew, autoResponse) {
            this.editor.setValue(message);
            this.editor.save();
            this.$refs.commandName.value = name;
            this.curCmdName = name;
            this.$refs.autoresponse.checked = autoResponse;
            this.createNewCommand = createNew;

            this.showEditor();
            this.$nextTick(() => {
                this.editor.refresh();
            });
        },
        saveInternally () {
            if (this.createNewCommand) {
                // create new
                this.createNew();
                return;
            }

            // save command
            this.saveEdit();
        },
        saveEdit () {
            const name = this.curCmdName;

            if (!name) {
                console.log('Excuse me?');
                return;
            }

            toast("Saving...");

            const cmdIndex = this.commandsList.findIndex(cmd => cmd.invoke === name);

            if (cmdIndex === -1) {
                return;
            }

            const command = this.commandsList[cmdIndex];
            command.message = this.editor.getValue();
            command.autoresponse = this.$refs.autoresponse.checked;

            this.doFetch('PATCH', command, () => {
                toast("Saved!");
                this.$nextTick(() => {
                    this.hideEditor();
                });
            });
        },
        createNew () {
            const name = this.$refs.commandName.value.replace(/\s+/g, '');
            const autoResponse = this.$refs.autoresponse.checked;

            if (!name) {
                toast('Please give a name');
                return;
            }

            if (name.length > 25) {
                toast('Name must be less than 25 characters');
                return
            }

            const action = this.editor.getValue();

            if (!action) {
                toast('Message cannot be empty');
                return
            }

            if (action.length > 4000) {
                toast('Message cannot greater than 4000 characters');
                return
            }

            const command = {
                invoke: name,
                message: action,
                guildId: this.guildId,
                autoresponse: autoResponse,
            };

            toast("Adding command....");

            this.doFetch('POST', command, () => {
                toast('Command added');
                this.$nextTick(() => {
                    this.hideEditor();
                    setTimeout(() => {
                        this.reloadCommands();
                    }, 500);
                });
            });
        },
        deleteCommand (name) {
            const conf = confirm("Are you sure that you want to delete this command?");

            if (!conf) {
                return;
            }

            toast(`Deleting "${name}"!`);

            this.doFetch('DELETE', {invoke: name}, () => {
                toast("Deleted!");
                this.clearEditor();
                this.$nextTick(() => {
                    window.scrollTo(0, 0);
                    setTimeout(() => {
                        this.reloadCommands();
                    }, 500);
                });
            });
        },
        showEditor () {
            this.$refs.editorRow.style.display = 'block';
            this.$refs.editorRow.scrollIntoView({behavior: 'smooth'});
        },
        hideEditor () {
            this.$refs.editorRow.style.display = 'none';
            this.$nextTick(() => {
                this.clearEditor();
            });
        },
        clearEditor () {
            this.editor.setValue("");
            this.editor.save();
            this.editor.refresh();
            this.$refs.commandName.value = '';
            this.curCmdName = null;
            this.createNewCommand = false;
        },
        notSaveToast (m) {
            toast(`Could not save: ${m}`);
        },
        doFetch (method, body, cb) {
            fetch(this.commandsUrl, {
                method: method,
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(body)
            })
                .then((response) => response.json())
                .then((json) => {
                    if (json.success) {
                        cb(json);
                        return
                    }

                    this.notSaveToast(json.message || json.title || 'Unknown error');
                })
                .catch((e) => {
                    this.notSaveToast(e);
                });
        },
    },
});
</script>

<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/addon/hint/show-hint.min.css">
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/codemirror.min.css">
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/codemirror/5.52.0/theme/monokai.min.css">
<style>
/* aa */
</style>
