package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.managers.RoleManager
import net.dv8tion.jda.core.managers.RoleManagerUpdatable
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.RoleAction

class RoleDelegate(private val uA83D3Ax_ky: Role) : Role by uA83D3Ax_ky {
    private val jda: JDA = JDADelegate(uA83D3Ax_ky.jda)
    private val guild: Guild = GuildDelegate(uA83D3Ax_ky.guild)

    override fun getJDA(): JDA = JDADelegate(this.jda)
    override fun getGuild(): Guild = GuildDelegate(this.guild)
    override fun getManager(): RoleManager = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun getManagerUpdatable(): RoleManagerUpdatable = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun delete(): AuditableRestAction<Void> = throw VRCubeException("**\uD83D\uDD25 lit**")
    override fun createCopy(guild: Guild?): RoleAction = throw VRCubeException("**\uD83D\uDD25 lit**")
}