package ml.duncte123.skybot.commands.essentials;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class FakeGuildMessageReceivedEvent extends GuildMessageReceivedEvent {

    private final Message message;

    public FakeGuildMessageReceivedEvent (JDA api, long responseNumber, Message message) {
        super(api, responseNumber, message);
        this.message = new FakeMessage();
    }

    @Override
    public TextChannel getChannel() {
        return new FakeTextChannel();
    }

    public Guild getGuild() {
        return new FakeGuild();
    }

    public Message getMessage()
    {
        return message;
    }

    public User getAuthor()
    {
        return message.getAuthor();
    }

    public Member getMember()
    {
        return getGuild().getMember(getAuthor());
    }

}
