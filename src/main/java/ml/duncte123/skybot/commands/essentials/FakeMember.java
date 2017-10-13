package ml.duncte123.skybot.commands.essentials;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public class FakeMember implements Member {
    @Override
    public User getUser() {
        return new FakeUser();
    }

    @Override
    public Guild getGuild() {
        return new FakeGuild();
    }

    @Override
    public List<Permission> getPermissions() {
        return null;
    }

    @Override
    public boolean hasPermission(Permission... permissions) {
        return false;
    }

    @Override
    public boolean hasPermission(Collection<Permission> permissions) {
        return false;
    }

    @Override
    public boolean hasPermission(Channel channel, Permission... permissions) {
        return false;
    }

    @Override
    public boolean hasPermission(Channel channel, Collection<Permission> permissions) {
        return false;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public OffsetDateTime getJoinDate() {
        return null;
    }

    @Override
    public GuildVoiceState getVoiceState() {
        return null;
    }

    @Override
    public Game getGame() {
        return null;
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        return null;
    }

    @Override
    public String getNickname() {
        return "duncte123";
    }

    @Override
    public String getEffectiveName() {
        return "duncte123";
    }

    @Override
    public List<Role> getRoles() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public List<Permission> getPermissions(Channel channel) {
        return null;
    }

    @Override
    public boolean canInteract(Member member) {
        return false;
    }

    @Override
    public boolean canInteract(Role role) {
        return false;
    }

    @Override
    public boolean canInteract(Emote emote) {
        return false;
    }

    @Override
    public boolean isOwner() {
        return true;
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel() {
        return null;
    }

    @Override
    public String getAsMention() {
        return "<@" + getUser().getId() + ">";
    }
}
