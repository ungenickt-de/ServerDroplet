package com.playerrealms.droplet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.playerrealms.client.NoAvailableServerException;
import com.playerrealms.client.ServerUpdateAdapter;
import com.playerrealms.client.ServerUpdateListener;
import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.menu.donate.Trail;
import com.playerrealms.droplet.rank.Rank;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DropletAPI {

	private DropletAPI() { }

	public static final int BAN_TYPE_PLAY = 1;
	public static final int BAN_TYPE_CREATE = 2;
	public static final int BAN_TYPE_BOTH = BAN_TYPE_PLAY | BAN_TYPE_CREATE;
	
	private static final Map<Long, Callback> responses = new HashMap<>();
	private static final Map<Long, BiConsumer<String, ConsoleContract>> consoleListeners = new HashMap<>();
	
	private static void registerCallback(long id, Callback cb){
		if(cb != null){
			if(id == 0) {
				cb.onReplyReceived(ResponseCodes.UNKNOWN_SERVER);
			}else {
				responses.put(id, cb);	
			}
		}
	}
	
	public static void shutdown(){
		ServerDroplet.client.shutdownServerManager();
	}
	
	/**
	 * Uses default type 'player'
	 * @param name the servers name
	 */
	public static void createServer(String name){
		createServer(name, "player", null);
	}
	
	public static void createServer(String name, String type){
		createServer(name, type, null);
	}
	
	/**
	 * <strong>Uses default type 'player'</strong>
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_CREATED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NAME_TAKEN}<br><br>
	 * 	{@link ResponseCodes#SERVER_NAME_INVALID}<br><br>
	 * 	{@link ResponseCodes#SERVER_NAME_LENGTH_INVALID}<br><br>
	 *  {@link ResponseCodes#UNKNOWN_SERVER_TYPE}
	 * <p>
	 * @param name the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void createServer(String name, Callback callback){
		long rid = ServerDroplet.client.sendCreateServer(name, "player");
		registerCallback(rid, callback);
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_CREATED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NAME_TAKEN}<br><br>
	 * 	{@link ResponseCodes#SERVER_NAME_INVALID}<br><br>
	 * 	{@link ResponseCodes#SERVER_NAME_LENGTH_INVALID}<br><br>
	 *  {@link ResponseCodes#UNKNOWN_SERVER_TYPE}
	 * <p>
	 * @param name the name of the server
	 * @param type the type of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void createServer(String name, String type, Callback callback){
		long rid = ServerDroplet.client.sendCreateServer(name, type);
		registerCallback(rid, callback);
	}
	
	public static void deleteServer(String name){
		deleteServer(name, null);
	}
	
	/**
	 * <i>Server will be killed if it is online</i><br>
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_REMOVED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void deleteServer(String name, Callback callback){
		long rid = ServerDroplet.client.sendDeleteServer(name);
		registerCallback(rid, callback);
	}
	
	/**
	 * <i>Server will be killed if it is online</i><br>
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_REMOVED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void deleteServerData(String name, Callback callback){
		long rid = ServerDroplet.client.sendDeleteServerData(name);
		registerCallback(rid, callback);
	}
	
	public static void stopServer(String name, boolean force){
		stopServer(name, force, null);
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_FORCE_STOPPED}<br><br>
	 * 	{@link ResponseCodes#SERVER_STOPPED}
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NOT_RUNNING}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param force whether or not to force the shutdown (if true the server process will be killed)
	 * @param callback will be executed when a response is returned
	 */
	public static void stopServer(String name, boolean force, Callback callback){
		long rid = ServerDroplet.client.sendStopServer(name, force);
		registerCallback(rid, callback);
	}
	
	public static void startServer(String name, boolean priority){
		startServer(name, priority, null);
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_STARTING}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_ALREADY_RUNNING}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void startServer(String name, boolean priority, Callback callback){
		long rid;
		try {
			rid = ServerDroplet.client.sendStartServer(name, priority);
			registerCallback(rid, callback);
		} catch (NoAvailableServerException e) {
			callback.onReplyReceived(ResponseCodes.MEMORY_LIMIT_REACHED);
		}
	}

	public static void restartServer(String name){
		restartServer(name, null);
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_RESTARTING}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NOT_RUNNING}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void restartServer(String name, Callback callback){
		long rid = ServerDroplet.client.sendRestartServer(name);
		registerCallback(rid, callback);
	}
	
	public static void commandServer(String name, String command){
		commandServer(name, command, null);
	}
	
	public static void giveCoinBonus(ServerInformation server, double bonus, long duration){
		long bonusTime = server.getCoinMultiplierTimeLeft();
		bonusTime += duration;
		bonusTime += System.currentTimeMillis();
		setMetadata(server.getName(), "multi", String.valueOf(bonus));
		setMetadata(server.getName(), "multitime", String.valueOf(bonusTime));
		
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_COMMAND_EXECUTED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NOT_RUNNING}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param name the name of the server
	 * @param command the command to execute as if it were typed into the console
	 * @param callback will be executed when a response is returned
	 */
	public static void commandServer(String name, String command, Callback callback){
		long rid = ServerDroplet.client.sendCommandToServer(name, command);
		registerCallback(rid, callback);
	}
	
	/**
	 * Listen to the console of a server. All output will be sent to the consumer
	 * @param name the name of the server
	 * @param reader the consumer of the console lines
	 */
	public static ConsoleContract listenToConsole(String name, BiConsumer<String, ConsoleContract> reader){
		if(reader == null){
			throw new IllegalArgumentException("reader cannot be null");
		}
		long rid = ServerDroplet.client.createConsoleReadContract(name);
		synchronized (consoleListeners) {
			consoleListeners.put(rid, reader);
		}
		return new ConsoleContract(rid);
	}
	
	public static void setMetadata(String server, String key, String value){
		setMetadata(server, key, value, null);
	}
	
	/**
	 * <p>Note: Metadata is saved when the server turns off or you call {@link #saveMetadata(String, Callback)}
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#METADATA_SET}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}
	 * <p>
	 * @param server the name of the server
	 * @param key the key of the metadata
	 * @param value the value of it
	 * @param callback will be executed when a response is returned
	 */
	public static void setMetadata(String server, String key, String value, Callback callback){
		long rid = ServerDroplet.client.setMetadata(server, key, value);
		registerCallback(rid, callback);
	}
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#METADATA_SAVED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#UNKNOWN_SERVER}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_ERROR} (Possibly from IO)
	 * <p>
	 * @param server the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void saveMetadata(String server, Callback callback){
		long rid = ServerDroplet.client.saveMetadata(server);
		
		registerCallback(rid, callback);
	}
	
	public static void saveMetadata(String server){
		saveMetadata(server, null);
	}
	
	public static void renameRealm(String oldName, String newName) {
		renameRealm(oldName, newName, null);
	}
	
	/**
	 * Possible responses:
	 * <p>
	 * 	<h2>Success</h2>
	 * 	{@link ResponseCodes#SERVER_RENAMED}<br>
	 * 	<h3>Errors</h3>
	 * 	{@link ResponseCodes#SERVER_NAME_INVALID}<br><br>
	 *  {@link ResponseCodes#SERVER_NAME_LENGTH_INVALID}<br><br>
	 *  {@link ResponseCodes#SERVER_NAME_TAKEN}<br><br>
	 * 	{@link ResponseCodes#UNKNOWN_ERROR} (Possibly from IO)
	 * <p>
	 * @param oldName the name of the server
	 * @param callback will be executed when a response is returned
	 */
	public static void renameRealm(String oldName, String newName, Callback callback) {
		long rid = ServerDroplet.client.renameRealm(oldName, newName);
		
		registerCallback(rid, callback);
	}
	
	public static Material getServerIcon(ServerInformation info){
		if(info.hasServerIcon()){
			return Material.valueOf(info.getServerIcon());
		}
		return Material.EMERALD_BLOCK;
	}
	
	public static void setServerIcon(ServerInformation info, Material material){
		setMetadata(info.getName(), "icon", material.name(), null);
	}
	
	public static void setServerIcon(ServerInformation info, Material material, Callback cb){
		setMetadata(info.getName(), "icon", material.name(), cb);
	}
	
	public static void setFeaturedServer(ServerInformation info){
		JedisAPI.setKey("featured", info.getName());
	}
	
	public static void setFeaturedServer(ServerInformation info, int time){
		JedisAPI.setKeyExpire("featured", info.getName(), time);
	}

	public static void setEarnings(ServerInformation server, int amount, Callback callback) {
		if(amount < 0){
			amount = 0;
		}
		DropletAPI.setMetadata(server.getName(), "earnings", String.valueOf(amount), callback);
	}

	public static int getGems(Player context) {
		return getGems(context.getUniqueId());
	}
	
	public static int getGems(ServerInformation info) {
		return getGems(info.getUUID());
	}
	
	public static int getThisServersGems() {
		return getGems(getThisServer());
	}
	
	public static int getGems(UUID context) {
		try {
			List<QueryResult> results = DatabaseAPI.query("SELECT `amount` FROM `gem_transactions` WHERE `uuid`=?", context.toString());
			int amount = 0;
			for(QueryResult result : results) {
				int dif = result.get("amount");
				amount += dif;
			}
			return amount;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static boolean createGemTransaction(UUID context, String name, int amount, String reason) {
		try {
			DatabaseAPI.execute("INSERT INTO `gem_transactions` (`uuid`, `amount`, `reason`, `name`) VALUES (?, ?, ?, ?)", context.toString(), amount, reason, name);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static int getCoins(Player player){
		if(!JedisAPI.keyExists("coins."+player.getUniqueId())){
			try {
				List<QueryResult> results = DatabaseAPI.query("SELECT `coins` FROM `players` WHERE `uuid`=?", player.getUniqueId().toString());
				//Select their data
				if(results.size() > 0){//This should always be true
					int coins = results.get(0).get("coins");
					//Cache values
					JedisAPI.setKey("coins."+player.getUniqueId().toString(), String.valueOf(coins));
					return coins;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		return Integer.valueOf(JedisAPI.getCachedValue("coins."+player.getUniqueId(), 60000));
	}
	
	public static void setCoins(Player player, int coins){
		try {
			coins = Math.max(0, coins);
			DatabaseAPI.execute("UPDATE `players` SET `coins`=? WHERE `uuid`=?", coins, player.getUniqueId().toString());
			JedisAPI.setKey("coins."+player.getUniqueId(), String.valueOf(coins));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static int getCoins(UUID uuid){
		if(!JedisAPI.keyExists("coins."+uuid)){
			try {
				List<QueryResult> results = DatabaseAPI.query("SELECT `coins` FROM `players` WHERE `uuid`=?", uuid.toString());
				//Select their data
				if(results.size() > 0){//This should always be true
					int coins = results.get(0).get("coins");
					//Cache values
					JedisAPI.setKey("coins."+uuid.toString(), String.valueOf(coins));
					return coins;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		return Integer.valueOf(JedisAPI.getCachedValue("coins."+uuid, 60000));
	}

	public static void setCoins(UUID uuid, int coins){
		try {
			coins = Math.max(0, coins);
			DatabaseAPI.execute("UPDATE `players` SET `coins`=? WHERE `uuid`=?", coins, uuid.toString());
			JedisAPI.setKey("coins."+uuid, String.valueOf(coins));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void changeCoins(Player pl, int amount) {
		int coins = getCoins(pl);
		setCoins(pl, coins + amount);
	}
	
	public static void removeFeaturedServer() {
		JedisAPI.removeKey("featured");
	}
	
	/**
	 * Get the currently featured server or null if it doesnt exist
	 * @return the featured server
	 */
	public static ServerInformation getFeaturedServer() {
		String featured = JedisAPI.getCachedValue("featured", TimeUnit.SECONDS.toMillis(5));
		if(featured != null){
			return getServerInfo(featured);
		}
		
		return null;
	}
	
	public static long getFeaturedTimeLeft(){
		if(getFeaturedServer() == null){
			return 0;
		}
		return JedisAPI.getTTL("featured", true);
	}
	
	public static void addServerUpdateListener(ServerUpdateListener listener){
		ServerDroplet.client.addListener(listener);
	}
	
	public static ServerInformation getServerInfo(String name){
		return ServerDroplet.client.getServerByName(name);
	}
	
	public static List<ServerInformation> getPlayerServers(){
		return ServerDroplet.client.getServers().stream().filter(info -> !info.isOfficial()).filter(server -> server.getServerType() == ServerType.PLAYER).collect(Collectors.toList());
	}
	
	public static Collection<ServerInformation> getServers(){
		return ServerDroplet.client.getServers();
	}
	
	public static List<ServerInformation> getByOwner(Player owner){
		Validate.notNull(owner);
		return getByOwner(owner.getUniqueId());
	}

	public static void addPremiumTime(ServerInformation server, long millis) {
		addPremiumTime(server, millis, null);
	}
	
	public static void addPremiumTime(ServerInformation server, long millis, Callback callback) {
		Validate.isTrue(millis > 0);
		Validate.notNull(server);
		long currentPremium = server.getPremiumLeft() + millis;
		DropletAPI.setMetadata(server.getName(), "premiumtime", String.valueOf(System.currentTimeMillis()+currentPremium), callback);
	}

	public static List<ServerInformation> getByOwner(UUID owner){
		Validate.notNull(owner);
		return getServers().stream().filter(info -> owner.equals(info.getOwner())).collect(Collectors.toList());
	}

	public static int getOnlinePlayers() {
		return Integer.valueOf(JedisAPI.getCachedValue("onlinecount", TimeUnit.SECONDS.toMillis(5)));
	}
	
	public static ServerInformation getThisServer(){
		String name = ServerDroplet.name;
		return getServerInfo(name);
	}
	
	public static void refreshServerInfo(String name, Callback cb){
		long id = ServerDroplet.client.refreshServer(name);
		registerCallback(id, cb);
	}
	
	public static void connectToServer(Player player, ServerInformation sinfo){
		final ServerInformation info = getServerInfo(sinfo.getName()); //Retrieve most updated
		if(info == null){
			Language.sendMessage(player, "connect.not_found");
			return;
		}else if(info.getStatus() == ServerStatus.OFFLINE){
			Language.sendMessage(player, "connect.offline");
			return;
		}else if(info.getStatus() == ServerStatus.STARTING){
			Language.sendMessage(player, "connect.starting");
			return;
		}else if(info.getStatus() == ServerStatus.ONLINE){
			Language.sendMessage(player, "connect.success", info.getName());
		}
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					Thread.sleep(1500);
				}catch(InterruptedException e) { }
				out.writeUTF("Connect");
				out.writeUTF(info.getName());
				player.sendPluginMessage(ServerDroplet.getInstance(), "BungeeCord", out.toByteArray());
			}
		}).start();
	}
	
	public static class ConsoleContract {
		private long id;
		private ConsoleContract(long id){
			this.id = id;
		}
		public void cancelContract() {
			synchronized (consoleListeners) {
				consoleListeners.remove(id);
				ServerDroplet.client.destroyConsoleContract(id);
			}
		}
		
	}
	
	public static class ResponseListener extends ServerUpdateAdapter {
		@Override
		public void onReply(long responseId, ResponseCodes code) {
			if(!responses.containsKey(responseId)){
				return;
			}
			Callback cb = responses.remove(responseId);
			Bukkit.getScheduler().runTask(ServerDroplet.getInstance(), () -> cb.onReplyReceived(code));
		}
		
		@Override
		public void onConsoleRead(long responseId, String line) {
			BiConsumer<String, ConsoleContract> reader = consoleListeners.get(responseId);
			if(reader == null){
				return;
			}
			reader.accept(line, new ConsoleContract(responseId));
		}
	}

	public static void setServerOwner(String text, UUID uniqueId) {
		DropletAPI.setMetadata(text, "owner", uniqueId.toString());
	}

	public static ServerInformation getHub(Player pl) {
		for(ServerInformation info : getServers()){
			if(info.getStatus() != ServerStatus.ONLINE){
				continue;
			}
			if(!info.getLanguage().equalsIgnoreCase(Language.getLocale(pl))){
				continue;
			}
			if(info.getServerType() == ServerType.HUB){
				return info;
			}
		}
		return getHub();
	}
	
	private static ServerInformation getHub() {
		for(ServerInformation info : getServers()){
			if(info.getServerType() == ServerType.HUB){
				return info;
			}
		}
		return null;
	}

	public static Rank getRank(Player player) {
		return getRank(player.getUniqueId());
	}
	
	public static Rank getRank(UUID uuid) {
		if(!JedisAPI.keyExists("ranks."+uuid)){
			List<QueryResult> results = Collections.emptyList();
			try {
				results = DatabaseAPI.query("SELECT `rank` FROM `players` WHERE `uuid`=?", uuid.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//Select their data
			if(results.size() > 0){//This should always be true
				int rank = results.get(0).get("rank");
				//Cache values
				JedisAPI.setKey("ranks."+uuid.toString(), String.valueOf(rank));
			}else{
				return ServerDroplet.getInstance().getRank("default");
			}
		}
		int rank = Integer.valueOf(JedisAPI.getCachedValue("ranks."+uuid, 60000));
		return ServerDroplet.getInstance().getRank(rank);
	}
	
	public static void setRank(Player pl, Rank rank){
		setRank(pl.getUniqueId(), rank);
	
	}

	public static List<ServerInformation> getOfficalGameServers() {
		return getServers().stream().filter(info -> info.isOfficial()).collect(Collectors.toList());
	}

	public static void setRank(UUID id, Rank rank) {
		try {
			
			if(JedisAPI.getValue("ranks."+id).equals(String.valueOf(rank.getId()))){
				ServerDroplet.getInstance().getLogger().warning("Rank already set for "+id+" to "+rank.getName());
				return;
			}
			ServerDroplet.getInstance().getLogger().info("Rank set for "+id+" to "+rank.getName());
			DatabaseAPI.execute("UPDATE `players` SET `rank`=? WHERE `uuid`=? LIMIT 1", rank.getId(), id.toString());
			JedisAPI.setKey("ranks."+id, String.valueOf(rank.getId()));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			Player player = Bukkit.getPlayer(id);
			if(player != null){
				rank.updatePermissionAttachement(player);
			}
		}
	}

	public static void reloadRanks() {
		JedisAPI.publish("rankReload", "Random Data");
		try {
			ServerDroplet.getInstance().loadRanks();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void reloadLists() {
		JedisAPI.publish("listReload", "Random Data");
		try {
			ServerDroplet.getInstance().loadFirewall();
			ServerDroplet.getInstance().loadBlacklist();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getTax() {
		if(!JedisAPI.keyExists("tax")){
			return 0;
		}
		return Integer.valueOf(JedisAPI.getCachedValue("tax", 60000));
	}
	
	public static void setTax(int percent){
		JedisAPI.setKey("tax", String.valueOf(percent));
	}

	public static int getTotalTaxed() {
		return getServers().stream().mapToInt(server -> server.getTaxedMoney()).sum();
	}

	public static void setTaxedMoney(ServerInformation info, int amount){
		setMetadata(info.getName(), "taxed", String.valueOf(amount));
	}

	public static void toggleCommandBlocks(ServerInformation thisServer, boolean flag, Callback callback) {
		setMetadata(thisServer.getName(), "cb", flag ? "" : "off", callback);
	}
	
	public static void toggleAllowFlight(ServerInformation thisServer, boolean flag, Callback callback) {
		setMetadata(thisServer.getName(), "af", flag ? "" : "off", callback);
	}

	public static ServerInformation getServerByUUID(UUID uuid) {
		for(ServerInformation info : ServerDroplet.client.getServers()) {
			if(info.getUUID().equals(uuid)) {
				return info;
			}
		}
		return null;
	}
	
	public static URLConnection openApiConnection(String path) throws IOException {
		URL url = new URL("");
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "");
		con.setRequestProperty("API-Key", "");
		return con;
	}

	private static final Map<UUID, Trail> trailCache = new HashMap<>();
	private static final Map<UUID, Long> trailCacheTime = new HashMap<>();
	
	public static Trail getTrail(Player pl) {
		Trail trail = null;
		if(trailCache.containsKey(pl.getUniqueId())) {
			long last = trailCacheTime.getOrDefault(pl.getUniqueId(), 0L);
			if(System.currentTimeMillis() - last < 60000) {
				trail = trailCache.get(pl.getUniqueId());
			}
		}
		
		if(trail == null) {
			String data = JedisAPI.getCachedValue("trails."+pl.getUniqueId(), 60000);
			if(data != null) {
				trail = Trail.getTrail(data);
				trailCache.put(pl.getUniqueId(), trail);
				trailCacheTime.put(pl.getUniqueId(), System.currentTimeMillis());
			}
		}
		return trail;
	}
	
	public static void setTrail(Player pl, Trail trail) {
		String data = trail.getName()+" "+trail.serialize();
		JedisAPI.setKey("trails."+pl.getUniqueId(), data);
		trailCacheTime.remove(pl.getUniqueId());
		trailCache.remove(pl.getUniqueId());
		
	}
}
