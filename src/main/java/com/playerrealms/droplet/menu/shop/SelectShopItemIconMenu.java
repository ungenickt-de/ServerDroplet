package com.playerrealms.droplet.menu.shop;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.lang.Language;

public class SelectShopItemIconMenu extends PagedMenu {

	public SelectShopItemIconMenu(Player player, ServerShopItem item, Material... icons) {
		super(Arrays.asList(icons).stream().map(icon -> new IconPageEntry(icon, player.getUniqueId(), item)).collect(Collectors.toList()), Language.getText(player, "menu.edit_store_item.select_icon_menu"));
	}
	
	static class IconPageEntry implements PageMenuEntry {

		private final Material icon;
		
		private final UUID player;
		
		private ServerShopItem item;
		
		public IconPageEntry(Material icon, UUID player, ServerShopItem item) {
			this.icon = icon;
			this.player = player;
			this.item = item;
		}
		
		@Override
		public int compareTo(PageMenuEntry o) {
			if(o instanceof IconPageEntry){
				return ((IconPageEntry) o).icon.compareTo(icon);
			}
			return 0;
		}

		@Override
		public ItemStack getItem() {
			Item item = new Item(icon).setTitle(ChatColor.AQUA+WordUtils.capitalizeFully(icon.name().replace('_', ' ')));
			if(this.item.getType().equals(icon)){
				item.setLore(Language.getText(Bukkit.getPlayer(player), "menu_items.shop_config.edit.icon_selected"));
				item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
			return item.build();
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					item.changeMaterial(icon);
					
					ShopItemEditMenu editMenu = new ShopItemEditMenu(player, item);
					
					editMenu.open(player);
				}
			};
		}
		
	}

}
