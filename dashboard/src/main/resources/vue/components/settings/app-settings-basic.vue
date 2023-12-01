<template id="settings-basic">
    <section class="section">
        <div class="row">
            <!-- TODO: split in components -->
            <div class="col s12">
                <section class="row section">
                    <div class="input-field col s12 m1">
                        <input placeholder="db!"
                               id="prefix"
                               type="text"
                               maxlength="10"
                               v-model="settings.prefix" required>
                        <label for="prefix">Prefix</label>
                    </div>

                    <select-list
                        class="col s12 m4"
                        v-model="settings.autorole"
                        :options="roles"
                        prefix="@"
                        name="role"
                        label="AutoRole"></select-list>
                </section>

                <section class="row section">
                    <div class="input-field col s12 m5">
                        <settings-switch
                            break-label
                            v-model="settings.announceNextTrack"
                            id="leaveChannelCB"
                            name="Announce tracks"></settings-switch>
                        <br/>
                        <settings-switch
                            break-label
                            v-model="settings.allowAllToStop"
                            id="leaveChannelCB"
                            name="Stop command behavior"
                            :custom-labels="['Default behavior', 'Allow all to stop']"></settings-switch>
                    </div>

                    <div class="input-field col s12 m5">
                        <button type="button" @click="showColorPicker()"
                                :style="{
                                backgroundColor: embedColor,
                            }"
                                :class="[
                                clsName,
                            ]"
                                class="btn-large waves-effect waves-light waves-ripple">
                            Embed color
                        </button>

                        <input type="color"
                               ref="color"
                               v-model="embedColor"/>
                    </div>
                </section>

                <section class="row">
                    <div class="divider"></div>
                </section>

                <section class="row section">
                    <h6>Leave timeout:</h6>
                    <p>The following value indicates the amount of seconds before the bot checks if the vc is empty and
                        automatically leaves</p>

                    <div class="col s5">
                        <div class="input-field inline">
                            <input type="number"
                                   v-model="settings.leave_timeout"
                                   min="1"
                                   max="60"
                                   required/>
                        </div>
                        Seconds
                    </div>
                </section>
            </div>
        </div>
    </section>
</template>

<script>
    Vue.component('app-settings-basic', {
        template: '#settings-basic',
        props: {
            settings: {
                type: Object,
                required: true
            },
            roles: {
                type: [Array, Object],
                required: true
            },
        },
        data: () => ({
            clsName: 'white-text',
        }),
        watch: {
            embedColor () {
                if (this.brightnessByColor(this.embedColor) >= 150) {
                    this.clsName = 'black-text';
                } else {
                    this.clsName = 'white-text';
                }
            },
        },
        computed: {
            embedColor: {
                get () {
                    const hex = this.settings.embed_setting.embed_color;
                    return `#${hex.toString(16).padStart(6, '0')}`
                },
                set (value) {
                    this.settings
                        .embed_setting
                        .embed_color = parseInt(value.replace('#', ''), 16);
                },
            },
        },
        methods: {
            showColorPicker () {
                this.$refs.color.click();
            },
            brightnessByColor (color) {
                const match = color.substr(1).match(color.length === 7 ? /(\S{2})/g : /(\S)/g);

                if (match) {
                    const r = parseInt(match[0], 16);
                    const g = parseInt(match[1], 16);
                    const b = parseInt(match[2], 16);

                    return (
                        (r * 299) +
                        (g * 587) +
                        (b * 114)
                    ) / 1000;
                }

                return null;
            },
        },
    });
</script>

<style scoped>
    input[type="color"] {
        visibility: hidden;
        display: inline;
        position: absolute;
        bottom: 0;
        left: 0;
    }
</style>
