package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class ServerCmListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"server_cm"};
	}

	@Override
	public void onMessage(String channel, String message) {
		
		String name = message;
		
		ServerInformation info = DropletAPI.getServerInfo(name);
		if(info == null) {
			ServerDroplet.getInstance().getLogger().warning("Cannot broadcast CM for unknown server "+name);
			return;
		}
		
		if(DropletAPI.getThisServer().getName().equals(name)) {
			UUID owner = DropletAPI.getThisServer().getOwner();
			if(Bukkit.getPlayer(owner) != null) {
				sendCM(Bukkit.getPlayer(owner), info);
			}
			return;
		}
		
		if(!DropletAPI.getThisServer().isUltraPremium()) {
			Random r = new Random();
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(r.nextInt(4) == 0) {
					sendCM(pl, info);
				}
			}
			
		}
		
	}
	
	private void sendCM(Player pl, ServerInformation info) {
		ComponentBuilder builder = new ComponentBuilder(info.getName()).color(ChatColor.GREEN).append("\n");
		builder.append("\n");
		builder.append(Language.getText(pl, "menu_items.page_entry.online", info.getPlayersOnline(), info.getMaxPlayers()));
		builder.append("\n");
		builder.append(Language.getText(pl, "menu_items.page_entry.votes", info.getVotes()));
		builder.append("\n");
		builder.append(Language.getText(pl, "menu_items.page_entry.owner", MojangAPI.getUsername(info.getOwner())));
		pl.spigot().sendMessage(new ComponentBuilder(Language.getText(pl, "koukoku.prefix")+" ")
				.append(info.getName())
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server "+info.getName()))
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()))
				.append(info.getMotd())
				.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server "+info.getName()))
				.create());
	}
}
