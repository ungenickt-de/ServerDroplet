package com.playerrealms.droplet.menu.shop;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerShopMenu extends PagedMenu {
	
	private UUID viewer;
	
	public ServerShopMenu(ServerShop shop, Player viewer) {
		super(convert(shop, viewer), "Server Shop");
		
		this.viewer = viewer.getUniqueId();
	}
	
	
	
	private void resetCoinItem(Player viewer) {
		addItem(0, new Item(Material.GOLD_NUGGET).setTitle(ChatColor.LIGHT_PURPLE.toString()+ChatColor.BOLD+"Island"+ChatColor.GOLD+ChatColor.BOLD+"Coins").setLore(ChatColor.GRAY+"You have "+ChatColor.GOLD.toString()+DropletAPI.getCoins(viewer)+" coins", "", ChatColor.GRAY+"You have "+ChatColor.AQUA.toString()+DropletAPI.getGems(viewer)+" gems").build());
	}
	
	private static List<PageMenuEntry> convert(ServerShop shop, Player viewer) {
		
		List<PageMenuEntry> entries = new ArrayList<>();
		
		for(ServerShopItem item : shop.getItems()){
			if(item.isHidden()) {
				continue;
			}
			entries.add(new ServerShopItemEntry(item, viewer.getUniqueId()));
		}
		
		return entries;
	}
	
	@Override
	public void open(Player pl) {
		if(ServerDroplet.getInstance().getShop().getItems().isEmpty()){
			Language.sendMessage(pl, "shop.no_items");
			return;
		}
		resetCoinItem(pl);
		super.open(pl);
	}
	
	@Override
	public void remake(boolean refreshTitle) {
		super.remake(refreshTitle);
		resetCoinItem(Bukkit.getPlayer(viewer));
	}
	
	static class ServerShopItemEntry implements PageMenuEntry {

		private final ServerShopItem item;
		private final UUID viewer;
		
		public ServerShopItemEntry(ServerShopItem item, UUID viewer) {
			this.item = item;
			this.viewer = viewer;
		}
		
		@Override
		public int compareTo(PageMenuEntry o) {
			if(o instanceof ServerShopItemEntry){
				return Long.compare(item.getCreationTime(), ((ServerShopItemEntry) o).item.getCreationTime());
			}
			return 0;
		}

		@Override
		public ItemStack getItem() {
			return item.getDisplayItem(Bukkit.getPlayer(viewer));
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					item.purchase(player);
				}
			};
		}
		
	}

}
