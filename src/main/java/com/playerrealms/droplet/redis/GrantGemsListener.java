package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GrantGemsListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"grantgems"};
	}

	@Override
	public void onMessage(String channel, String message) {

		String[] args = message.split(" ");
		
		UUID uuid = UUID.fromString(args[0]);
		String name = args[1];
		int amount = Integer.parseInt(args[2]);
		
		String reason = "";
		for(int i = 3; i < args.length;i++) {
			reason += args[i] + " ";
		}
		DropletAPI.createGemTransaction(uuid, name, amount, reason);
		
		Player pl = Bukkit.getPlayer(uuid);
		if(pl != null) {
			Language.sendMessage(pl, "gem_purchase.receive", amount);
		}
		
	}

}
