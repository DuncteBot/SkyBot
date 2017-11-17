package ml.duncte123.skybot.entities.delegate

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild

class GuildDelegate(private val z88Am1Alk: Guild) : Guild by z88Am1Alk {
    private val jda: JDA = JDADelegate(z88Am1Alk.jda)
    override fun getJDA(): JDA = JDADelegate(this.jda)
}