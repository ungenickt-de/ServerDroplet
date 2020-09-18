package com.playerrealms.droplet.menu;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.util.PluginData;

public class PluginManagerMenu extends PagedMenu {

	private int page;
	
	public PluginManagerMenu(Player player) {
		this(0, player);
	}
	
	public PluginManagerMenu(int page, Player player) {
		super(convert(ServerDroplet.getInstance().getPlugins(), player), ChatColor.LIGHT_PURPLE+"Plugin Manager");
		this.page = page;
	}
	private static List<PageMenuEntry> convert(Map<PluginData, Boolean> plugins, Player player){
		return plugins.entrySet().stream().map(entry -> new PluginPageEntry(entry.getKey(), entry.getValue(), player, true)).collect(Collectors.toList());
	}
	
	public int getPage() {
		return page;
	}
	
}
