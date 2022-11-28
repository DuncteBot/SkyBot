<template id="settings-select-list">
    <div class="input-field">
        <select v-model="dataValue" ref="sel">
            <option value="0" selected disabled>Select {{ aOrAn }} {{ name }}</option>
            <option v-for="item in options"
                    :key="item.id"
                    :value="item.id">{{ prefix }}{{ item.name }}</option>
            <option v-if="!hideDisabled" value="0">Disable</option>
        </select>
        <label>{{ label }}</label>
    </div>
</template>

<script>
    Vue.component('select-list', {
        template: '#settings-select-list',
        props: {
            label: {
                type: String,
                required: true,
            },
            name: {
                type: String,
                required: true,
            },
            prefix: {
                type: String,
                required: true,
            },
            options: {
                type: Array,
                required: true,
            },
            value: {
                type: String,
                required: true,
            },
            hideDisabled: Boolean,
        },
        data () {
            return {
                dataValue: this.value,
                vowels: ['a', 'e', 'i', 'o', 'u', 'y'],
            };
        },
        mounted () {
            M.FormSelect.init(this.$refs.sel);
        },
        watch: {
            value () {
                if (this.dataValue !== this.value) {
                    this.dataValue = this.value;
                }

               this.$nextTick(() => {
                   // fake an update event so the select sees it and reflects a change
                   // hack 101
                   // https://github.com/Dogfalo/materialize/issues/2838
                   const event = new CustomEvent('change', {
                       detail: 'change',
                       bubbles: true
                   });
                   this.$refs.sel.dispatchEvent(event);
               });
            },
            dataValue () {
                this.$emit('input', this.dataValue);
            },
        },
        computed: {
            compId () {
                return this.name + this.label.replaceAll(' ', '');
            },
            aOrAn () {
                return this.vowels.includes(this.name[0]) ? 'an' : 'a';
            }
        },
    });
</script>
