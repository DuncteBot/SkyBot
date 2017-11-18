package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.managers.Presence

class PresenceDelegate(private val presence: Presence) : Presence by presence {
    
    override fun setGame(game: Game?) {
        throw VRCubeException("No set game")
    }
    
    override fun setIdle(idle: Boolean) {
        throw VRCubeException("No set idle")
    }
    
    override fun setStatus(status: OnlineStatus?) {
        throw VRCubeException("No set status")
    }
    
    override fun setPresence(game: Game?, idle: Boolean) {
        throw VRCubeException("No set game and idle")
    }
    
    override fun setPresence(status: OnlineStatus?, game: Game?) {
        throw VRCubeException("No set status and game")
    }
    
    override fun setPresence(status: OnlineStatus?, idle: Boolean) {
        throw VRCubeException("No set status and idle")
    }
    
    override fun setPresence(status: OnlineStatus?, game: Game?, idle: Boolean) {
        throw VRCubeException("No set status, game and idle")
    }
}
