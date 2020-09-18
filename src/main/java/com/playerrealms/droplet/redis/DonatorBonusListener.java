package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;

public class DonatorBonusListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"donator_bonus"};
	}

	@Override
	public void onMessage(String channel, String message) {
		/*for(Player pl : Bukkit.getOnlinePlayers()) {
			
			int amount = 5;
			
			DropletAPI.setCoins(pl, DropletAPI.getCoins(pl) + amount);
			
			//Language.sendMessage(pl, "thanks_to", message, 5);

		}*/
	}
}
