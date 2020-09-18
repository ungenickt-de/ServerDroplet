package com.playerrealms.droplet.menu.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import net.md_5.bungee.api.ChatColor;

public class ServerShopEditMenu extends PagedMenu {

	public ServerShopEditMenu(ServerShop shop, Player viewer) {
		super(convert(shop, viewer), "Server Shop (Edit)");
	}
	
	private static List<PageMenuEntry> convert(ServerShop shop, Player viewer) {
		
		List<PageMenuEntry> entries = new ArrayList<>();
		
		for(ServerShopItem item : shop.getItems()){
			entries.add(new ServerShopItemEntry(item, viewer.getUniqueId()));
		}
		
		return entries;
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
				return Long.compare(((ServerShopItemEntry) o).item.getCreationTime(), item.getCreationTime());
			}
			return 0;
		}

		@Override
		public ItemStack getItem() {
			
			ItemStack itemstack = item.getDisplayItem(Bukkit.getPlayer(viewer));
			
			Item i = new Item(itemstack);
			
			i.getLore().add(0, ChatColor.YELLOW+"Click to Edit!!");
			
			return i.build();
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					ShopItemEditMenu otherMenu = new ShopItemEditMenu(player, item);
					
					otherMenu.open(player);
				}
			};
		}
		
	}

}
