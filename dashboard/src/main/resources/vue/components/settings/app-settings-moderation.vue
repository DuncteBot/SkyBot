<template id="settings-moderation">
    <section class="section">
        <div class="row">
            <div class="col s12">
                <select-list
                    class="col s12 m5"
                    v-model="settings.logChannelId"
                    :options="channels"
                    prefix="#"
                    name="channel"
                    label="Modlog channel"></select-list>

                <div class="col s12 l7">
                    <settings-switch
                        name="Invite logging"
                        id="invite_log"
                        v-model="settings.invite_logging"
                        :disabled="!patreon"
                    ></settings-switch>

                    <settings-switch
                        name="Message logging"
                        id="message_log"
                        v-model="settings.message_logging"
                    ></settings-switch>

                    <settings-switch
                        v-for="(item, i) in $javalin.state.loggingTypes.slice(2)"
                        :key="i"
                        :name="`${item} logging`"
                        :id="`${item}_log`"
                        v-model="settings[`${item.toLowerCase()}Logging`]"
                    ></settings-switch>
                </div>

                <div class="col s12">
                    <settings-switch
                        name="Auto de-hoist"
                        id="auto_dehoist"
                        v-model="settings.autoDehoist"
                    ></settings-switch>
                    <settings-switch
                        name="Invite filter"
                        id="filterInvites"
                        v-model="settings.filterInvites"
                    ></settings-switch>
                    <settings-switch
                        name="AI Profanity filter"
                        id="swearFilter"
                        v-model="settings.enableSwearFilter"
                    ></settings-switch>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col s12 m6">
                <p><a
                    href="https://github.com/DuncteBot/SkyBot/wiki/What-filter-type-do-I-choose%3F"
                    target="_blank"
                    title="Explanation of the models"
                >What type do I choose?</a></p>

                <select-list
                    v-model="settings.filterType"
                    :options="$javalin.state.filterValues"
                    prefix=""
                    name="type"
                    :hide-disabled="true"
                    label="Profanity filter type"></select-list>
            </div>

            <div class="col s12 m6">
                <p>A value between 0.7 and 0.85 is recommended (higher means less sensitive)</p>
                <div id="range-field" style="position: relative;">
                    <label for="ai-sensitivity">AI Sensitivity</label>
                    <input type="range" id="ai-sensitivity"
                           v-model="settings.aiSensitivity"
                           min="0" max="1" step="0.01"/>
                </div>
            </div>
        </div>

        <div class="row">
            <hr>
        </div>

        <div class="row">
            <select-list
                class="col s12 m4"
                v-model="settings.muteRoleId"
                :options="roles"
                prefix="@"
                name="role"
                label="Mute role"></select-list>

            <div class="input-field col s12 m6">
                <settings-switch
                    v-model="settings.spamFilterState"
                    id="spamFilter"
                    name="Toggle spam filter"></settings-switch>


                <settings-switch
                    v-model="settings.kickInsteadState"
                    id="kickSate"
                    name="Kick mode"
                    :custom-labels="['Mute members', 'Kick members']"
                ></settings-switch>
            </div>
        </div>

        <div class="row">
            <hr>
        </div>

        <div class="row">
            <div class="col s12">
                <h6>Ratelimits:</h6>
                <p>The following values indicate the mute duration in minutes for incrementing amount of
                    violations
                </p>
                <p>The first time a member gets muted, they will be muted for {{ rateLimitArray[0] }} minutes.
                    If they keep spamming and get muted again it will go to the next interval.
                    This will keep incrementing until the last one.</p>
                <p>The rate limits will only stick to these 6 numbers for duration.</p>
                <br/>

                <div class="input-field col s12 m2" v-for="(item, index) of rateLimitArray" :key="index">
                    <input type="number" v-model="rateLimitArray[index]" min="1" max="1000000" required/>
                </div>
            </div>
        </div>

        <div class="row">
            <hr>
        </div>

        <warnactions
            v-model="settings.warn_actions"
            :patreon="patreon"
        ></warnactions>

        <div class="row">
            <hr>
        </div>

        <div class="row">
            <div class="col s12">
                <h5>Auto banning of new accounts</h5>
                <p>These settings set the minimum days an account has to be exist before it may join your server,
                    the account will be banned otherwise</p>

                <settings-switch
                    v-model="settings.young_account_ban_enabled"
                    id="young_account_ban_enabled"
                    name=""></settings-switch>

                <br/>

                <div class="input-field">
                    <label for="young_account_threshold">Threshold</label>
                    <input
                        type="number"
                        id="young_account_threshold"
                        v-model="settings.young_account_threshold"
                        min="-1"
                        max="1000000"
                        required/>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
    Vue.component('app-settings-moderation', {
        template: '#settings-moderation',
        props: {
            settings: {
                type: Object,
                required: true
            },
            patreon: Boolean,
            channels: {
                type: [Array, Object],
                required: true
            },
            roles: {
                type: [Array, Object],
                required: true
            },
        },
        data () {
            return {
                rateLimitArray: this.settings.ratelimits.split('|'),
            };
        },
        watch: {
            rateLimitArray () {
                this.settings.ratelimits = this.rateLimitArray.join('|');
            },
        },
    });
</script>
