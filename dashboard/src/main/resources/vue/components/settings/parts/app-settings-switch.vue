<template id="settings-switch">
    <div class="switch">
        <template v-if="name">
            {{ name }}:
            <template v-if="breakLabel">
                <br/>
            </template>
        </template>
        <label>
            {{ customLabels[0] }}
            <input type="checkbox" :id="id"
                   :disabled="disabled"
                   v-model="valueWrapped">
            <span class="lever"></span>
            {{ customLabels[1] }}
        </label>
    </div>
</template>

<script>
    Vue.component('settings-switch', {
        template: '#settings-switch',
        props: {
            name: {
                type: String,
                required: true,
            },
            id: {
                type: String,
                required: true,
            },
            value: {
                type: Boolean,
                required: true,
            },
            customLabels: {
                type: Array,
                default: () => ['Disabled', 'Enabled'],
            },
            breakLabel: Boolean,
            disabled: Boolean,
        },
        computed: {
            valueWrapped: {
                get () {
                    return this.value && !this.disabled;
                },
                set (v) {
                    this.$emit('input', v)
                },
            },
        },
    });
</script>
