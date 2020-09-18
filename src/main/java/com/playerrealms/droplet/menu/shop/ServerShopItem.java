package com.playerrealms.droplet.menu.shop;

import com.nirvana.menu.Item;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.menu.shop.actions.CommandShopAction;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ServerShopItem {

	private Material type;
	private String name;
	private int price;
	private PriceType priceType;
	private Sound purchaseSound;
	private long creationTime;
	private int sale;
	private boolean hidden;
	
	private List<ServerShopItemAction> actions;
	
	public ServerShopItem() {
		actions = new ArrayList<>();
		price = 0;
		creationTime = System.currentTimeMillis();
		sale = 0;
		hidden = false;
		priceType = PriceType.COINS;
	}
	
	public List<ServerShopItemAction> getPurchaseActions() {
		return actions;
	}
	
	public void serialize(ConfigurationSection config) {
		config.set("type", getType().name());
		config.set("name", name);
		config.set("price", price);
		config.set("creationTime", creationTime);
		config.set("sale", sale);
		config.set("price_type", priceType.name());
		config.set("hidden", hidden);
		ConfigurationSection actionSection = config.createSection("actions");
		int i = 0;
		for(ServerShopItemAction action : actions){
			ConfigurationSection subSec = actionSection.createSection(String.valueOf(i++));
			subSec.set("actionType", action.getType());
			action.serialize(subSec);
		}
	}
	
	public void deserialize(ConfigurationSection config) {
		type = Material.valueOf(config.getString("type"));
		name = config.getString("name");
		price = Math.abs(config.getInt("price"));
		sale = config.getInt("sale", 0);
		creationTime = config.getLong("creationTime");
		
		if(!config.contains("price_type")) {
			if(config.getBoolean("vote_reward", false)) {//backwards compatibility
				priceType = PriceType.VOTE;
			}else {
				priceType = PriceType.COINS;
			}
		}else {
			priceType = PriceType.valueOf(config.getString("price_type"));
		}
		
		hidden = config.getBoolean("hidden", false);
		ConfigurationSection actionSection = config.getConfigurationSection("actions");
		for(String key : actionSection.getKeys(false)){
			ConfigurationSection actionSec = actionSection.getConfigurationSection(key);
			
			String actionType = actionSec.getString("actionType");
			
			ServerShopItemAction action = null;
			
			if(actionType.equals("command")){
				action = new CommandShopAction();
			}else{
				continue;
			}
			
			action.deserialize(actionSec);
			
			actions.add(action);
		}
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public PriceType getPriceType() {
		return priceType;
	}
	
	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	public int getPrice() {
		if(priceType == PriceType.VOTE) {
			return 0;
		}
		if(sale > 0){
			float salePercent = 100F - sale;
			
			salePercent /= 100F;
			
			return (int) (salePercent * price);
		}
		return price;
	}
	
	public void changePrice(int cost){
		this.price = Math.abs(cost);
	}
	
	public void changeSale(int sale){
		if(sale > 100){
			sale = 100;
		}else if(sale < 0){
			sale = 0;
		}
		this.sale = sale;
	}
	
	private boolean isOwner(Player player) {
		return DropletAPI.getThisServer().hasOwner() && DropletAPI.getThisServer().getOwner().equals(player.getUniqueId());
	}
	
	public void purchase(Player player) {
		int price = getPrice();
		if(priceType == PriceType.VOTE && !isOwner(player)) {
			Language.sendMessage(player, "server_shop_item.vote_only");
			return;
		}
		int coins = priceType.getBalance(player);
		
		if(coins >= price || isOwner(player)){
			
			priceType.confirmPurchase(price, player, new Consumer<Boolean>() {
				
				@Override
				public void accept(Boolean bought) {
					if(bought) {
						if(!ServerDroplet.isClientActive()){
							Language.sendMessage(player, "server_shop_item.failed_player");
							if(Bukkit.getPlayer(DropletAPI.getThisServer().getOwner()) != null){
								Language.sendMessage(Bukkit.getPlayer(DropletAPI.getThisServer().getOwner()), "server_shop_item.failed_owner");
							}
							return;
						}

						if(!isOwner(player)) {
							priceType.adjustBalance(player, -price);
							
							priceType.adjustServerBalance(price, player.getName()+" "+player.getUniqueId());	
						}
						
						for(ServerShopItemAction action : actions){
							action.doAction(player);
						}
						
						Language.sendMessage(player, "server_shop_item.purchased", getName());
						
						UUID owner = DropletAPI.getThisServer().getOwner();
						
						if(Bukkit.getPlayer(owner) != null){
							Language.sendMessage(Bukkit.getPlayer(owner), "server_shop_item.purchased_to_owner", player.getName(), getName());
						}
					}else {
						ServerShopMenu menu =  new ServerShopMenu(ServerDroplet.getInstance().getShop(), player);
						menu.open(player);
					}
				}
			});
			
		}else{
			Language.sendMessage(player, "server_shop_item.need_coins");
		}
	}
	
	
	public ItemStack getDisplayItem(Player player) {
		return new Item(getType()).setTitle(name).setLore(getLore(player)).build();
	}
	
	private List<String> getLore(Player player) {
		List<String> lore = new ArrayList<>();
		if(sale > 0){
			int salePercent = sale;
			if(priceType == PriceType.VOTE) {
				lore.add(Language.getText(player, "menu_items.shop.vote_reward"));
			}else if(priceType == PriceType.COINS) {
				lore.add(Language.getText(player, "menu_items.shop.price_sale_coins", price, getPrice()));
				lore.add(Language.getText(player, "menu_items.shop.sale", salePercent));
			}else if(priceType == PriceType.GEMS) {
				lore.add(Language.getText(player, "menu_items.shop.price_sale_gems", price, getPrice()));
				lore.add(Language.getText(player, "menu_items.shop.sale", salePercent));
			}
		}else{
			if(priceType == PriceType.VOTE) {
				lore.add(Language.getText(player, "menu_items.shop.vote_reward"));
			}else if(priceType == PriceType.COINS) {
				lore.add(Language.getText(player, "menu_items.shop.price_coins", getPrice()));
			}else if(priceType == PriceType.GEMS) {
				lore.add(Language.getText(player, "menu_items.shop.price_gems", getPrice()));
			}
		}
		
		return lore;
	}

	public void rename(String name) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
	}
	
	public void changeMaterial(Material material) {
		type = material;
	}
	
	public void changePurchaseSound(Sound purchaseSound) {
		this.purchaseSound = purchaseSound;
	}
	
	public String getName() {
		if(name == null){
			return "Un-named Item";
		}
		return name;
	}
	
	public Material getType() {
		if(type == null){
			return Material.APPLE;
		}
		return type;
	}
	
	public Sound getPurchaseSound() {
		if(purchaseSound == null){
			return Sound.ENTITY_PLAYER_LEVELUP;
		}
		return purchaseSound;
	}

	public int getSale() {
		return sale;
	}
	
}
