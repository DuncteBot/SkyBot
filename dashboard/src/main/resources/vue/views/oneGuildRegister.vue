<template id="one-guild-register">
    <div class="container">
        <div class="row">
            <div class="col s12">
                <h3>Register your server for patron perks</h3>
                <div class="divider"></div>
                <p>If you bought the <strong>One server premium features</strong> from
                    <a href="https://www.patreon.com/DuncteBot" target="_blank">our Patreon</a> you can enter the id of
                    you and the server that you want the perks to be active in here to have the patron perks enabled</p>

                <p>Don't know how to get these ids? No problem, here's <a :href="discordLink" target="_blank">an article</a> explaining how
                    to obtain these ids: <a :href="discordLink" target="_blank">{{ discordLink }}</a></p>

                <div v-if="errorMsg" class="row">
                    <div class="col s5">
                        <p>{{ errorMsg }}</p>
                    </div>
                </div>

                <div v-if="resultData" class="row">
                    <div class="col s12 m6">
                        <div class="card indigo">
                            <div class="card-content white-text">
                                <span class="card-title">Confirm your selection</span>
                                <p>To make sure that the patron perks get added to the correct user and server,
                                    please confirm your input</p>
                                <br>

                                <p>User: <i>{{ resultData.user }}</i></p>
                                <p>Server: <i>{{ resultData.guild }}</i></p>
                                <br>

                                <p>If this is not correct please change the ids in the form and press submit again.</p>
                            </div>
                            <div class="card-action ">
                                <a href="#" class="btn green white-text text-lighten-4" @click.prevent="submitForm">This is correct</a>
                            </div>
                            <span>{{ statusMsg }}</span>
                        </div>
                    </div>
                </div>

                <form v-if="showForm" @subit.prevent="handleForm" autocomplete="off">
                    <!-- Token is from request now -->

                    <div class="input-field">
                        <input type="text" v-model="userId"
                               class="input-field validate" id="user_id" pattern="[0-9]{17,}" required/>
                        <label for="user_id">Your discord user id</label>
                        <span class="helper-text" data-error="That does not look like a discord id"
                              data-success=""></span>
                    </div>

                    <div class="input-field">
                        <input type="text" v-model="guildId"
                               class="input-field validate" id="guild_id" pattern="[0-9]{17,}" required/>
                        <label for="guild_id">Your discord server id</label>
                        <span class="helper-text" data-error="That does not look like a discord id"
                              data-success=""></span>
                    </div>

                    <div id="captcha"></div>
                    <vue-hcaptcha :sitekey="$javalin.state.captchaSitekey" @error="fuck" @verify="captchaVerify"></vue-hcaptcha>

                    <button class="btn waves-effect waves-light waves-ripple blue accent-4"
                            :class="{
                            'disabled': buttonDisabled,
                        }"
                            :disabled="buttonDisabled"
                            @click.prevent="handleForm"
                            type="button">Submit
                    </button>

                    <span>{{ statusMsg }}</span>
                </form>
            </div>
        </div>
    </div>
</template>

<script src="https://unpkg.com/@hcaptcha/vue-hcaptcha"></script>
<!--<script src="https://hcaptcha.com/1/api.js?render=explicit"></script>-->
<script>
    Vue.component('one-guild-register', {
        template: '#one-guild-register',
        components: {
            // VueHcaptcha,
        },
        data: () => ({
            discordLink: 'https://support.discord.com/hc/en-us/articles/206346498',
            statusMsg: '',
            errorMsg: '',
            showForm: true,
            buttonDisabled: false,
            userId: '',
            guildId: '',
            serverToken: '',
            resultData: null,
            captchaResponse: '',
        }),
        mounted () {
            /*hcaptcha.render('captcha', {
                sitekey: this.$javalin.state.captchaSitekey,
                theme: 'dark',
                'error-callback': (error) => {
                    console.log(error);
                },
            });*/
        },
        methods: {
            fuck (err) {
                console.error(err);
            },
            captchaVerify (token, eKey) {
                this.captchaResponse = token;
            },
            handleForm () {
                if (!this.captchaResponse) {
                    return;
                }

                this.buttonDisabled = true;

                this.statusMsg = 'Checking ids.....';

                fetch('/api/check/user-guild', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify({
                        user_id: this.userId,
                        guild_id: this.guildId,
                        captcha_response: this.captchaResponse
                    })
                })
                    .then((blob) => blob.json())
                    .then((json) => {
                        this.reset('');

                        if (json.code !== 200) {
                            this.errorMsg = `ERROR: <b>${getMessage(json.message)}</b>`;
                            return;
                        }

                        this.resultData = {
                            user: json.user.formatted,
                            userId: json.user.id,
                            guild: json.guild.name,
                            guildId: json.guild.id,
                            token: json.token,
                        };
                    })
                    .catch((e) => {
                        reset(e.message);
                        console.log(e);
                        console.error(e)
                    });

                this.buttonDisabled = false;

                return false;
            },
            reset (msg) {
                this.statusMsg = msg;
                this.errorMsg = '';
                this.buttonDisabled = false;
                this.resultData = null;
                window.scrollTo(0, 0);
            },
            submitForm () {
                if (!this.resultData || !this.resultData.token) {
                    // what?
                    return false;
                }

                this.statusMsg = 'Submitting.....';

                fetch('/api/guilds/patreon-settings', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(this.resultData)
                })
                    .then((blob) => blob.json())
                    .then((json) => {
                        this.reset('');

                        if (json.code !== 200) {
                            this.errorMsg = `ERROR: <b>${getMessage(json.message)}</b>`;
                            return;
                        }

                        this.showForm = false;
                        this.errorMsg = getMessage(json.message);
                        window.scrollTo(0, 0);
                    })
                    .catch((e) => {
                        reset(e.message);
                        console.log(e);
                        console.error(e)
                    });

                return false;
            },
        },
    })
</script>
