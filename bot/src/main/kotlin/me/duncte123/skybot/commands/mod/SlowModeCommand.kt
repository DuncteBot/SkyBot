/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import me.duncte123.durationparser.DurationParser
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class SlowModeCommand : ModBaseCommand() {
    init {
        this.name = "slowmode"
        this.aliases = arrayOf("sm")
        this.help = "Sets the slowmode in the current channel"
        this.usage = "<seconds (0-${TextChannel.MAX_SLOWMODE})/off>"
        this.userPermissions = arrayOf(Permission.MESSAGE_MANAGE)
        this.botPermissions = arrayOf(Permission.MANAGE_CHANNEL)
    }

    override fun execute(ctx: CommandContext) {
        val txtChan = ctx.channel

        if (txtChan !is ISlowmodeChannel) {
            sendMsg(ctx, "This channel does not accept slowmode")
            return
        }

        if (ctx.args.isEmpty()) {
            val currentMode = txtChan.slowmode
            val currentModeString = if (currentMode == 0) "disabled" else "$currentMode seconds"

            sendMsg(ctx, "Current slowmode is `$currentModeString`")
            return
        }

        val delay = ctx.args[0]

        if (delay == "off") {
            txtChan.manager.setSlowmode(0).reason("Requested by ${ctx.author.asTag}").queue()
            sendSuccess(ctx.message)
            return
        }

        val intDelay = if (!AirUtils.isInt(delay)) {
            val parser = DurationParser.parse(delay)

            if (parser.isEmpty) {
                sendMsg(ctx, "Provided argument is not an integer or duration")
                return
            }

            parser.get().seconds
        } else {
            delay.toLong()
        }

        if (intDelay < 0 || intDelay > TextChannel.MAX_SLOWMODE) {
            sendMsg(
                ctx,
                "$intDelay is not valid, a valid delay is a number in the range 0-${
                    TextChannel.MAX_SLOWMODE
                } (21600 is 6 hours in seconds)"
            )
            return
        }

        txtChan.manager.setSlowmode(intDelay.toInt()).reason("Requested by ${ctx.author.asTag}").queue()
        sendSuccess(ctx.message)
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addSubcommands(
            SubcommandData(
                "off",
                "Disables slowmode in the current channel"
            ),
            SubcommandData(
                "set",
                "Turn slowmode on, or change it in the current channel",
            )
                .addOptions(
                    OptionData(
                        OptionType.INTEGER,
                        "seconds",
                        "The amount of seconds to set the slowmode to",
                        true
                    )
                        .setMinValue(0)
                        .setMaxValue(TextChannel.MAX_SLOWMODE.toLong())
                )
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val txtChan = event.channel

        if (txtChan !is ISlowmodeChannel) {
            event.reply("This channel does not accept slowmode")
                .setEphemeral(true)
                .queue()
            return
        }

        when (event.fullCommandName) {
            "${this.name} off" -> {
                txtChan.manager.setSlowmode(0)
                    .reason("Requested by ${event.user.asTag}")
                    .queue()
                event.reply("Slowmode has been disabled")
                    .setEphemeral(true)
                    .queue()
            }
            "${this.name} set" -> {
                val newSlowmode = event.getOption("seconds")!!.asInt

                txtChan.manager.setSlowmode(newSlowmode)
                    .reason("Requested by ${event.user.asTag}")
                    .queue()

                event.reply(
                    "Slowmode has been ${if (newSlowmode == 0) "disabled" else "set to $newSlowmode seconds"}"
                )
                    .setEphemeral(true)
                    .queue()
            }

            else -> {
                event.reply("invalid command").setEphemeral(true).queue()
            }
        }
    }
}
