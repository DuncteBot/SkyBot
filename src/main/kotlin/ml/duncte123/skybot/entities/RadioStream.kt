package ml.duncte123.skybot.entities

class RadioStream(public val name: String, public val url: String, public val website: String?) {
    fun hasWebsite() = !website.isNullOrBlank()
}