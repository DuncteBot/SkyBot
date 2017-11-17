package ml.duncte123.skybot.entities

import net.dv8tion.jda.core.entitirs.impl.MemberImpl
import net.dv8tion.jda.core.entities.*

class MemberDelegate(val xH4z9a_Qe: Member) : MemberImpl {
    public fun getRoles(): List<Role> = xH4z9a_Qe.roles
    
    public fun getNickname(): String? = xH4z9a_Qe.nickname
    
    public fun getGame(): Game? = xH4z9a_Qe.game
}
