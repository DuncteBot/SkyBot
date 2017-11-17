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
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public abstract class Command {

    /**
     * This holds the prefix for us
     */
    protected final String PREFIX = Settings.prefix;
    /**
     * This holds the category
     */
    protected CommandCategory category = CommandCategory.MAIN;

    /**
     * Returns the current category of the command
     * @return the current category of the command
     */
    public CommandCategory getCategory() {
        return this.category;
    }

    /**
     * This is the action of the command, this will hold what the commands needs to to
     * @param invoke The command that is ran
     * @param args The command agruments
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    public abstract void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event);

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
     * @param guild the guild that we need the settings for
     * @return the {@link GuildSettings GuildSettings} for the given guild
     */
    protected GuildSettings getSettings(Guild guild) {
        return GuildSettingsUtils.getGuild(guild);
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
        if(!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_EMBED_LINKS)) {
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

    /**
     * This is a shortcut for sending an image inside an embed with using a dynamic url
     * @param event an instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent}
     * @param inStream is the input that will be uploaded.
     * @param filename the filename with ending/type the file has.
     * @see {@link java.io.FileInputStream#FileInputStream(java.io.File)} for getting an InputStream from a file
     * @see {@link java.net.URL#openStream()} for opening an InputStream from an URL
     */
    protected void sendImageAsEmbed(GuildMessageReceivedEvent event, InputStream inStream, String filename) {
        event.getChannel().sendFile(inStream, filename, new MessageBuilder().setEmbed(EmbedUtils.embedImage("attachment://"+filename)).build()).queue();
    }

    @Override
    public String toString() {
        return "Command[" + getName() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Command command = (Command) obj;

        return this.help().equals(command.help()) && this.getName().equals(command.getName());
    }
}
