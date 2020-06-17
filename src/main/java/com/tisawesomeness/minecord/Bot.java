package com.tisawesomeness.minecord;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.discordbots.api.client.DiscordBotListAPI;

import com.tisawesomeness.minecord.command.Registry;
import com.tisawesomeness.minecord.database.Database;
import com.tisawesomeness.minecord.database.VoteHandler;
import com.tisawesomeness.minecord.item.Item;
import com.tisawesomeness.minecord.item.Recipe;
import com.tisawesomeness.minecord.setting.SettingRegistry;
import com.tisawesomeness.minecord.util.ColorUtils;
import com.tisawesomeness.minecord.util.DateUtils;
import com.tisawesomeness.minecord.util.DiscordUtils;
import com.tisawesomeness.minecord.util.MessageUtils;
import com.tisawesomeness.minecord.util.RequestUtils;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;

@NoArgsConstructor
public class Bot {

	public static final String author = "Tis_awesomeness";
	public static final String authorTag = "@Tis_awesomeness#8617";
	public static final String helpServer = "https://minecord.github.io/support";
	public static final String website = "https://minecord.github.io";
	public static final String github = "https://github.com/Tisawesomeness/Minecord";
	public static final String version = "0.10.0";
	public static final String javaVersion = "1.8";
	public static final String jdaVersion = "4.1.1_151";
	public static final Color color = Color.GREEN;

	private static final List<GatewayIntent> gateways = Arrays.asList(
			GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);
	private static final EnumSet<CacheFlag> disabledCacheFlags = EnumSet.of(
			CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.VOICE_STATE);

	private long id;
	private Listener listener;
	private ReactListener reactListener;
	private ReadyListener readyListener;
	public String[] args;
	private Thread thread;
	@Getter private SettingRegistry settings;
	@Getter private ShardManager shardManager;
	@Getter private VoteHandler voteHandler;
	@Getter private long birth;
	@Getter private long bootTime;
	@Setter private PersistPackage pack = null;

	private volatile int readyShards = 0;
	
	public boolean setup(String[] args, boolean devMode) {
		long startTime = System.currentTimeMillis();
		if (!devMode) {
			System.out.println("Bot starting...");
		}
		
		//Parse arguments
		this.args = args;
		Config.read(this, false);
		if (Config.getDevMode() && !devMode) return false;
		boolean reload = false;
		if (args.length > 0 && Arrays.asList(args).contains("-r")) reload = true;
		
		//Pre-init
		thread = Thread.currentThread();
		listener = new Listener(this);
		reactListener = new ReactListener();
		readyListener = new ReadyListener(this);
		try {
			Announcement.init(Config.getPath());
			ColorUtils.init(Config.getPath());
			Item.init(Config.getPath());
			Recipe.init(Config.getPath());
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		ReactMenu.startPurgeThread();
		Registry.init();
		
		// Connect to database
		Future<Boolean> db = Database.start();
		
		//Fetch main class
		try {
				
			//If this is the first run
			birth = startTime;

			//Initialize JDA
			shardManager = DefaultShardManagerBuilder.create(gateways)
				.setToken(Config.getClientToken())
				.setAutoReconnect(true)
				.addEventListeners(listener, reactListener, readyListener)
				.setShardsTotal(Config.getShardCount())
				.setActivity(Activity.playing("Loading..."))
				.setMemberCachePolicy(MemberCachePolicy.NONE)
				.disableCache(disabledCacheFlags)
				.build();

			// Wait for shards to ready
			while (readyShards < shardManager.getShardsTotal()) {
				//System.out.println("Ready shards: " + readyShards + " / " + shardManager.getShardsTotal());
				Thread.sleep(100);
			}
			System.out.println("Shards ready");

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
		//Start discordbots.org API
		if (Config.getSendServerCount() || Config.getReceiveVotes()) {
			RequestUtils.api = new DiscordBotListAPI.Builder().token(Config.getOrgToken()).build();
		}

		// Wait for database
		try {
			if (!db.get()) {
				return false;
			}
		} catch (InterruptedException | ExecutionException ex) {
			return false;
		}

		// Create settings
		settings = new SettingRegistry();

		System.out.println("Bot ready!");

		// Start web server
		voteHandler = new VoteHandler(shardManager);
		Future<Boolean> ws = voteHandler.start();

		//Update persistent bot info
		if (!Config.getLogChannel().equals("0")) {
			MessageUtils.logChannel = shardManager.getTextChannelById(Config.getLogChannel());
		}
		
		//Post-init
		bootTime = System.currentTimeMillis() - startTime;
		System.out.println("Boot Time: " + DateUtils.getBootTime(bootTime));
		MessageUtils.log(":white_check_mark: **Bot started!**");
		DiscordUtils.update(shardManager);
		RequestUtils.sendGuilds(shardManager);
		
		return true;
		
	}

	public void addReadyShard() {
		readyShards++;
	}
	
	public void shutdown(Message m, User u) {
		
		//Disable JDA
		for (JDA jda : shardManager.getShards()) {
			jda.setAutoReconnect(false);
			jda.removeEventListener(listener, reactListener, readyListener);
		}
		
		//Stop the thread
		thread.interrupt();

	}

}
