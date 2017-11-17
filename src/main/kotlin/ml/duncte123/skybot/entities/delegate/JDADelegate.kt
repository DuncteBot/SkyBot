package ml.duncte123.skybot.entities.delegate

import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.core.JDA
import org.slf4j.event.Level

class JDADelegate(private val x0sAlsm7sW: JDA) : JDA by x0sAlsm7sW {
    private val token: String = "Bot MyUltraOPTokenJustForProsAndNotForNoobsTM"
    override fun getToken(): String = this.token

    override fun shutdown() = AirUtils.log(this::class.java.simpleName, Level.INFO, "Nice meme. Trying to shutdown a delegate..")

    override fun shutdownNow() = AirUtils.log(this::class.java.simpleName, Level.INFO, "Nice meme. Trying to shutdown a delegate..")
}