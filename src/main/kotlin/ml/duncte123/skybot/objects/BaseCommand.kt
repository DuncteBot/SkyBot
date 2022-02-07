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

package ml.duncte123.skybot.objects

import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.toHuman
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.Flag
import ml.duncte123.skybot.objects.command.ICommand
import net.dv8tion.jda.api.Permission
import java.util.concurrent.TimeUnit

abstract class BaseCommand @JvmOverloads constructor (
    private val name: String,
    val help: String,
    private val category: CommandCategory = CommandCategory.MAIN,
    private val extraInfo: String? = null,

    private val aliases: Array<String> = arrayOf(),
    private val displayAliasesInHelp: Boolean = false,

    val usage: String = "",
    val requiresArgs: Boolean = false,
    private val requiredArgCount: Int = 1,

    val userPermissions: List<Permission> = listOf(),
    val botPermissions: List<Permission> = listOf(),
    val flags: Array<Flag> = arrayOf(),

    val cooldown: Int = 0,
    val cooldownScope: CooldownScope = CooldownScope.USER,
    val cooldownKey: (String, CommandContext) -> String = cooldownScope::formatKey,
    val overridesCooldown: (CommandContext) -> Boolean = { false }
) : ICommand {
    // I love mixing java into this :D
    // TODO: might try to optimize this a bit instead of using JvmOverloads
    // constructor(name: String, help: String) : this(name, help, extraInfo = null)
    constructor(
        name: String,
        help: String,
        category: CommandCategory,
        aliases: Array<String>,
        flags: Array<Flag>
    ) : this(name, help, category = category, null, aliases = aliases, flags = flags)

    // TODO: coroutines
    abstract fun execute(ctx: CommandContext)

    override fun executeCommand(ctx: CommandContext) {
        if (userPermissions.any() && !ctx.member.hasPermission(ctx.channel, userPermissions)) {
            val word = "permission${if (userPermissions.size > 1) "s" else ""}"

            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .setMessageFormat(
                        "You need the `%s` %s for this command\nPlease contact your server administrator if this is incorrect.",
                        userPermissions.toHuman(), word
                    )
                    .build()
            )

            return
        }

        if (botPermissions.any() && !ctx.selfMember.hasPermission(ctx.channel, botPermissions)) {
            val word = "permission${if (botPermissions.size > 1) "s" else ""}"

            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .setMessageFormat(
                        "I need the `%s` %s for this command to work\nPlease contact your server administrator about this.",
                        botPermissions.toHuman(), word
                    )
                    .build()
            )

            return
        }

        if (requiresArgs) {
            val args = ctx.args

            if (args.isEmpty() || args.size < requiredArgCount) {
                sendMsg(ctx, "Missing arguments, usage: ${getUsageInstructions(ctx)}")
                return
            }
        }

        if (cooldown > 0 && !overridesCooldown(ctx) && checkCooldown(ctx)) {
            return
        }

        execute(ctx)
    }

    fun getUsageInstructions(prefix: String, invoke: String) = "`$prefix$invoke${" $usage".replace("{prefix}", prefix).trim { it <= ' ' }}`"

    fun getUsageInstructions(ctx: CommandContext) = this.getUsageInstructions(ctx.prefix, ctx.invoke)

    protected fun sendUsageInstructions(ctx: CommandContext) = sendMsg(ctx, "Usage: " + this.getUsageInstructions(ctx))

    override fun getHelp(invoke: String, prefix: String) = help.replace("{prefix}", prefix)

    override fun shouldDisplayAliasesInHelp() = displayAliasesInHelp

    override fun getName() = name

    override fun getCategory() = category

    override fun getAliases() = aliases

    private fun checkCooldown(ctx: CommandContext): Boolean {
        // Get the cooldown key for this command
        val cooldownKey = cooldownKey(name, ctx)
        val commandManager = ctx.commandManager
        val remainingCooldown = commandManager.getRemainingCooldown(cooldownKey)

        if (remainingCooldown > 0) {
            sendMsg(
                MessageConfig.Builder.fromCtx(ctx)
                    .setMessageFormat(
                        "This command is on cooldown for %s more seconds%s!",
                        remainingCooldown,
                        cooldownScope.extraErrorMsg
                    )
                    .setSuccessAction {
                        it.delete().queueAfter(10, TimeUnit.SECONDS)
                    }
                .build()
            )
            MessageUtils.sendError(ctx.message)
            return true
        }

        // Set the cooldown for the command
        commandManager.setCooldown(cooldownKey, cooldown)

        return false
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BaseCommand) {
            return false
        }

        return name == other.name
    }

    override fun hashCode(): Int {
        var result = requiresArgs.hashCode()
        result = 31 * result + requiredArgCount
        result = 31 * result + displayAliasesInHelp.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + aliases.contentHashCode()
        result = 31 * result + help.hashCode()
        result = 31 * result + usage.hashCode()
        result = 31 * result + (extraInfo?.hashCode() ?: 0)
        result = 31 * result + userPermissions.hashCode()
        result = 31 * result + botPermissions.hashCode()
        result = 31 * result + flags.contentHashCode()
        result = 31 * result + cooldown
        result = 31 * result + cooldownScope.hashCode()
        result = 31 * result + cooldownKey.hashCode()
        result = 31 * result + overridesCooldown.hashCode()
        return result
    }
}
