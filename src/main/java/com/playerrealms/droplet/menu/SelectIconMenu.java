package com.playerrealms.droplet.menu;


import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.DropletAPI;

public class SelectIconMenu extends PagedMenu {

	public SelectIconMenu(Material... icons) {
		super(Arrays.asList(icons).stream().map(material -> new IconEntry(material)).collect(Collectors.toList()), ChatColor.BLUE+"Select a Icon");
	
		
	
	}

	static class IconEntry implements PageMenuEntry {

		private Material material;
		
		public IconEntry(Material m) {
			material = m;
		}
		
		@Override
		public int compareTo(PageMenuEntry o) {
			if(o instanceof IconEntry){
				return material.compareTo(((IconEntry) o).material);
			}
			return 0;
		}

		@Override
		public ItemStack getItem() {
			
			boolean cur = DropletAPI.getServerIcon(DropletAPI.getThisServer()) == material;
			
			Item item = new Item(material).setTitle(ChatColor.YELLOW+WordUtils.capitalizeFully(material.name().replace('_', ' ')));
			
			if(cur){
				item.setLore(ChatColor.GREEN.toString()+ChatColor.BOLD+"Selected.");
				item.addEnchantment(Enchantment.ARROW_DAMAGE, 0);
			}else{
				item.setLore(ChatColor.GRAY+"Click to select.");
			}
			
			return item.build();
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					DropletAPI.setServerIcon(DropletAPI.getThisServer(), material, code -> {
						if(menu instanceof PagedMenu){
							((PagedMenu) menu).remake();
						}
					});
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F);
				}
				
			};
		}
		
	}
	
}
