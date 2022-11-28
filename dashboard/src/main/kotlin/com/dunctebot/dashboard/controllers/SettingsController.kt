package com.dunctebot.dashboard.controllers

import com.dunctebot.dashboard.*
import com.dunctebot.dashboard.WebServer.Companion.FLASH_MESSAGE
import com.dunctebot.dashboard.utils.fetchGuildData
import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.WarnAction
import com.dunctebot.models.utils.Utils.colorToInt
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.RedirectResponse
import kotlin.math.max
import kotlin.math.min

object SettingsController {
    fun saveSettings(ctx: Context) {
        val params = ctx.formParamMap()
        val rateLimits = parseRateLimits(params)

        // rate limits are null when parsing failed
        if (rateLimits == null) {
            ctx.sessionAttribute(FLASH_MESSAGE, "<h4>Rate limits are invalid</h4>")
            ctx.redirect(ctx.url())

            throw RedirectResponse()
        }

        val (settings, patron) = fetchGuildData(ctx.guildId) // string

        setBasic(params, settings)
        setModeration(params, rateLimits, settings, patron)
        setMessages(params, settings)

        sendSettingUpdate(settings)

        ctx.sessionAttribute(FLASH_MESSAGE, "All settings updated!")
        ctx.redirect(ctx.url())
    }

    private fun setBasic(params: Map<String, List<String>>, settings: GuildSetting) {
        var prefix = params["prefix"]?.firstOrNull() ?: "db!"

        if (prefix.length > 10) {
            prefix = prefix.substring(0, 10)
        }

        val autorole = params["autoRoleRole"]?.firstOrNull().toSafeLong()
        val announceTracks = params["announceTracks"]?.firstOrNull().toCBBool()
        val allowAllToStop = params["allowAllToStop"]?.firstOrNull().toCBBool()
        val color = colorToInt(params["embedColor"]?.first())
        var leaveTimeout = params["leaveTimeout"]?.firstOrNull().toSafeLong().toInt()

        if (leaveTimeout < 1 || leaveTimeout > 60) {
            leaveTimeout = 1
        }

        // never really over
            settings.setCustomPrefix(prefix)
            .setAutoroleRole(autorole)
            .setAnnounceTracks(announceTracks)
            .setLeaveTimeout(leaveTimeout)
            .setAllowAllToStop(allowAllToStop)
            .setEmbedColor(color)
    }

    private fun setModeration(params: Map<String, List<String>>, rateLimits: LongArray, settings: GuildSetting, isGuildPatron: Boolean) {
        val modLogChannel = params["modChannel"]?.firstOrNull().toSafeLong()
        val autoDeHoist = params["autoDeHoist"]?.firstOrNull().toCBBool()
        val filterInvites = params["filterInvites"]?.firstOrNull().toCBBool()
        val swearFilter = params["swearFilter"]?.firstOrNull().toCBBool()
        val muteRole = params["muteRole"]?.firstOrNull().toSafeLong()
        val spamFilter = params["spamFilter"]?.firstOrNull().toCBBool()
        val kickMode = params["kickMode"]?.firstOrNull().toCBBool()
        val spamThreshold = params["spamThreshold"]?.firstOrNull()?.toIntOrNull() ?: 7
        val filterType = params["filterType"]?.firstOrNull()

        val logBan = params["logBan"]?.firstOrNull().toCBBool()
        val logUnban = params["logUnban"]?.firstOrNull().toCBBool()
        val logMute = params["logMute"]?.firstOrNull().toCBBool()
        val logKick = params["logKick"]?.firstOrNull().toCBBool()
        val logWarn = params["logWarn"]?.firstOrNull().toCBBool()
        val logInvite = params["logInvite"]?.firstOrNull().toCBBool()
        val logMember = params["logMember"]?.firstOrNull().toCBBool()

        val aiSensitivity = ((params["ai-sensitivity"]?.firstOrNull() ?: "0.7").toFloatOrNull() ?: 0.7f).minMax(0f, 1f)

        val youngAccountThreshold = params["young_account_threshold"]?.firstOrNull()?.toIntOrNull() ?: 10
        val youngAccountBanEnable = params["young_account_ban_enabled"]?.firstOrNull().toCBBool()

        val guildId = settings.guildId
        val warnActionsList = parseWarnActions(guildId, params, isGuildPatron)

        settings
            .setLogChannel(modLogChannel)
            .setAutoDeHoist(autoDeHoist)
            .setFilterInvites(filterInvites)
            .setMuteRoleId(muteRole)
            .setKickState(kickMode)
            .setRatelimits(rateLimits)
            .setEnableSpamFilter(spamFilter)
            .setEnableSwearFilter(swearFilter)
            .setSpamThreshold(spamThreshold)
            .setFilterType(filterType)
            .setBanLogging(logBan)
            .setUnbanLogging(logUnban)
            .setMuteLogging(logMute)
            .setKickLogging(logKick)
            .setWarnLogging(logWarn)
            .setInviteLogging(logInvite)
            .setMemberLogging(logMember)
            .setAiSensitivity(aiSensitivity)
            .setWarnActions(warnActionsList)
            .setYoungAccountThreshold(youngAccountThreshold)
            .setYoungAccountBanEnabled(youngAccountBanEnable)
    }

    private fun setMessages(params: Map<String, List<String>>, settings: GuildSetting) {
        val welcomeEnabled = params["welcomeChannelCB"]?.firstOrNull().toCBBool()
        val leaveEnabled = params["leaveChannelCB"]?.firstOrNull().toCBBool()
        val welcomeMessage = params["welcomeMessage"]?.firstOrNull()
        val leaveMessage = params["leaveMessage"]?.firstOrNull()
        val serverDescription = params["serverDescription"]?.firstOrNull()
        val welcomeChannel = params["welcomeChannel"]?.firstOrNull().toSafeLong()

        settings.setServerDesc(serverDescription)
            .setWelcomeLeaveChannel(welcomeChannel)
            .setCustomJoinMessage(welcomeMessage)
            .setCustomLeaveMessage(leaveMessage)
            .setEnableJoinMessage(welcomeEnabled)
            .setEnableLeaveMessage(leaveEnabled)
    }

    private fun sendSettingUpdate(setting: GuildSetting) {
        val request = jsonMapper.createObjectNode()
            .put("t", "GUILD_SETTINGS")

        request.putObject("d")
            .putArray("update")
            .add(setting.toJson(jsonMapper))

        webSocket.broadcast(request)

        duncteApis.saveGuildSetting(setting)
    }

    private fun parseRateLimits(params: Map<String, List<String>>): LongArray? {
        val rateLimits = LongArray(6)

        for (i in 0..5) {
            val reqItemId = i + 1
            val value = params.getValue("rateLimits[$reqItemId]")

            if (value.isEmpty()) {
                return null
            }

            rateLimits[i] = value.first().toLong()
        }

        return rateLimits
    }

    private fun parseWarnActions(guildId: Long, params: Map<String, List<String>>, isGuildPatron: Boolean): List<WarnAction> {
        val warnActionsList = arrayListOf<WarnAction>()
        val maxWarningActionCount = if(isGuildPatron) WarnAction.PATRON_MAX_ACTIONS else 1


        for (i in 1 until maxWarningActionCount + 1) {
            if (!params.containsKey("warningAction$i")) {
                continue
            }

            if (!params.containsKey("tempDays$i") ||
                !params.containsKey("threshold$i")
            ) {
                throw BadRequestResponse("Invalid warn action detected")
            }

            if (
            // Check for empty values (they should never be empty)
                params["warningAction$i"].isNullOrEmpty() ||
                params["tempDays$i"].isNullOrEmpty() ||
                params["threshold$i"].isNullOrEmpty()
            ) {
                throw BadRequestResponse("One or more warn actions has empty values")
            }

            val action = WarnAction.Type.valueOf(params.getValue("warningAction$i").first())
            val duration = params.getValue("tempDays$i").first().toInt()
            val threshold = params.getValue("threshold$i").first().toInt()

            warnActionsList.add(WarnAction(action, threshold, duration))
        }

        return warnActionsList
    }

    private fun Float.minMax(min: Float, max: Float): Float {
        // max returns the highest value and min returns the lowest value
        return min(max, max(min, this))
    }
}
