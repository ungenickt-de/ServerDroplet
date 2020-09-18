package com.playerrealms.droplet.redis;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.crates.CrateAPI;
import com.playerrealms.droplet.lang.Language;

public class GiveCratesListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"givecrates"};
	}

	@Override
	public void onMessage(String channel, String message) {
		UUID uuid = UUID.fromString(message.split(" ")[0]);
		String type = message.split(" ")[1];
		int amount = Integer.parseInt(message.split(" ")[2]);
		
		Player pl = Bukkit.getPlayer(uuid);
		
		for(int i = 0; i < amount;i++) {
			try {
				CrateAPI.giveCrate(pl.getUniqueId(), type);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
		Language.sendMessage(pl, "donation.crates", String.valueOf(amount));
	}

}
