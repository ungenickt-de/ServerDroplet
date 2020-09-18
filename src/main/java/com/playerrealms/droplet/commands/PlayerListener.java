package com.playerrealms.droplet.commands;

import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.crates.CrateAPI;
import com.playerrealms.droplet.crates.GlobalCrateTypes;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.menu.shop.ServerShopItem;
import com.playerrealms.droplet.menu.shop.ServerShopItemAction;
import com.playerrealms.droplet.rank.Rank;
import com.playerrealms.droplet.redis.GlobalBanListener;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerListener implements Listener {

	private static final Pattern STRIP_ILLEGAL_COLORS = Pattern.compile(".*(&k)|(&m)|(&n).*");
	private static final String HEART = String.valueOf('\u2764');
	int maxplayer = Bukkit.getMaxPlayers() + 20;
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event){
		String msg = event.getMessage();
		String[] splits = msg.split(" ");
		if(splits[0].contains(":")){
			String split = splits[0];
			String fixed = split.substring(split.indexOf(':')+1);
			splits[0] = fixed;
			String joined = "";
			for(String s : splits){
				joined += s + " ";
			}
			event.setMessage(joined);
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Rank rank = DropletAPI.getRank(event.getPlayer());
		if(rank.hasPermission("playerrealms.chatcolor")) {
			String message = event.getMessage();
			message = STRIP_ILLEGAL_COLORS.matcher(message).replaceAll("");
			message = ChatColor.translateAlternateColorCodes('&', message);
			event.setMessage(message);
		}
		if(rank.hasPermission("playerrealms.heart")) {
			String message = event.getMessage();
			message = message.replace("<3", ChatColor.RED+HEART);
			event. setMessage(message);
		}
		if(DropletAPI.getThisServer().getName().equals("Annihilation")){
			return;
		}
		if (!event.isCancelled())
		{
			boolean pf = true;
			if(JedisAPI.keyExists("hiderank.toggle."+event.getPlayer().getUniqueId())){
				pf = false;
			}
			if(pf) {
				if (DropletAPI.getThisServer().getServerType() != ServerType.PLAYER) {
					String format = rank.getFormat() + ChatColor.WHITE + ": %s";
					event.setFormat(format.trim());
				} else {
					String format = rank.getPrefix() + event.getFormat();
					event.setFormat(format.trim());
				}
			}else{
				if (DropletAPI.getThisServer().getServerType() != ServerType.PLAYER) {
					String format = ChatColor.GRAY+"%s"+ChatColor.WHITE + ": %s";
					event.setFormat(format.trim());
				} else {
					String format = event.getFormat();
					event.setFormat(format.trim());
				}
			}
			
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChatFinish(AsyncPlayerChatEvent event){
		if(DropletAPI.getThisServer().getName().equals("Annihilation")){
			return;
		}
		if (!event.isCancelled())
		{
			boolean pf = true;
			if(JedisAPI.keyExists("hiderank.toggle."+event.getPlayer().getUniqueId())){
				pf = false;
				JedisAPI.cacheKey("hiderank.toggle."+event.getPlayer().getUniqueId(), 60000);
			}
			if(pf) {
				Rank rank = DropletAPI.getRank(event.getPlayer());
				String format = rank.getFormat() + ChatColor.WHITE + ": %s";

				event.setFormat(format.trim());
			}else{
				String format = ChatColor.GRAY+"%s"+ChatColor.WHITE + ": %s";
				event.setFormat(format.trim());
			}
		}

	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event){
		event.getWorld().setGameRuleValue("commandBlockOutput", "false");
		event.getWorld().setGameRuleValue("sendCommandFeedback", "false");
		event.getWorld().setGameRuleValue("logAdminCommands", "false");
		if(DropletAPI.getThisServer().getServerType() == ServerType.HUB) {
			event.getWorld().setGameRuleValue("reducedDebugInfo", "true");
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		Rank rank = DropletAPI.getRank(player);
		rank.updatePermissionAttachement(player);
		Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(DropletAPI.getThisServer().getServerType() == ServerType.HUB) {
					String ToggleKey = "hiderank.toggle." + player.getUniqueId();
					if (JedisAPI.keyExists(ToggleKey)) {
						JedisAPI.removeKey("hiderank.toggle." + player.getUniqueId());
						Language.sendMessage(player, "hiderank.turn_on");
					}
				}
				if(player.getUniqueId().equals(DropletAPI.getThisServer().getOwner())){
					Language.sendMessage(player, "realm_command.tutorial");
				}
				if(JedisAPI.keyExists("server_vote_reward."+player.getUniqueId())) {
					String server = JedisAPI.getValue("server_vote_reward."+player.getUniqueId());
					if(DropletAPI.getThisServer().getUUID().toString().equals(server)) {
						JedisAPI.removeKey("server_vote_reward."+player.getUniqueId());
						List<ServerShopItem> voteRewards = ServerDroplet.getInstance().getShop().getVoteRewards();
						for(ServerShopItem item : voteRewards) {
							for(ServerShopItemAction action : item.getPurchaseActions()) {
								action.doAction(player);
							}
						}
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerLogin(PlayerLoginEvent e){
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if(e.getResult() == PlayerLoginEvent.Result.KICK_FULL && ( (Bukkit.getOnlinePlayers().size() < maxplayer && DropletAPI.getRank(p).hasPermission("playerrealms.bypass.limit")) || (DropletAPI.getThisServer().getServerType() == ServerType.PLAYER && DropletAPI.getThisServer().getOwner().equals(uuid)))){
			e.setResult(PlayerLoginEvent.Result.ALLOWED);
		}
		if(e.getResult() == PlayerLoginEvent.Result.KICK_BANNED && (DropletAPI.getRank(p).hasPermission("playerrealms.bypass.admin") || (DropletAPI.getThisServer().getServerType() == ServerType.PLAYER && DropletAPI.getThisServer().getOwner().equals(uuid)))){
			e.setResult(PlayerLoginEvent.Result.ALLOWED);
		}
		if (e.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST && (DropletAPI.getRank(p).hasPermission("playerrealms.bypass.admin")) || (DropletAPI.getThisServer().getServerType() == ServerType.PLAYER && DropletAPI.getThisServer().getOwner().equals(uuid))) {
			e.setResult(PlayerLoginEvent.Result.ALLOWED);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onLogin(AsyncPlayerPreLoginEvent event){
		try {
			List<QueryResult> results = DatabaseAPI.query("SELECT `coins`,`rank`,`ban_expire_time`,`ban_reason`,`ban_type` FROM `players` WHERE `uuid`=?", event.getUniqueId().toString());
			
			if(!results.isEmpty() && results.get(0).hasKey("ban_expire_time")) {
				int banType = results.get(0).get("ban_type");
				if((banType & DropletAPI.BAN_TYPE_PLAY) == DropletAPI.BAN_TYPE_PLAY) {
					long expireTime = results.get(0).get("ban_expire_time");
					if(System.currentTimeMillis() < expireTime) {
						if(DropletAPI.getThisServer().getServerType() != ServerType.HUB) {
							String banReason = results.get(0).get("ban_reason");
							event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
							event.setKickMessage(banReason+" "+ GlobalBanListener.formatBanTime(expireTime - System.currentTimeMillis(), Language.getLanguage(event.getUniqueId())));
						}
						return;
					}	
				}
			}
			
			if(results.size() == 0) {
				DatabaseAPI.execute("INSERT IGNORE INTO `players` (`uuid`) VALUES (?)", event.getUniqueId().toString());//Insert the player in
			}
			
			//If rank or coins aren't cached
			if(!JedisAPI.keyExists("coins."+event.getUniqueId().toString()) || !JedisAPI.keyExists("ranks."+event.getUniqueId().toString())){
				int updated = DatabaseAPI.execute("INSERT IGNORE INTO `players` (`uuid`) VALUES (?)", event.getUniqueId().toString());//Insert the player in
				if(updated == 0){//If we couldn't add them, then they already exist
					//Select their data
					if(results.size() > 0){//This should always be true
						int coins = results.get(0).get("coins");
						int rank = results.get(0).get("rank");
						//Cache values
						JedisAPI.setKey("coins."+event.getUniqueId().toString(), String.valueOf(coins));
						JedisAPI.setKey("ranks."+event.getUniqueId().toString(), String.valueOf(rank));
						
						ServerDroplet.getInstance().getLogger().info("Loaded coins and rank from mysql for "+event.getUniqueId());
					}
					//They are new!
				}else{
					CrateAPI.giveCrate(event.getUniqueId(), GlobalCrateTypes.STARTER.getTypeString());
					JedisAPI.setKey("coins."+event.getUniqueId().toString(), String.valueOf(0));//Cache no coins
					JedisAPI.setKey("ranks."+event.getUniqueId().toString(), String.valueOf(0));//Cache as default
				}
			}else{
				JedisAPI.cacheKey("coins."+event.getUniqueId().toString(), 60000);//Cache coins for 60 seconds
				JedisAPI.cacheKey("ranks."+event.getUniqueId().toString(), 60000);//Cache rank for 60 seconds
			}
			DatabaseAPI.execute("UPDATE `players` SET `ip`=?,`last_server`=? WHERE `uuid`=?", event.getAddress().toString(), DropletAPI.getThisServer().getName(), event.getUniqueId().toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
