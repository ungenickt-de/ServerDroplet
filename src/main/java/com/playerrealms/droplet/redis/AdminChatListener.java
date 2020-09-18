package com.playerrealms.droplet.redis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;

public class AdminChatListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] { "adminchat" };
	}

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(" ");
		
		String server = args[0];
		
		String player = args[1];
		
		String msg = "";
		
		for(int i = 2; i < args.length;i++) {
			msg += args[i] + " ";
		}
		
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(DropletAPI.getRank(pl).hasPermission("playerrealms.adminchat")) {
				pl.sendMessage(ChatColor.DARK_RED+"[AC] "+ChatColor.BLUE+"["+server+"] "+ChatColor.GREEN+player+" "+msg);
			}
		}
		
	}

}
