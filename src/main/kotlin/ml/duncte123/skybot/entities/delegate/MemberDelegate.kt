package ml.duncte123.skybot.entities.delegate

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member

class MemberDelegate(private val xH4z9a_Qe: Member) : Member by xH4z9a_Qe {
    private val jda: JDA = JDADelegate(xH4z9a_Qe.jda)
    private val guild: Guild = GuildDelegate(xH4z9a_Qe.guild)
    override fun getJDA(): JDA = JDADelegate(this.jda)
    override fun getGuild(): Guild = GuildDelegate(this.guild)
}
