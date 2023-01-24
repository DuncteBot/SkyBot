<template id="settings-join-leave">
    <section class="section">
        <div class="row">
            <div class="col s12">
                <div class="section">
                    <div class="row">
                        <settings-switch
                            v-model="settings.enableJoinMessage"
                            :disabled="checkDisabled"
                            id="welcomeChannelCB"
                            name="Join message"></settings-switch>
                        <settings-switch
                            v-model="settings.enableLeaveMessage"
                            :disabled="checkDisabled"
                            id="leaveChannelCB"
                            name="Leave message"></settings-switch>
                    </div>

                    <div class="row">

                        <select-list
                            class="col s12 mm"
                            v-model="settings.welcomeLeaveChannel"
                            :options="channels"
                            prefix="#"
                            name="channel"
                            label="Join/Leave Channel"></select-list>
                    </div>

                    <div class="row">
                        <div class="input-field col s12 m6">
                            <textarea id="welcomeMessage" v-model="settings.customWelcomeMessage"
                                      name="welcomeMessage" class="materialize-textarea"></textarea>
                            <label for="welcomeMessage">Join Message</label>
                        </div>

                        <div class="input-field col s12 m6">
                            <textarea id="leaveMessage" v-model="settings.customLeaveMessage"
                                      name="leaveMessage" class="materialize-textarea"></textarea>
                            <label for="leaveMessage">Leave Message</label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="divider"></div>
                </div>

                <div class="row section">
                    <h6>Server description:</h6> <br/>
                    <div class="input-field col s12 m6">
                        <textarea id="serverDescription" v-model="settings.serverDesc"
                                  name="serverDescription" class="materialize-textarea"></textarea>
                        <label for="serverDescription">Server Description (deprecated)</label>
                        <p>This feature will be removed soon, discord has added this feature themselves</p>
                    </div>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
    Vue.component('app-settings-join-leave', {
        template: '#settings-join-leave',
        props: {
            settings: {
                type: Object,
                required: true
            },
            channels: {
                type: [Array, Object],
                required: true
            },
        },
        data: () => ({
            checkDisabled: false,
        }),
        watch: {
            'settings.welcomeLeaveChannel': {
                immediate: true,
                handler () {
                    const newVal = parseInt(this.settings.welcomeLeaveChannel, 10);

                    if (newVal > 0) {
                        this.settings.enableJoinMessage = true;
                        this.settings.enableLeaveMessage = true;
                        this.checkDisabled = false;
                    } else {
                        this.settings.enableJoinMessage = false;
                        this.settings.enableLeaveMessage = false;
                        this.checkDisabled = true;
                    }
                },
            },
        },
    });
</script>
