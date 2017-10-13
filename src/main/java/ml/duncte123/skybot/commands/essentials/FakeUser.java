package ml.duncte123.skybot.commands.essentials;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.time.OffsetDateTime;
import java.util.List;

public class FakeUser implements User {
    @Override
    public String getName() {
        return "duncte123";
    }

    @Override
    public String getDiscriminator() {
        return "1245";
    }

    @Override
    public String getAvatarId() {
        return "ed4e799d46e7af262ae3d4d8a722533a";
    }

    @Override
    public String getAvatarUrl() {
        return "https://cdn.discordapp.com/avatars/191231307290771456/ed4e799d46e7af262ae3d4d8a722533a.png";
    }

    @Override
    public String getDefaultAvatarId() {
        return "6debd47ed13483642cf09e832ed0bc1b";
    }

    @Override
    public String getDefaultAvatarUrl() {
        return "https://discordapp.com/assets/6debd47ed13483642cf09e832ed0bc1b.png";
    }

    @Override
    public String getEffectiveAvatarUrl() {
        return getAvatarUrl();
    }

    @Override
    public boolean hasPrivateChannel() {
        return false;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel() {
        return null;
    }

    @Override
    public List<Guild> getMutualGuilds() {
        return null;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public boolean isFake() {
        return false;
    }

    @Override
    public String getAsMention() {
        return "<@" + getId() + ">";
    }

    @Override
    public String getId() {
        return getIdLong() + "";
    }

    @Override
    public long getIdLong() {
        return 191231307290771456L;
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return null;
    }
}
