package ml.duncte123.skybot.objects.JDA;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class JDADelagteGuildMessageReceivedEvent extends GuildMessageReceivedEvent {

    private final Message message;

    public JDADelagteGuildMessageReceivedEvent(JDA api, long responseNumber, Message message) {
        super(api, responseNumber, message);
        this.message = message;
    }

    @Override
    public JDA getJDA() {
        return new JDADelegate(super.getJDA());
    }

    public Message getMessage()  {
        return message;
    }

    public User getAuthor() {
        return message.getAuthor();
    }

    public Member getMember() {
        return getGuild().getMember(getAuthor());
    }
}
