package ml.duncte123.skybot.unstable

import ml.duncte123.skybot.Settings

class UnstableSettings : Settings() {
    init {
        Settings.isUnstable = true
        Settings.version = version.substring(0, 5) + "UNSTABLE"
    }
}