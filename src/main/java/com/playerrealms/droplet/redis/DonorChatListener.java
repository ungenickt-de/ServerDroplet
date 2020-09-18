package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DonorChatListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] { "donorchat" };
	}

	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(" ");

		String server = args[0];

		String player = args[1];

		String msg = "";

		for (int i = 2; i < args.length; i++) {
			msg += args[i] + " ";
		}

		TextComponent tc = new TextComponent(ChatColor.BLUE+"["+server+"] "+ChatColor.GREEN+player+" "+msg);
		
		tc.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/server "+server));

		for(Player pl : Bukkit.getOnlinePlayers()) {
			
			boolean display = true;
			
			if(JedisAPI.keyExists("donatorchat.toggle."+pl.getUniqueId())){
				display = false;
				JedisAPI.cacheKey("donatorchat.toggle."+pl.getUniqueId(), 60000);
			}
			
			if(display && DropletAPI.getRank(pl).hasPermission("playerrealms.donorchat")) {
				pl.spigot().sendMessage(tc);
			}
		}
	}


}
