package com.playerrealms.droplet.menu.shop;

import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.lang.Language;

public class RemoveActionMenu extends PagedMenu {

	public RemoveActionMenu(ServerShopItem item, Player viewer) {
		super(item.getPurchaseActions().stream().map(action -> new PurchaseActionEntry(item, action, viewer.getUniqueId())).collect(Collectors.toList()), "Select Action to Remove");
	}

	static class PurchaseActionEntry implements PageMenuEntry {

		private ServerShopItemAction action;
		private UUID person;
		private ServerShopItem item;
		
		public PurchaseActionEntry(ServerShopItem item, ServerShopItemAction action, UUID viewer) {
			this.action = action;
			person = viewer;
			this.item = item;
		}
		
		@Override
		public int compareTo(PageMenuEntry o) {
			return 0;
		}

		@Override
		public ItemStack getItem() {
			return new Item(Material.COMMAND).setTitle(Language.getText(Bukkit.getPlayer(person), "menu_items.remove_action.title")).setLore(ChatColor.RED+action.getItemLore(Bukkit.getPlayer(person))).build();
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					item.getPurchaseActions().remove(action);
					
					ShopItemEditMenu edit = new ShopItemEditMenu(player, item);
					edit.open(player);
				}
			};
		}
		
	}
	
}
