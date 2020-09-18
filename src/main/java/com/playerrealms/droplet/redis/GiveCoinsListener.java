package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GiveCoinsListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"givecoins"};
	}

	@Override
	public void onMessage(String channel, String message) {
		UUID uuid = UUID.fromString(message.split(" ")[0]);
		int amount = Integer.parseInt(message.split(" ")[1]);
		
		int coins = DropletAPI.getCoins(uuid) + amount;
		DropletAPI.setCoins(uuid, coins);

		Player pl = Bukkit.getPlayer(uuid);
		if (pl != null) {
			Language.sendMessage(pl, "donation.thanks", String.valueOf(amount));
		}
	}

}
