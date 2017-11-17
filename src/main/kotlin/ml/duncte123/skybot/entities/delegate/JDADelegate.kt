package ml.duncte123.skybot.entities.delegate

import net.dv8tion.jda.core.JDA

class JDADelegate(private val x0sAlsm7sW: JDA) : JDA by x0sAlsm7sW {
    private val token: String = "Bot MyUltraOPTokenJustForProsAndNotForNoobsTM"
    override fun getToken(): String = this.token

    override fun shutdown() {}

    override fun shutdownNow() {}
}