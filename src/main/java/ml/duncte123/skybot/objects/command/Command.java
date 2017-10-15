/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {

    /**
     * This is the action of the command, this will hold what the commands needs to to
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    public abstract void executeCommand(String[] args, GuildMessageReceivedEvent event);

    /**
     * The usage instructions of the command
     * @return a String
     */
    public abstract String help();

    /**
     * This will hold the command name aka what the user puts after the prefix
     * @return The command name
     */
    public abstract String getName();

    /**
     * This wil hold any aliases that this command might have
     * @return the current aliases for the command if set
     */
    public String[] getAliases() {
        return new String[0];
    }

    /**
     * This returns the settings for the given guild
     * @param guildId the id if the guild that we need the settings for
     * @return the {@link ml.duncte123.skybot.objects.guild.GuildSettings GuildSettings} for the given guild
     */
    protected GuildSettings getSettings(String guildId) {
        return AirUtils.guildSettings.get(guildId);
    }

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command
     * @param message the message to add the reaction to
     */
    protected void sendError(Message message)
    {
        message.addReaction("❌").queue();
    }

    /**
     * This will react with a ✅ if the user doesn't have permission to run the command
     * @param message the message to add the reaction to
     */
    protected void sendSuccess(Message message)
    {
        message.addReaction("✅").queue();
    }

    /**
     * This will chcek if we can send a embed and convert it to a message if we can't send embeds
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param embed The embed to send
     */
    protected void sendEmbed(GuildMessageReceivedEvent event, MessageEmbed embed) {
        if(!event.getGuild().getSelfMember().hasPermission( Permission.MESSAGE_EMBED_LINKS)) {
            sendMsg(event, EmbedUtils.embedToMessage(embed));
            return;
        }
        sendMsg(event, embed);
    }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    protected void sendMsg(GuildMessageReceivedEvent event, String msg) {
       sendMsg(event, (new MessageBuilder()).append(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    protected void sendMsg(GuildMessageReceivedEvent event, MessageEmbed msg) {
        sendMsg(event, (new MessageBuilder()).setEmbed(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    protected void sendMsg(GuildMessageReceivedEvent event, Message msg) {
        event.getChannel().sendMessage(msg).queue();
    }

    @Override
    public String toString() {
        return "Command[" + getName() + "]";
    }
}
