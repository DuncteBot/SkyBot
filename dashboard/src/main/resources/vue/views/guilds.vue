<template id="base-guilds">
    <div v-if="guildsData.loaded">
        <template v-if="!guilds.length">
            <h1 class="center">No servers found</h1>
            <h5 class="center">Make sure that you have administrator permission in at least 1 server</h5>
        </template>
        <div class="row" v-else>
            <div class="col s12 m6 l4 xl3" v-for="guild in guilds" :key="guild.id">
                <div class="card horizontal hoverable">
                    <div class="card-image">
                        <img :src="`${guild.iconUrl}?size=256`">
                    </div>
                    <div class="card-stacked">
                        <div class="card-content">
                            <h6 class="truncate">{{ guild.name }}</h6>
                            <p>{{ getMembersDisplay(guild) }}</p>
                        </div>
                        <div class="card-action" v-html="getSettingsLink(guild)"></div>
                    </div>
                </div>
            </div>

            <div class="col s12 m6 l4 xl3">
                <div class="card horizontal hoverable">
                    <div class="card-image">
                        <img :src="`https://cdn.discordapp.com/embed/avatars/${Math.floor(Math.random() * 5)}.png?size=256`" />
                    </div>
                    <div class="card-stacked">
                        <div class="card-content">
                            <h6 class="truncate">Your total server count:</h6>
                            <p>{{ guildsData.data.total }} Servers</p>
                        </div>
                        <div class="card-action">
                            <a href="https://patreon.com/DuncteBot" target="_blank">Become a patron</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div v-else>
        <section class="section" v-if="error">
            <h1 class="center">Session not valid</h1>
            <h5 class="center">Please refresh your browser or <a href="/logout">click here</a> to log out</h5>
        </section>
        <section v-else class="section">
            <div class="row">
                <div class="col center s4 offset-s4">
                    <h1 class="center">
                        Loading...
                    </h1>
                    <br />
                    <br />
                    <div class="progress center">
                        <div class="indeterminate"></div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</template>

<script>
    Vue.component('guilds', {
       template: '#base-guilds',
        data () {
           return {
               error: false,
               guildsData: new LoadableData('/api/guilds', false, (error) => {
                   this.error = error;
               }),
           };
        },
        computed: {
           guilds () {
               return this.guildsData.data.guilds;
           },
        },
        methods: {
           setError () {},
           getSettingsLink (guild) {
               if (guild.members > -1) {
                   return `<a href="/server/${guild.id}">Edit settings</a>`;
               }

               return '<a href="https://r.duncte.bot/inv?guild_id=${guild.id}" target="_blank">Invite Bot</a>';
           },
            getMembersDisplay (guild) {
               if (guild.members > -1) {
                   return `${guild.members} members`;
               }

               return 'Bot not in server';
            },
        },
    });
</script>

<style>
img {
    height: 200px !important;
    width: 200px !important;
    max-width: 200px !important;
    clip: rect(0px, 100px, 200px, 0px);
    position: absolute !important;
}

.card-image {
    width: 100px !important;
    overflow: hidden;
}
</style>
