<template id="no-guild">
    <div>
        <h4 class="center">
            Server not found!
        </h4>

        <p class="center">
            DuncteBot is currently not in the server that you are trying to edit. <br/>
            If you would like to invite DuncteBot to this server, you can click this link: <a href="{{ url }}" target="_blank">{{ url }}</a>
        </p>
    </div>
</template>

<script>
Vue.component('error-discord-no-guild', {
    template: '#no-guild',
    data () {
        const guildId = this.$javalin.state.guildId;

        return {
            url: `https://r.duncte.bot/inv?guild_id=${guildId}`,
        };
    },
});
</script>
