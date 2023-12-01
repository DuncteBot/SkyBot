<template id="error-no-perms">
    <div>
        <h1 class="center">
            Missing permissions!
        </h1>

        <h4 class="center">
            You do not have access to edit the settings for this server, if you believe this is an error please contact your server administrator and ensure that you have the "Manage Server" permission
            <br/>
            If you have this permission and are still seeing this message please wait 15 minutes and try again after.
        </h4>
    </div>
</template>

<script>
Vue.component('error-discord-no-perms', {
    template: '#error-no-perms',
});
</script>
