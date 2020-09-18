package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.crates.CrateAPI;
import com.playerrealms.droplet.crates.GlobalCrateTypes;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimedRewardListener implements JedisListener {

	private static final int VIP = 0, MVP = 1, NONE = -1;
	
	@Override
	public String[] getChannel() {
		return new String[] {"timed_reward"};
	}
	
	private int getBestRankOnServer() {
		int best = NONE;
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.mvp")){
				return MVP;
			}else if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.vip")){
				best = VIP;
			}
		}
		return best;
	}

	@Override
	public void onMessage(String ch, String message) {
		if(DropletAPI.getThisServer().getServerType() != ServerType.PLAYER){
			return;
		}
		UUID uuid = UUID.fromString(message.split(" ")[0]);
		int amount = Integer.parseInt(message.split(" ")[1]);
		Player pl = Bukkit.getPlayer(uuid);
		double bonus = DropletAPI.getThisServer().getCoinMultiplier();
		if(pl == null){
			return;
		}
		
		if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.vip") || DropletAPI.getRank(pl).hasPermission("playerrealms.rank.mvp")) {
			JedisAPI.publish("donator_bonus", pl.getName());
		}
		
		int bonusAmount = (int) (bonus * amount);
		
		if (pl != null) {
			if(bonusAmount > 0){
				if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.mvp")){
					Language.sendMessage(pl, "generic.timed_reward_bonus", amount + bonusAmount, bonusAmount);
				}else {
					Language.sendMessage(pl, "generic.timed_reward_friend_bonus", amount + bonusAmount, bonusAmount);
				}
			}else{
				Language.sendMessage(pl, "generic.timed_reward", amount);
			}
			
			DropletAPI.setCoins(pl, DropletAPI.getCoins(pl) + amount + bonusAmount);
			
			String crateTimeKey = "last_crate."+pl.getUniqueId();
			
			boolean canReceiveCrate = true;
			
			if(JedisAPI.keyExists(crateTimeKey)) {
				long lastCrate = Long.valueOf(JedisAPI.getValue(crateTimeKey));
				if(System.currentTimeMillis() - lastCrate < TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30)) {
					canReceiveCrate = false;
				}
			}
			
			if(canReceiveCrate) {
				JedisAPI.setKey(crateTimeKey, String.valueOf(System.currentTimeMillis()));
				Random r = new Random();
				try {
					int type = r.nextInt(100);

					String globalEvent = JedisAPI.getCachedValue("global_event", TimeUnit.HOURS.toMillis(1));
					
					if(globalEvent == null) {
						globalEvent = "none";
					}
					
					if(globalEvent.equals("ani_2018")) {
						if(type < 80) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.KINENN_2018_COMMON.getTypeString());	
						}else if(type < 97) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.KINENN_2018_RARE.getTypeString());
						}else {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.KINENN_2018_LEGENDARY.getTypeString());
						}
					}else if(globalEvent.equals("gemweekend")) {
						if(type < 80) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.GEM_BOX_SMALL.getTypeString());	
						}else if(type < 95) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.GEM_BOX_MEDIUM.getTypeString());
						}else {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.GEM_BOX_LARGE.getTypeString());
						}
					}else if(globalEvent.equals("summer")) {
						if(type < 80) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.SUMMER_SMALL.getTypeString());
						}else if(type < 95) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.SUMMER_MEDIUM.getTypeString());
						}else {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.SUMMER_BIG.getTypeString());
						}
					}else {
						if(type < 80) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.COMMON.getTypeString());	
						}else if(type < 97) {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.RARE.getTypeString());
						}else {
							CrateAPI.giveCrate(uuid, GlobalCrateTypes.LEGENDARY.getTypeString());
						}	
					}
					
					
					Language.sendMessage(pl, "crates.get");
					int bonusCrate = -1;
					if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.mvp")){
						bonusCrate = 5;
					}else if(DropletAPI.getRank(pl).hasPermission("playerrealms.rank.vip")){
						bonusCrate += 10;
					}
					
					if(bonusCrate >= 0 && r.nextInt(bonusCrate) == 0) {
						CrateAPI.giveCrate(uuid, GlobalCrateTypes.DONATION.getTypeString());
					}
					
					
					UUID plId = pl.getUniqueId();
					Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {
						@Override
						public void run() {
							try {
								List<QueryResult> results = DatabaseAPI.query("SELECT `playtime` FROM `players` WHERE `uuid`=?", plId.toString());
								if(results.size() > 0)
								{
									int playtime = results.get(0).get("playtime");
									playtime += 30;//30 minutes
									DatabaseAPI.execute("UPDATE `players` SET `playtime`=? WHERE `uuid`=?", playtime, plId.toString());
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
	}

}
