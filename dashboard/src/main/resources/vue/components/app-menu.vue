<template id="app-menu">
    <header>
        <div class="navbar-fixed">
            <nav class="indigo">
                <div class="nav-wrapper container">
                    <a href="#" data-target="server-setting-tabs" class="sidenav-trigger"><i class="material-icons">menu</i></a>
                    <a v-if="guildName" href="/resources/public" class="brand-logo truncate">
                        <i class="material-icons">arrow_back</i>  Editing {{ guildName }}
                    </a>
                </div>
            </nav>

            <ul id="server-setting-tabs" class="discord not-black sidenav sidenav-fixed">
                <li class="bold" :class="getActiveClass('basic')">
                    <a class="white-text"
                       href="#basic"
                       @click.prevent="changePage('basic')">Basic Settings</a>
                </li>
                <li class="bold" :class="getActiveClass('moderation')">
                    <a class="white-text"
                       href="#moderation"
                       @click.prevent="changePage('moderation')">Moderation Settings</a>
                </li>
                <li class="bold" :class="getActiveClass('welcome-leave')">
                    <a class="white-text"
                       href="#welcome-leave"
                       @click.prevent="changePage('welcome-leave')">Welcome/Leave message</a>
                </li>
                <li class="divider grey darken-2"></li>
                <li class="bold" :class="getActiveClass('custom-commands')">
                    <a class="white-text"
                       href="#custom-commands"
                       @click.prevent="changePage('custom-commands')">Custom Commands</a>
                </li>
            </ul>
        </div>
    </header>
</template>

<script>
  Vue.component('app-menu', {
    template: '#app-menu',
    props: {
      sidenav: Boolean,
      guildName: {
        type: String,
        default: null,
      },
      showing: {
        type: String,
        default: null,
      },
    },
    data: () => ({
      navOpen: false,
    }),
    mounted() {
      this.nav = M.Sidenav.init(document.querySelector('.sidenav'), {
        onOpenEnd: () => {
          this.navOpen = true;
        },
        onCloseEnd: () => {
          this.navOpen = false;
        },
      });

      const main = document.querySelector('main');
      const foot = document.querySelector('footer');

      [main, foot].forEach((el) => {
          if (!el.classList.contains('pad')) {
              el.classList.add('pad');
          }
      });
    },
    methods: {
      getActiveClass(name) {
        return {
          active: this.showing === name,
        };
      },
      changePage(page) {
        // or this will happen: https://entered.space/kU9XoK5.png
        if (this.navOpen) {
          this.nav.close();
        }

        this.$emit('change-menu', page);
      },
    },
  });
</script>

<style>
    main.pad, footer.pad {
        padding-left: 300px;
    }

    @media only screen and (max-width : 992px) {
        header, main, footer {
            padding-left: 0;
        }
    }

    .sidenav .sidenav-fixed {
        top: 20px;
    }

    .sidenav-overlay {
      z-index: 2!important;
    }
</style>
