<template id="roles">
    <div class="container">
        <p v-if="roleData.loading">Loading...</p>
        <p v-if="roleData.loadError">Something went wrong! ({{ roleData.loadError.text }})</p>
        <template v-if="roleData.loaded">
            <h1>Roles for {{ roleData.data.guildName }}</h1>
            <table class="striped centered">
                <thead>
                <tr>
                    <th>Id</th>
                    <th>Name</th>
                    <th>Members with this role</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(role, i) in roleData.data.roles"
                    :key="i"
                    :style="{
                    backgroundColor: parseColor(role.colorRaw) // ensure it is a sting
                }">
                    <td>{{ role.id }}</td>
                    <td>{{ role.name }}</td>
                    <td>{{ role.memberCount }} Members</td>
                </tr>
                </tbody>
            </table>
        </template>
    </div>
</template>

<script>
    const defaultColor = '536870911';

    Vue.component('roles', { // the component name is what we need to enter in VueComponent("...")
        template: '#roles',
        data () {
            return {
                roleData: new LoadableData(`/api/guilds/${this.$javalin.state.guildId}/roles`),
            };
        },
        methods: {
            parseColor (col) {
                if (col === defaultColor) {
                    // default discord role color
                    return 'rgb(153, 170, 181)';
                }

                const b = col & 0xFF;
                const g = (col & 0xFF00) >>> 8;
                const r = (col & 0xFF0000) >>> 16;

                return 'rgb(' + [r, g, b].join(',') + ')';
            },
        },
    });
</script>

<style>
    table {
        margin-bottom: 20px;
    }
</style>
