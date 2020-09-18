package com.playerrealms.droplet.menu.shop;

import com.nirvana.menu.*;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public enum PriceType {

	COINS,
	VOTE,
	GEMS;
	
	public int getBalance(Player player) {
		if(this == COINS) {
			return DropletAPI.getCoins(player);
		}else if(this == GEMS) {
			return DropletAPI.getGems(player);
		}else if(this == PriceType.VOTE) {
			return 0;
		}
		return 0;
	}
	
	public void confirmPurchase(int amount, Player player, Consumer<Boolean> callback) {
		if(this == COINS) {
			callback.accept(true);
		}else if(this == GEMS) {
			
			ChestPacketMenu menu = new ChestPacketMenu(9, "Do you want to buy?") {
				
				@Override
				public void close() {
					super.close();
					if(getMinimumClickDelay() != 9999){
						callback.accept(false);
					}
				}
			};
			for(int i = 0; i < menu.getSize();i++) {
				menu.addItem(i, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "gem_purchase.cancel")).build());
			}
			menu.addItem(4, new Item(Material.EMERALD).setTitle(Language.getText(player, "gem_purchase.buy")).setLore(Language.getText(player, "gem_purchase.buy_lore", amount)).build());
			menu.addGeneralHandler(new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					if(interactionInfo.getItem().getType() == Material.EMERALD) {
						((ChestPacketMenu)menu).setMinimumClickDelay(9999);//We can use this to signal to the close() method that we dont want it to callback(false)
						menu.close();
						callback.accept(true);
					}else {
						((ChestPacketMenu)menu).setMinimumClickDelay(9999);
						menu.close();
						callback.accept(false);
					}
				}
			});
			menu.open(player);
		}else if(this == VOTE) {
			callback.accept(true);
		}
	}
	
	public boolean adjustServerBalance(int amount, String playerName) {
		if(this == COINS) {
			DropletAPI.setEarnings(DropletAPI.getThisServer(), DropletAPI.getThisServer().getEarnings() + amount, null);
			return true;
		}else if(this == GEMS) {
			ServerInformation info = DropletAPI.getThisServer();
			return DropletAPI.createGemTransaction(info.getUUID(), "Server "+info.getName(), amount, "Receive "+Math.abs(amount)+" for store purchase from "+playerName);
		}else if(this == PriceType.VOTE) {
			return false;
		}
		return false;
	}
	
	public boolean adjustBalance(Player player, int amount) {
		if(this == COINS) {
			DropletAPI.setCoins(player, getBalance(player) + amount);
			return true;
		}else if(this == GEMS) {
			return DropletAPI.createGemTransaction(player.getUniqueId(), player.getName(), amount, "Purchase item for "+Math.abs(amount)+" on "+DropletAPI.getThisServer().getName());
		}else if(this == PriceType.VOTE) {
			return false;
		}
		return false;
	}
	
}
