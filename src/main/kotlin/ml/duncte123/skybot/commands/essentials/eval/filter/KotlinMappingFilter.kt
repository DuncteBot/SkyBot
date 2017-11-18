package ml.duncte123.skybot.commands.essentials.eval.filter

import ml.duncte123.skybot.entities.delegate.*
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.managers.Presence
import org.kohsuke.groovy.sandbox.GroovyValueFilter

class KotlinMappingFilter : GroovyValueFilter() {
    
    override fun filter(o: Any?): Any? {
        if (o == null) return null
        
        if (o is JDA)
            return JDADelegate(o)
        if (o is Guild)
            return GuildDelegate(o)
        if (o is Member)
            return MemberDelegate(o)
        if (o is Presence)
            return PresenceDelegate(o)
        if (o is User)
            return UserDelegate(o)
        
        return o
    }
}