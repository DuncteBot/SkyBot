<template id="app-settings-warn-actions">
    <div>
        <div class="row">
            <div class="col s12">
                <h5>Warn actions</h5>
                <p>The following settings determine what actions the bot take when a user reaches a set amount of warnings</p>
                <p>Temp bans are in days and temp mutes are in minutes</p>
            </div>
        </div>

        <div class="row">
            <div class="col s12">
                <button
                    @click.prevent="addAction()"
                    type="button"
                    :disabled="actions.length >= $javalin.state.patronMaxWarnActions"
                    class="btn btn-primary">add action</button>

                <ul>
                    <li class="row" v-for="(action, i) of actions" :key="i">
                        <div class="col s3">
                            <select-list
                                v-model="action.type"
                                :options="types"
                                prefix=""
                                name="action"
                                label="Action"
                                :hide-disabled="true"
                            ></select-list>
                        </div>

                        <div class="col s3" v-show="action.type.startsWith('TEMP_')">
                            <div class="input-field">
                                <input :id="`duration${i}`" type="number" min="1" v-model="action.duration"/>
                                <label :for="`duration${i}`">Duration</label>
                            </div>
                        </div>

                        <div class="col s3">
                            <div class="input-field">
                                <input type="number"
                                       min="1"
                                       :id="`threshold${i}`"
                                       v-model="action.threshold"/>
                                <label :for="`threshold${i}`">Threshold</label>
                            </div>
                        </div>

                        <div class="col s1">
                            <button
                                :disabled="actions.length === 1"
                                type="button"
                                class="btn red"
                                @click.prevent="removeAction(i)">
                                <i class="material-icons">delete_forever</i>
                            </button>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script>
    Vue.component('warnactions', {
        template: '#app-settings-warn-actions',
        props: {
            value: {
                type: Array,
                required: true
            },
            patreon: {
                type: Boolean,
                required: true,
            },
        },
        data () {
            return {
                actions: [ ...this.value ],
                types: this.$javalin.state.warnActionTypes,
            };
        },
        watch: {
            actions () {
                this.$emit('input', this.actions);
            },
        },
        methods: {
            addAction () {
                if (!this.patreon) {
                    alert('Free servers only get one warn action\n' +
                        'Want more warn actions?\n' +
                        'Consider supporting the bot by making your server a patron server');
                    return;
                }

                if (this.actions.length >= this.$javalin.state.patronMaxWarnActions) {
                    // They know what they did
                    return;
                }

                this.actions.push({
                    type: 'TEMP_BAN',
                    threshold: 3,
                    duration: 5,
                });

                this.$nextTick(() => {
                    M.updateTextFields();
                });
            },
            removeAction (i) {
                if (this.actions.length > 0) {
                    this.actions.splice(i, 1);
                }
            },
        },
    });
</script>
