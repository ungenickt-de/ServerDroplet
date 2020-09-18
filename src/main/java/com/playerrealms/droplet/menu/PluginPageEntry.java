package com.playerrealms.droplet.menu;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.PluginData;

public class PluginPageEntry implements PageMenuEntry {

	private final PluginData plugin;
	
	private boolean enabled;
	
	private UUID uuid;
	
	private boolean sortEnabled;
	
	public PluginPageEntry(PluginData plugin, boolean enabled, Player viewer, boolean sortEnabled) {
		if(plugin == null){
			throw new IllegalArgumentException("plugin cannot be null");
		}
		this.sortEnabled = sortEnabled;
		this.plugin = plugin;
		this.enabled = enabled;
		uuid = viewer.getUniqueId();
	}
	
	public void setSortEnabled(boolean sortEnabled) {
		this.sortEnabled = sortEnabled;
	}
	
	@Override
	public int compareTo(PageMenuEntry o) {
		if(o instanceof PluginPageEntry){
			PluginPageEntry other = (PluginPageEntry) o;
			
			if(sortEnabled && other.sortEnabled) {
				if(enabled) {
					return other.enabled ? 0 : -1;
				}else if(other.enabled) {
					return enabled ? 0 : 1;
				}
			}
			
			if(plugin.isExclusive()){
				return other.plugin.isExclusive() ? 0 : -1;
			}else if(other.plugin.isExclusive()) {
				return plugin.isExclusive() ? 0 : 1;
			}
			return plugin.getName().compareTo(((PluginPageEntry) o).plugin.getName());
		}
		return 0;
	}

	private String getLanguageKey(){
		return "plugins."+plugin.getName();
	}
	
	@Override
	public ItemStack getItem() {
		Player player = Bukkit.getPlayer(uuid);

		List<String> lore = new LinkedList<>();

		Item item = null;

		if(plugin.isHidden() && !player.getName().equals("yukit0308")){
			lore.add(ChatColor.GRAY+"Force disabled by Staff");
			item = new Item(Material.BEDROCK).setTitle(ChatColor.GRAY+plugin.getName()).setLore(lore);
			return item.build();
		}

		if(Language.hasKey(player, getLanguageKey())){
			String description = Language.getText(player, getLanguageKey());
			
			lore.add("");
			lore.add(description);
		}
		
		if(plugin.isExclusive() && !DropletAPI.getThisServer().isUltraPremium()){
			lore.add("");
			lore.add(Language.getText(player, "menu_items.plugin_menu.ultra_only"));
			lore.add(Language.getText(player, "menu_items.plugin_menu.ultra_only_desc", Language.getText(player, "generic.store_url")));
		}
		
		if(plugin.getDependencies().size() > 0){
			lore.add("");
			lore.add(Language.getText(player, "menu_items.plugin_menu.dependencies"));
			for(String depend : plugin.getDependencies()){
				lore.add(ChatColor.GRAY+depend);
			}
		}

		if(enabled){
			lore.add(0, Language.getText(player, "menu_items.plugin_menu.enabled"));
			item = new Item(Material.INK_SACK).setData(10).setTitle(ChatColor.GRAY+plugin.getName()).setLore(lore);
		}else{
			lore.add(0, Language.getText(player, "menu_items.plugin_menu.disabled"));
			item = new Item(Material.INK_SACK).setData(1).setTitle(ChatColor.GRAY+plugin.getName()).setLore(lore);
		}
		
		if(plugin.isExclusive()){
			item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		}

		if(plugin.isHidden()){
			item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		}

		return item.build();
	}

	@Override
	public PacketMenuSlotHandler getHandler() {
		return new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				if(plugin.isHidden() && !player.getName().equals("yukit0308")){
					return;
				}

				if(plugin.isExclusive() && !DropletAPI.getThisServer().isUltraPremium() && !enabled){
					return;
				}
				
				enabled = !enabled;
				
				ServerDroplet.getInstance().getPlugins().put(plugin, enabled);
				
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F);
				
				sortEnabled = false;
				
				if(menu instanceof PagedMenu){
					Bukkit.getScheduler().runTaskLater(ServerDroplet.getInstance(), () -> {
						((PagedMenu) menu).remake(false);
					}, 3);
				}
				
			}
		};
	}

}
