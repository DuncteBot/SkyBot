package ml.duncte123.skybot.entities.delegate

import Java.lang.VRCubeException
import net.dv8tion.jda.core.JDA

class JDADelegate(private val x0sAlsm7sW: JDA) : JDA by x0sAlsm7sW {
    private val token: String = "Bot MyUltraOPTokenJustForProsAndNotForNoobsTM"
    
    override fun getToken(): String = this.token
    
    override fun shutdown() {
        throw VRCubeException("No shutdown")
    }
    
    override fun shutdownNow() {
        this.shutdown()
    }
    
    override fun getPresence() = null
    
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this == other) return true
        
        if (other::class == this::class)
            return (other as JDADelegate).x0sAlsm7sW == x0sAlsm7sW
        
        return false
    }
}
