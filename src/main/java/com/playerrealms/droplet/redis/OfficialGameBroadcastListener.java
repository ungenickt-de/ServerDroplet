package com.playerrealms.droplet.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.lang.Language;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class OfficialGameBroadcastListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"official"};
	}

	@Override
	public void onMessage(String channel, String message) {
		if(message.equals("dvz.starting")) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				String text = Language.getText(player, "official_broadcasts.dvz.starting");
				TextComponent tc = new TextComponent(text);
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join DwarfsVsZombies"));
				player.spigot().sendMessage(tc);
			}
		}else if (message.equals("anni.starting")) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				String text = Language.getText(player, "official_broadcasts.anni.starting");
				TextComponent tc = new TextComponent(text);
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join Annihilation"));
				player.spigot().sendMessage(tc);
			}
		}
	}
}
