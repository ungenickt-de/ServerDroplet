package com.playerrealms.droplet.redis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] { "report" };
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

		TextComponent tc = new TextComponent(ChatColor.DARK_RED+"[REPORT] "+ChatColor.BLUE+"["+server+"] "+ChatColor.GREEN+player+" "+msg);
		
		tc.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/server "+server));

		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(DropletAPI.getRank(pl).hasPermission("playerrealms.adminchat")) {
				pl.spigot().sendMessage(tc);
			}
		}
		
		
	}

}
