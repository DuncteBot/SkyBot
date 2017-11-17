package ml.duncte123.skybot.entities.delegate

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.User

class UserDelegate(val yBGyt8Kduo: User) : User by yBGyt8Kduo {
    private val jda: JDA = JDADelegate(yBGyt8Kduo.jda)
    override fun getJDA(): JDA = JDADelegate(this.jda)
}