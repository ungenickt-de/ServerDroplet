package com.playerrealms.droplet.menu.shop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface ServerShopItemAction {

	public void doAction(Player player);
	
	public void serialize(ConfigurationSection config);
	
	public void deserialize(ConfigurationSection config);

	public String getItemLore(Player player);
	
	public String getType();
	
}
