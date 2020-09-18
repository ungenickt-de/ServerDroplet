package com.playerrealms.droplet.redis;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.menu.shop.ServerShopItem;
import com.playerrealms.droplet.menu.shop.ServerShopItemAction;

public class ServerVoteListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"server_vote"};
	}

	@Override
	public void onMessage(String ch, String message) {
		
		String[] args = message.split(" ");
		
		UUID player = UUID.fromString(args[0]);
		
		String serverName = args[1];
		
		if(DropletAPI.getThisServer().getName().equalsIgnoreCase(serverName)) {
			
			Player online = Bukkit.getPlayer(player);
			
			
			if(online != null) {
				List<ServerShopItem> voteRewards = ServerDroplet.getInstance().getShop().getVoteRewards();
					
				for(ServerShopItem item : voteRewards) {
					for(ServerShopItemAction action : item.getPurchaseActions()) {
						action.doAction(online);
					}
				}
				
			}else{
				JedisAPI.setKey("server_vote_reward."+player, DropletAPI.getThisServer().getUUID().toString());
			}
			
		}
		
	}

}
