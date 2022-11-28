<template id="base-settings">
    <div>
        <!-- Save button-->
        <div class="fixed-action-btn" v-show="showingItem !== 'custom-commands'">
            <a @click.prevent="saveSettings()"
                class="btn-floating btn-large waves-effect waves-light waves-ripple blue accent-4 white">
                <i class="large material-icons">save</i>
            </a>
        </div>

        <app-menu :guild-name="settingData.loaded ? guild.name : null"
                  :showing="showingItem"
                  @change-menu="setShow($event)"
        ></app-menu>

        <div class="container">
            <div v-if="settingData.loaded">
                <!-- {{ settings }} -->

                <form action="#" ref="settingsForm" class="row" onsubmit="return false;">
                    <app-settings-basic
                        v-show="showingItem === 'basic'"
                        :settings="settings"
                        :roles="roles"
                    ></app-settings-basic>
                    <app-settings-moderation
                        v-show="showingItem === 'moderation'"
                        :settings="settings"
                        :channels="channels"
                        :roles="roles"
                        :patreon="dataRw.patron"
                    ></app-settings-moderation>

                    <app-settings-join-leave
                        v-show="showingItem === 'welcome-leave'"
                        :settings="settings"
                        :channels="channels"
                    ></app-settings-join-leave>

                    <app-settings-custom-commands
                        v-if="showingItem === 'custom-commands'"
                        :prefix="settings.prefix"
                        :guild-id="guild.id"
                    ></app-settings-custom-commands>
                </form>
            </div>
            <h1 v-else>Loading...</h1>
        </div>
    </div>
</template>

<script>
    Vue.component('settings', {
        template: '#base-settings',
        data () {
            const guildId = this.$javalin.pathParams['guildId'];
            const settingsURL = `/api/guilds/${guildId}/settings`;

            return {
                originalSettings: {}, // unsaved changes checking
                saving: false,
                settingsURL,
                settingData: new LoadableData(settingsURL, false),
                showingItem: (window.location.hash || 'basic').replace('#', ''),
            };
        },
        watch: {
            // TODO: ugly
            'settingData.loaded' () {
                this.$nextTick(() => {
                    this.originalSettings = { ...this.settings };
                    // M.FormSelect.init(document.querySelectorAll('select'));
                    M.updateTextFields();
                    M.Range.init(document.querySelector('input#ai-sensitivity'));
                });
            },
            showingItem () {
                this.$nextTick(() => {
                    window.scrollTo(0, 0);

                    document.querySelectorAll('textarea').forEach((it) => {
                        M.textareaAutoResize(it);
                    });
                });
            },
        },
        computed: {
            dataRw () {
                return this.settingData.data;
            },
            settings () {
                return this.settingData.data.settings;
            },
            roles () {
                return this.settingData.data.roles;
            },
            channels () {
                return this.settingData.data.channels;
            },
            guild () {
                return this.settingData.data.guild;
            },
        },
        methods: {
            async saveSettings () {
                const valid = this.$refs.settingsForm.reportValidity();

                if (!valid) {
                    toast('There are some errors, please check all fields', 6000);
                    return;
                }

                if (this.saving) {
                    return;
                }

                this.saving = true;

                toast('Saving');

                try {
                    const res = await fetch(this.settingsURL, {
                        method: 'POST',
                        credentials: 'include',
                        body: JSON.stringify(this.settings)
                    });
                    const data = await res.json();

                    console.log(data);

                    toast('Saved!');
                } catch (e) {
                    console.log(e);
                } finally {
                    this.saving = false;
                }
            },
            setShow (item) {
                this.showingItem = item;
                window.location.hash = item;
            },
        },
    });
</script>

<style>
    .caret {
        fill: #FFFFFF !important;
    }

    select {
        display: none;
    }
</style>
