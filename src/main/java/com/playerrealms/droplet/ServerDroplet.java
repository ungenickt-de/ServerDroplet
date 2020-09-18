package com.playerrealms.droplet;

import com.comphenix.protocol.ProtocolLibrary;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.playerrealms.client.ServerManagerClient;
import com.playerrealms.client.ServerUpdateAdapter;
import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.client.redis.RedisMongoManagerClient;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.commands.PlayerListener;
import com.playerrealms.droplet.commands.ServerAdminCommand;
import com.playerrealms.droplet.commands.VanishCommand;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.lang.LanguagePacketListener;
import com.playerrealms.droplet.menu.RealmManagerMenu;
import com.playerrealms.droplet.menu.RealmMenuPermission;
import com.playerrealms.droplet.menu.VoteConfirmMenu;
import com.playerrealms.droplet.menu.donate.DonateEffectListener;
import com.playerrealms.droplet.menu.donate.GlobalGadgetMenu;
import com.playerrealms.droplet.menu.donate.TrailRunner;
import com.playerrealms.droplet.menu.hub.ServerManagerMenu;
import com.playerrealms.droplet.menu.hub.ServerMenu;
import com.playerrealms.droplet.menu.shop.ConfigureServerShopMenu;
import com.playerrealms.droplet.menu.shop.ServerShop;
import com.playerrealms.droplet.menu.shop.ServerShopMenu;
import com.playerrealms.droplet.rank.Rank;
import com.playerrealms.droplet.rank.RealmPermission;
import com.playerrealms.droplet.rank.SetRankRedis;
import com.playerrealms.droplet.redis.*;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;
import com.playerrealms.droplet.util.PluginData;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerDroplet extends JavaPlugin {

	private static ServerDroplet instance;
	
	protected static ServerManagerClient client;
	
	protected static String name;
	
	private Map<PluginData, Boolean> plugins;
	
	private ServerShop donationShop;

	private RealmMenuPermission menuPermission;
	
	private Map<Integer, Rank> ranks;
	
	private String pastebinKey;

	private static List<String> blockedServerNames;
	
	private MongoClient mongoClient;

	private static final List<SocketPermission> firewall = new ArrayList<>();

	private static final List<String> blacklist = new ArrayList<>();

	
	@Override
	public void onEnable() {
		instance = this;
		name = System.getenv("servername");
		
		if(name == null){
			getLogger().info("No server name was passed to us, shutting down.");
			setEnabled(false);
			return;
		}

		//System.setSecurityManager(new PlayerRealmsSecurity());
		
		ranks = new HashMap<>();
		
		try {
			saveDefaultConfig();
			YamlConfiguration config = downloadConfig();
			
			File configFile = new File(getDataFolder(), "config.yml");
			
			config.save(configFile);
			
			getConfig().load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		
		JedisAPI.setup(getConfig().getString("redis_new"), 6379, getConfig().getString("redis_password"));

		mongoClient = new MongoClient(getConfig().getString("mongo"), 27017);
		
		client = new RedisMongoManagerClient(new JedisInterface(), mongoClient.getDatabase("playerrealms"));
		client.connect("", 0);
		
		File shopConfig = new File(getDataFolder(), "shop.yml");

		if(!shopConfig.exists()){
			try {
				shopConfig.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		donationShop = new ServerShop(YamlConfiguration.loadConfiguration(shopConfig), toSave -> {
			try {
				toSave.save(shopConfig);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		File permissionConfig = new File(getDataFolder(), "allowed.yml");

		if(!permissionConfig.exists()){
			try{
				permissionConfig.createNewFile();
			}catch (IOException e){
				e.printStackTrace();
			}
		}

		menuPermission = new RealmMenuPermission(YamlConfiguration.loadConfiguration(permissionConfig), toSave -> {
			try {
				toSave.save(permissionConfig);
			}catch (IOException e){
				e.printStackTrace();
			}
		});
		
		pastebinKey = getConfig().getString("pastebin");

		blockedServerNames = getConfig().getStringList("blocked");
		
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		getServer().getPluginManager().registerEvents(new VanishCommand(), this);
		
		client.addListener(new DropletAPI.ResponseListener());
		client.addListener(new ServerUpdateAdapter() {
			@Override
			public void onDisconnectFromServerManager() {
				getLogger().info("Disconnected from server manager, shutting down.");
				Bukkit.shutdown();
			}
		});
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Lag(), 0, 1);
		Bukkit.getScheduler().runTask(this, new Runnable() {
			@Override
			public void run() {
				DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "STATUS", ServerStatus.ONLINE.name());
			}
		});

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TrailRunner(), 100, 3);
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				client.sendUpdateServerPlayers(name, Bukkit.getOnlinePlayers().size());
			}
		}, 100, 200);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "tps", String.valueOf((int) Lag.getTPS()));
				
				if(Bukkit.hasWhitelist()){
					DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "whitelist", "true");
				}else{
					DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "whitelist", "");
				}
			}
			
		}, 100, 200);
		
		if(Bukkit.getPluginManager().isPluginEnabled("Skellett")) {
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Skellett"));
		}
		
		Language.registerLanguage(getTextResource("lang/en_US.yml"), "en_us");
		Language.registerLanguage(getTextResource("lang/ja_JP.yml"), "ja_jp");
		Language.registerLanguage(getTextResource("lang/lol_US.yml"), "lol_us");
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		if(DropletAPI.getThisServer().getServerType() == ServerType.HUB){
			getCommand("sadmin").setExecutor(new ServerAdminCommand());
			/*getCommand("prban").setExecutor(new PlayerRealmBanCommand());*/
		}
		
/*		if(DropletAPI.getThisServer().getServerType() == ServerType.PLAYER) {
			Policy.setPolicy(new PlayerRealmsPluginPolicy());
			System.setSecurityManager(new PlayerRealmsSecurity());
		}*/
		
		if(DropletAPI.getThisServer().getServerType() == ServerType.PLAYER){
			Bukkit.getScheduler().runTaskTimerAsynchronously(this, new PlayerChecker(), 20 * 60, 20);
			
			Bukkit.getScheduler().runTaskTimer(this, () -> {
				
				if(DropletAPI.getThisServer().isPremium()) {
					return;
				}
				
				if(Bukkit.getOnlinePlayers().size() < 2) {
					if(System.currentTimeMillis() - DropletAPI.getThisServer().getStartTime() > TimeUnit.MINUTES.toMillis(45)) {
						for(Player pl : Bukkit.getOnlinePlayers()) {
							Language.sendMessage(pl, "closing");
						}
						Bukkit.getScheduler().runTaskLater(this, () -> Bukkit.shutdown(), 20 * 10);
					}
				}
			}, 20 * 2700, 20 * 2700);

		}
		
		Bukkit.getPluginManager().registerEvents(new DonateEffectListener(), this);
		
		//getCommand("server").setExecutor(new ServerCommand());
		
		plugins = new HashMap<>();
		
		loadPlugins();
		
		getLogger().info("Using redis from "+getConfig().getString("redis_new"));
		
		if(JedisAPI.isValid()){
			getLogger().info("Connected to Redis.");
		}else{
			getLogger().severe("Failed to connect to Redis.");
		}
		
		DatabaseAPI.setup("jdbc:mysql://"+getConfig().getString("mysql.remote_ip")+"/playerrealms?useUnicode=yes&characterEncoding=UTF-8", getConfig().getString("mysql.remote_password"));
			
		try {
			if(DatabaseAPI.isValid()){
				getLogger().info("Created database pool");
				loadRanks();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().severe("Failed to connect to MySQL");
			Bukkit.shutdown();
			return;
		}
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new LanguagePacketListener(this));
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Announcer("announcer.vote", "announcer.donate", "announcer.premium", "announcer.ultra", "announcer.coins", "announcer.amazon", "announcer.twitter", "announcer.rule"), 100, 20 * 100);
		
		DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "st", String.valueOf(System.currentTimeMillis()));
		
		JedisAPI.registerListener(new JedisListener() {
			
			@Override
			public void onMessage(String ch, String message) {
				try {
					loadRanks();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public String[] getChannel() {
				return new String[] {"rankReload"};
			}
		});

		JedisAPI.registerListener(new JedisListener() {
			@Override
			public void onMessage(String channel, String message) {
				try{
					loadFirewall();
					loadBlacklist();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public String[] getChannel() { return new String[] {"listReload"}; }
		});

		JedisAPI.registerListener(new PeakTimeListener());
		JedisAPI.registerListener(new GiveCoinsListener());
		JedisAPI.registerListener(new SetRankRedis());
		JedisAPI.registerListener(new MessageListener());
		
		if(DropletAPI.getThisServer().getServerType() != ServerType.HUB) {
			JedisAPI.registerListener(new GlobalBanListener());
		}
		
		Bukkit.getPluginManager().registerEvents(new DropletListener(), this);
		
		if(!DropletAPI.getThisServer().getMetadata().containsKey("uuid")) {
			DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "uuid", UUID.randomUUID().toString());
		}

		try {
			loadFirewall();
			loadBlacklist();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean checkFirewalls(SocketPermission sp){
		if(firewall.contains(sp)){
			return true;
		}
		return false;
	}

	public void loadFirewall() throws IOException{
		firewall.clear();
		InputStream is;
		try{
			URLConnection conn = DropletAPI.openApiConnection("firewall.txt");
			is = conn.getInputStream();
		}catch (Exception e){
			is = ServerDroplet.getInstance().getResource("firewall.txt");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = reader.readLine()) != null) {
			getLogger().info("Firewall: " + line);
			firewall.add(new SocketPermission(line, "resolve"));
			try {
				InetAddress[] list = Inet4Address.getAllByName(line);
				for (InetAddress dns : list) {
					firewall.add(new SocketPermission(dns.getHostAddress() + ":80", "connect,resolve"));
					firewall.add(new SocketPermission(dns.getHostAddress() + ":443", "connect,resolve"));
				}
			} catch (UnknownHostException e) {
			}
			firewall.add(new SocketPermission(line + ":80", "connect,resolve"));
			firewall.add(new SocketPermission(line + ":443", "connect,resolve"));
		}
	}

	public static boolean checkBlacklist(String sp){
		if(blacklist.contains(sp)){
			return true;
		}
		return false;
	}

	/*
	 * File Blacklist
	 */
	public void loadBlacklist() throws IOException{
		blacklist.clear();
		InputStream is;
		try{
			URLConnection conn = DropletAPI.openApiConnection("blacklist_file.txt");
			is = conn.getInputStream();
		}catch (Exception e){
			is = ServerDroplet.getInstance().getResource("blacklist_file.txt");
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = reader.readLine()) != null){
			getLogger().info("File: "+line);
			blacklist.add(line);
		}
	}

	@Override
	public FileConfiguration getConfig() {
		return super.getConfig();
	}
	
	public static YamlConfiguration downloadConfig() throws IOException, InvalidConfigurationException {
		URLConnection con = DropletAPI.openApiConnection("");
		
		YamlConfiguration config = new YamlConfiguration();
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))){
			config.load(reader);
		}
		
		return config;
	}
	
	public static String getPastebinKey(){
		return getInstance().pastebinKey;
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new VoidGenerator();
	}
	
	public Rank getRank(int id){
		return ranks.get(id);
	}
	
	protected void loadRanks() throws SQLException{
		ranks.clear();
		
		try{
			List<QueryResult> ranksResults = DatabaseAPI.query("SELECT * FROM `ranks`");
			
			for(QueryResult result : ranksResults){
				int id = result.get("id");
				String name = result.get("name");
				int power = result.get("power");
				String prefix = result.get("prefix");
				int child = -1;
				int tabPriority = result.get("tab_priority");
				
				if(result.hasKey("child")){
					child = result.get("child");
				}
				
				List<RealmPermission> permissions;
				
				List<QueryResult> permissionsResults = DatabaseAPI.query("SELECT `permission`,`global` FROM `permissions` WHERE `rank`=?", id);
				
				permissions = permissionsResults.stream().map(r -> new RealmPermission(r.get("permission"), (int) r.get("global") == 1)).collect(Collectors.toList());
				
				ranks.put(id, new Rank(id, power, name, child, prefix, tabPriority, permissions));
				
				getLogger().info("Loaded rank "+id+" => "+name+" permissions:  "+permissions.size());
			}
			
			getLogger().info("Loaded "+ranks.size()+" ranks.");
		
			for(Player pl : Bukkit.getOnlinePlayers()){
				DropletAPI.getRank(pl).updatePermissionAttachement(pl);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		

	}
	
	@Override
	public void onDisable() {
		client.sendUpdateServerPlayers(name, 0);
		
		client.shutdown();
		
		donationShop.save();

		for(PluginData data : plugins.keySet()){
			
			boolean enabled = plugins.get(data);
			
			if(enabled){
				try {
					FileUtils.copyFileToDirectory(data.getFile(), getDataFolder().getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				try {
					File file = new File(getDataFolder().getParentFile(), data.getFile().getName());
					if(file.exists())
						FileUtils.forceDelete(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		getLogger().info("Shutting down jedis.");
		JedisAPI.destroy();
		getLogger().info("Shutdown jedis");
		getLogger().info("Shutting down Mongo.");
		mongoClient.close();
		getLogger().info("Shut down Mongo.");
	}
	
	public Map<PluginData, Boolean> getPlugins(){
		return plugins;
	}
	
	private void loadPlugins(){
		List<File> plugins = findPlugins();
		try {
			PluginData.loadExclusives();
			PluginData.loadHidden();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		File bukkitPluginsFolder = getDataFolder().getParentFile();
		for(File pluginFile : plugins){
			String fileName = pluginFile.getName();
			File child = new File(bukkitPluginsFolder, fileName);
			try{
				PluginData generated = PluginData.generate(pluginFile);
				if(generated == null){
					continue;
				}
				if(child.exists()){
					this.plugins.put(generated, true);
				}else{
					this.plugins.put(generated, false);
				}
			}catch(Exception e){
				e.printStackTrace();
				getLogger().severe("Failed to load plugin "+pluginFile.getName());
			}
		}
	}
	
	private List<File> findPlugins(){
		File dataFolder = new File("/plugins");
		if(!dataFolder.exists()){
			return Collections.emptyList();
		}
		List<File> plugins = new ArrayList<>();
		for(File pluginFile : dataFolder.listFiles()) {
			if(pluginFile.getName().endsWith(".jar")) {
				plugins.add(pluginFile);
			}
		}
		return plugins;
	}
	
	private void openRealmManager(Player player, boolean override){
		if(DropletAPI.getThisServer().getOwner() != null){
			UUID owner = DropletAPI.getThisServer().getOwner();
			if(!(player.getUniqueId().equals(owner) || getMenuPermission().checkUUID(player.getUniqueId().toString())) && !override){
				ServerShopMenu shopMenu = new ServerShopMenu(getShop(), player);
				shopMenu.open(player);
				return;
			}
			RealmManagerMenu menu = new RealmManagerMenu(player);
			menu.open(player);
		}
	}
	
	private void openRealmShop(Player player){
		if(player.getUniqueId().equals(DropletAPI.getThisServer().getOwner())){
			ConfigureServerShopMenu editMenu = new ConfigureServerShopMenu(player);
			editMenu.open(player);
		}else if(getShop().getItems().size() > 0){
			
			ServerShopMenu menu = new ServerShopMenu(getShop(), player);
			
			menu.open(player);
		}else{
			Language.sendMessage(player, "shop.no_items");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if((command.getName().equals("island") || command.getName().equals("realm")) && sender instanceof Player){
			Player pl = (Player) sender;
			boolean override = false;
			if(args.length > 0){
				if(args[0].equals("override")){
					if(DropletAPI.getRank(pl).hasPermission("playerrealms.manage")){
						override = true;
					}
				}
			}
			if(DropletAPI.getThisServer().getServerType() != ServerType.PLAYER){
				if(override){
					RealmManagerMenu menu = new RealmManagerMenu(pl);
					menu.open(pl);
				}else{
					if(DropletAPI.getThisServer().getServerType() == ServerType.HUB){
						Player player = (Player) sender;
						List<ServerInformation> ownedServers = DropletAPI.getByOwner(player);
						if(ownedServers.size() == 1){
							ServerMenu menu = new ServerMenu(ownedServers.get(0), player);
							menu.open(player);
						}else{
							ServerManagerMenu menu = new ServerManagerMenu(player);
							menu.open(player);
						}
					}
				}
				return true;
			}
			if(DropletAPI.getThisServer().getOwner() != null){
				openRealmManager(pl, override);
				return true;
			}
		}else if(command.getName().equals("kifumenu") && sender instanceof Player) {
			Player pl = (Player) sender;
			if(DropletAPI.getRank(pl).hasPermission("droplet.funmenu")) {
				GlobalGadgetMenu d = new GlobalGadgetMenu(pl);
				d.open(pl);
			}else {
				Language.sendMessage(pl, "donate_powers.not_donator");
			}
		}else if(command.getName().equals("hub") && sender instanceof Player){
			Player pl = (Player) sender;
			if(JedisAPI.keyExists("cname_connect."+((Player) sender).getUniqueId())){
				sender.sendMessage(Language.getText((Player) sender, "jump_server_deny"));
				return true;
			}
			if(DropletAPI.getThisServer().getServerType() == ServerType.HUB){
				Language.sendMessage(pl, "connect.already_in_hub");
			}else{
				ServerInformation hub = DropletAPI.getHub(pl);
				DropletAPI.connectToServer(pl, hub);
			}
		}else if(command.getName().equals("sadmin") && sender instanceof Player){
			Player pl = (Player) sender;
			openRealmManager(pl, false);
		}else if(command.getName().equals("store")){
			Player pl = (Player) sender;
			openRealmShop(pl);
		}else if(command.getName().equals("kifuchat")) {
			if(!(sender instanceof Player)){
				return false;
			}
			Player pl = (Player) sender;
			boolean isReport = command.getName().equals("pireport");
			boolean isDonorChat = command.getName().equals("kifuchat");
			String redisChannel = "";
			if(args.length == 0){
				return false;
			}
			if(isReport) {
				long last = 0;
				if(JedisAPI.keyExists("lastreport."+pl.getUniqueId())) {
					last = Long.parseLong(JedisAPI.getCachedValue("lastreport."+pl.getUniqueId(), 60000));
				}
				if(System.currentTimeMillis() - last < TimeUnit.MINUTES.toMillis(10)) {
					Language.sendMessage(pl, "report.wait", (System.currentTimeMillis() - last) / 1000);
					return true;
				}
				redisChannel = "report";
			}else if(isDonorChat){
				if(!DropletAPI.getRank(pl).hasPermission("playerrealms.donorchat")) {
					Language.sendMessage(pl, "donor_chat.perm");
					return true;
				}
				redisChannel = "donorchat";
			}else {
				if(!DropletAPI.getRank(pl).hasPermission("playerrealms.adminchat")) {
					Language.sendMessage(pl, "report.perm");
					return true;
				}
				redisChannel = "adminchat";
			}
			String msg = "";
			for(int i = 0; i < args.length;i++) {
				msg += args[i] + " ";
			}
			msg = msg.substring(0, msg.length() - 1);
			if(isDonorChat){
				if(msg.equals("off")){
					JedisAPI.setKey("donatorchat.toggle."+pl.getUniqueId(), "off");
					Language.sendMessage(pl, "donor_chat.turn_off");
					return true;
				}else if(msg.equals("on")){
					JedisAPI.removeKey("donatorchat.toggle."+pl.getUniqueId());
					Language.sendMessage(pl, "donor_chat.turn_on");
					return true;
				}
			}
			String oldmsg = msg;
			msg = ChatColor.translateAlternateColorCodes('&', msg);
			if(msg == null){
				msg = oldmsg;
			}
			boolean pf = true;
            if(JedisAPI.keyExists("hiderank.toggle."+pl.getUniqueId())){
                pf = false;
                JedisAPI.cacheKey("hiderank.toggle."+pl.getUniqueId(), 60000);
            }
            if(pf) {
                JedisAPI.publish(redisChannel, DropletAPI.getThisServer().getName() + " " + DropletAPI.getRank(pl).getPlayerName(pl.getName()) + ChatColor.GRAY + " " + msg);
            }else{
                JedisAPI.publish(redisChannel, DropletAPI.getThisServer().getName() + " " + pl.getName() + ChatColor.GRAY + " " + msg);
            }
			/*if(isReport) {
				Language.sendMessage(pl, "report.sent");
				JedisAPI.setKey("lastreport."+pl.getUniqueId(), String.valueOf(System.currentTimeMillis()));
			}*/
			return true;
		}else if(command.getName().equals("pivote")) {
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				String jedisKey = "servervote." + player.getUniqueId() + ".time";
				if (JedisAPI.keyExists(jedisKey))
				{
					long time = Long.parseLong(JedisAPI.getValue(jedisKey));
					if (System.currentTimeMillis() - time < TimeUnit.DAYS.toMillis(1))
					{
						Language.sendMessage(player, "hub.vote.time");
						return true;
					}
				}
				new VoteConfirmMenu(player).open(player);
			} else {
				sender.sendMessage(ChatColor.RED + "Player only.");
			}
			return true;
		}else if(command.getName().equals("hiderank")) {
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				if(!DropletAPI.getRank(player).hasPermission("playerrealms.hiderank")) {
					Language.sendMessage(player, "hiderank.perm");
					return true;
				}
				String ToggleKey = "hiderank.toggle." + player.getUniqueId();
				if (JedisAPI.keyExists(ToggleKey))
				{
					JedisAPI.removeKey("hiderank.toggle." + player.getUniqueId());
					Language.sendMessage(player, "hiderank.turn_on");
					return true;
				}
				JedisAPI.setKey("hiderank.toggle."+player.getUniqueId(), "off");
                JedisAPI.cacheKey("hiderank.toggle."+player.getUniqueId(), 300);
				Language.sendMessage(player, "hiderank.turn_off");
			} else {
				sender.sendMessage(ChatColor.RED + "Player only.");
			}
			return true;
		}else if(command.getName().equals("vanish")) {
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				if (!DropletAPI.getRank(player).hasPermission("playerrealms.vanish")) {
					Language.sendMessage(player, "vanish.perm");
					return true;
				}
				if (VanishCommand.isVanished(player)) {
					VanishCommand.showPlayer(player);
					return true;
				}
				VanishCommand.vanishPlayer(player);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "Player only.");
			}
			return true;
		}
		return true;
	}

	public static ServerDroplet getInstance() {
		return instance;
	}

	public ServerShop getShop() {
		return donationShop;
	}

	public RealmMenuPermission getMenuPermission() {
		return menuPermission;
	}

	public Rank getRank(String rankName) {
		for(Rank rank : ranks.values()){
			if(rank.getName().equalsIgnoreCase(rankName)){
				return rank;
			}
		}
		return null;
	}
	
	public MongoDatabase getDatabase() {
		return mongoClient.getDatabase("playerrealms");
	}

	public static List<String> getBlockedServerNames() {
		return blockedServerNames;
	}

	public static boolean isBackblazeLoaded() {
		return Bukkit.getPluginManager().isPluginEnabled("Backblaze");
	}

	public static boolean isClientActive() {
		if(JedisAPI.isValid() && DatabaseAPI.isValid()){
			return true;
		}else {
			return false;
		}
	}
}
