package ml.duncte123.skybot.commands.essentials;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import java.util.Collection;
import java.util.List;

public class FakeJDA implements JDA {
    @Override
    public Status getStatus() {
        return Status.CONNECTED;
    }

    @Override
    public long getPing() {
        return 0;
    }

    @Override
    public List<String> getCloudflareRays() {
        return null;
    }

    @Override
    public List<String> getWebSocketTrace() {
        return null;
    }

    @Override
    public void setEventManager(IEventManager manager) {

    }

    @Override
    public void addEventListener(Object... listeners) {

    }

    @Override
    public void removeEventListener(Object... listeners) {

    }

    @Override
    public List<Object> getRegisteredListeners() {
        return null;
    }

    @Override
    public SnowflakeCacheView<User> getUserCache() {
        return null;
    }

    @Override
    public List<Guild> getMutualGuilds(User... users) {
        return null;
    }

    @Override
    public List<Guild> getMutualGuilds(Collection<User> users) {
        return null;
    }

    @Override
    public RestAction<User> retrieveUserById(String id) {
        return null;
    }

    @Override
    public RestAction<User> retrieveUserById(long id) {
        return null;
    }

    @Override
    public SnowflakeCacheView<Guild> getGuildCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache() {
        return null;
    }

    @Override
    public SelfUser getSelfUser() {
        return null;
    }

    @Override
    public Presence getPresence() {
        return null;
    }

    @Override
    public ShardInfo getShardInfo() {
        return null;
    }

    @Override
    public String getToken() {
        return "HELL NO";
    }

    @Override
    public long getResponseTotal() {
        return 0;
    }

    @Override
    public int getMaxReconnectDelay() {
        return 0;
    }

    @Override
    public void setAutoReconnect(boolean reconnect) {

    }

    @Override
    public boolean isAutoReconnect() {
        return false;
    }

    @Override
    public boolean isAudioEnabled() {
        return false;
    }

    @Override
    public boolean isBulkDeleteSplittingEnabled() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdownNow() {

    }

    @Override
    public AuditableRestAction<Void> installAuxiliaryCable(int port) {
        return null;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.BOT;
    }

    @Override
    public JDAClient asClient() {
        return null;
    }

    @Override
    public JDABot asBot() {
        return null;
    }
}
