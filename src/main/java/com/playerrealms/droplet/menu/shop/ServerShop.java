package com.playerrealms.droplet.menu.shop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ServerShop {

	private List<ServerShopItem> items;
	
	private FileConfiguration config;
	
	private SaveMethod saveMethod;
	
	public ServerShop(FileConfiguration config, SaveMethod saveMethod) {
		items = new ArrayList<>();
		this.config = config;
		this.saveMethod = saveMethod;
		load();
	}
	

	public List<ServerShopItem> getVoteRewards() {
		List<ServerShopItem> voteRewards = new ArrayList<>();
		
		for(ServerShopItem item : items) {
			if(item.getPriceType() == PriceType.VOTE) {
				voteRewards.add(item);
			}
		}
		
		return voteRewards;
	}
	
	private void load(){
		ConfigurationSection itemsSection = null;
		
		if(!config.contains("items")){
			itemsSection = config.createSection("items");
		}else{
			itemsSection = config.getConfigurationSection("items");
		}
		
		for(String key : itemsSection.getKeys(false)){
			ConfigurationSection itemSec = itemsSection.getConfigurationSection(key);
			
			ServerShopItem item = new ServerShopItem();
			item.deserialize(itemSec);
			
			items.add(item);
		}
		
	}
	
	public void save(){
		ConfigurationSection itemsSection = null;
		
		if(!config.contains("items")){
			itemsSection = config.createSection("items");
		}else{
			itemsSection = config.getConfigurationSection("items");
		}
		for(String key : itemsSection.getKeys(false)){
			itemsSection.set(key, null);
		}
		int i = 0;
		for(ServerShopItem item : items){
			ConfigurationSection itemSec = itemsSection.createSection(String.valueOf(i++));
			
			item.serialize(itemSec);
		}
		saveMethod.save(config);
	}
	
	public List<ServerShopItem> getItems() {
		return items;
	}
	
	public void addItem(ServerShopItem item){
		items.add(item);
		save();
	}
	
	public void removeItem(ServerShopItem item){
		items.remove(item);
		save();
	}
	
	public static interface SaveMethod {
		/**
		 * Save this file configuration
		 * @param config the config to save
		 */
		public void save(FileConfiguration config);
	}
}
