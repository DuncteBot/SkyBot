package ml.duncte123.skybot.objects.delegate;

import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.Presence;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.GuildAction;
import net.dv8tion.jda.core.utils.cache.CacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

public class JDADelegate
implements JDA {

	private JDA __$__$$e__0_$0;

	public JDADelegate(JDA __$__$$e__0_$0) {
		this.__$__$$e__0_$0 = __$__$$e__0_$0;
	}

	public Status getStatus() {
		return __$__$$e__0_$0.getStatus();
	}

	public long getPing() {
		return __$__$$e__0_$0.getPing();
	}

	public List<String> getCloudflareRays() {
		return __$__$$e__0_$0.getCloudflareRays();
	}

	public List<String> getWebSocketTrace() {
		return __$__$$e__0_$0.getWebSocketTrace();
	}

	public void setEventManager(IEventManager manager) {
		__$__$$e__0_$0.setEventManager(manager);
	}

	public void addEventListener(Object... listeners) {
		__$__$$e__0_$0.addEventListener(listeners);
	}

	public void removeEventListener(Object... listeners) {
		__$__$$e__0_$0.removeEventListener(listeners);
	}

	public List<Object> getRegisteredListeners() {
		return __$__$$e__0_$0.getRegisteredListeners();
	}

	public List<User> getUsers() {
		return __$__$$e__0_$0.getUsers();
	}

	public User getUserById(String id) {
		return __$__$$e__0_$0.getUserById(id);
	}

	public User getUserById(long id) {
		return __$__$$e__0_$0.getUserById(id);
	}

	public List<Guild> getMutualGuilds(User... users) {
		return __$__$$e__0_$0.getMutualGuilds(users);
	}

	public List<Guild> getMutualGuilds(Collection<User> users) {
		return __$__$$e__0_$0.getMutualGuilds(users);
	}

	public List<User> getUsersByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getUsersByName(name, ignoreCase);
	}

	public RestAction<User> retrieveUserById(String id) {
		return __$__$$e__0_$0.retrieveUserById(id);
	}

	public RestAction<User> retrieveUserById(long id) {
		return __$__$$e__0_$0.retrieveUserById(id);
	}

	public List<Guild> getGuilds() {
		return __$__$$e__0_$0.getGuilds();
	}

	public Guild getGuildById(String id) {
		return __$__$$e__0_$0.getGuildById(id);
	}

	public Guild getGuildById(long id) {
		return __$__$$e__0_$0.getGuildById(id);
	}

	public List<Guild> getGuildsByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getGuildsByName(name, ignoreCase);
	}

	public List<Role> getRoles() {
		return __$__$$e__0_$0.getRoles();
	}

	public Role getRoleById(String id) {
		return __$__$$e__0_$0.getRoleById(id);
	}

	public Role getRoleById(long id) {
		return __$__$$e__0_$0.getRoleById(id);
	}

	public List<Role> getRolesByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getRolesByName(name, ignoreCase);
	}

	public List<TextChannel> getTextChannels() {
		return __$__$$e__0_$0.getTextChannels();
	}

	public TextChannel getTextChannelById(String id) {
		return __$__$$e__0_$0.getTextChannelById(id);
	}

	public TextChannel getTextChannelById(long id) {
		return __$__$$e__0_$0.getTextChannelById(id);
	}

	public List<TextChannel> getTextChannelsByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getTextChannelsByName(name, ignoreCase);
	}

	public List<VoiceChannel> getVoiceChannels() {
		return __$__$$e__0_$0.getVoiceChannels();
	}

	public VoiceChannel getVoiceChannelById(String id) {
		return __$__$$e__0_$0.getVoiceChannelById(id);
	}

	public VoiceChannel getVoiceChannelById(long id) {
		return __$__$$e__0_$0.getVoiceChannelById(id);
	}

	public List<VoiceChannel> getVoiceChannelByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getVoiceChannelByName(name, ignoreCase);
	}

	public List<PrivateChannel> getPrivateChannels() {
		return __$__$$e__0_$0.getPrivateChannels();
	}

	public PrivateChannel getPrivateChannelById(String id) {
		return __$__$$e__0_$0.getPrivateChannelById(id);
	}

	public PrivateChannel getPrivateChannelById(long id) {
		return __$__$$e__0_$0.getPrivateChannelById(id);
	}

	public List<Emote> getEmotes() {
		return __$__$$e__0_$0.getEmotes();
	}

	public Emote getEmoteById(String id) {
		return __$__$$e__0_$0.getEmoteById(id);
	}

	public Emote getEmoteById(long id) {
		return __$__$$e__0_$0.getEmoteById(id);
	}

	public List<Emote> getEmotesByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getEmotesByName(name, ignoreCase);
	}

	public SelfUser getSelfUser() {
		return __$__$$e__0_$0.getSelfUser();
	}

	public Presence getPresence() {
		return __$__$$e__0_$0.getPresence();
	}

	public ShardInfo getShardInfo() {
		return __$__$$e__0_$0.getShardInfo();
	}

	public String getToken() {
	    // Token must be changed
		return __$__$$e__0_$0.getToken()
				.replace('.', '-')
				.replace('k', 'M')
				.replaceAll("[n-zA-M]", "p");
	}

	public long getResponseTotal() {
		return __$__$$e__0_$0.getResponseTotal();
	}

	public int getMaxReconnectDelay() {
		return __$__$$e__0_$0.getMaxReconnectDelay();
	}

	public void setAutoReconnect(boolean reconnect) {
		__$__$$e__0_$0.setAutoReconnect(reconnect);
	}

	public boolean isAutoReconnect() {
		return __$__$$e__0_$0.isAutoReconnect();
	}

	public boolean isAudioEnabled() {
		return __$__$$e__0_$0.isAudioEnabled();
	}

	public boolean isBulkDeleteSplittingEnabled() {
		return __$__$$e__0_$0.isBulkDeleteSplittingEnabled();
	}

	public void shutdown() {
		__$__$$e__0_$0.shutdown();
	}

	public void shutdownNow() {
		__$__$$e__0_$0.shutdownNow();
	}

	public AuditableRestAction<Void> installAuxiliaryCable(int port) {
		return __$__$$e__0_$0.installAuxiliaryCable(port);
	}

	public AccountType getAccountType() {
		return __$__$$e__0_$0.getAccountType();
	}

	public JDAClient asClient() {
		return __$__$$e__0_$0.asClient();
	}

	public JDABot asBot() {
		return __$__$$e__0_$0.asBot();
	}

	@Override
	public List<Category> getCategories() {
		return __$__$$e__0_$0.getCategories();
	}

	@Override
	public List<Category> getCategoriesByName(String name, boolean ignoreCase) {
		return __$__$$e__0_$0.getCategoriesByName(name, ignoreCase);
	}

	@Override
	public Category getCategoryById(String id) {
		return __$__$$e__0_$0.getCategoryById(id);
	}

	@Override
	public Category getCategoryById(long id) {
		return __$__$$e__0_$0.getCategoryById(id);
	}

	@Override
	public SnowflakeCacheView<User> getUserCache() {
		return __$__$$e__0_$0.getUserCache();
	}

	@Override
	public SnowflakeCacheView<Guild> getGuildCache() {
		return __$__$$e__0_$0.getGuildCache();
	}

	@Override
	public SnowflakeCacheView<Role> getRoleCache() {
		return __$__$$e__0_$0.getRoleCache();
	}

	@Override
	public SnowflakeCacheView<Category> getCategoryCache() {
		return __$__$$e__0_$0.getCategoryCache();
	}

	@Override
	public SnowflakeCacheView<TextChannel> getTextChannelCache() {
		return __$__$$e__0_$0.getTextChannelCache();
	}

	@Override
	public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
		return __$__$$e__0_$0.getVoiceChannelCache();
	}

	@Override
	public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
		return __$__$$e__0_$0.getPrivateChannelCache();
	}

	@Override
	public SnowflakeCacheView<Emote> getEmoteCache() {
		return __$__$$e__0_$0.getEmoteCache();
	}

    @Override
    public GuildAction createGuild(String name) {
        return __$__$$e__0_$0.createGuild(name);
    }

    @Override
    public CacheView<AudioManager> getAudioManagerCache() {
        return __$__$$e__0_$0.getAudioManagerCache();
    }

    @Override
    public void setRequestTimeoutRetry(boolean retryOnTimeout) {
        __$__$$e__0_$0.setRequestTimeoutRetry(retryOnTimeout);
    }
}
