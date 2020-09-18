package com.playerrealms.droplet.menu.donate;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.nirvana.menu.menus.PageMenuEntry;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;

import net.md_5.bungee.api.ChatColor;

public class TrailEffectEntry implements PageMenuEntry {

	private String name;
	private int data;
	private Material material;
	private Trail trail;
	
	public TrailEffectEntry(String name, Material mat, Trail trail) {
		this(name, mat, 0, trail);
	}
	
	public TrailEffectEntry(String name, Material mat, int data, Trail trail) {
		this.name = name;
		this.material = mat;
		this.data = data;
		this.trail = trail;
	}
	
	@Override
	public int compareTo(PageMenuEntry o) {
		return 0;
	}

	@Override
	public ItemStack getItem() {
		return new Item(material).setTitle(name).setData(data).setLore(ChatColor.GRAY+"Click to Select", "", ChatColor.GRAY+"Need "+trail.getRequirement()).build();
	}

	@Override
	public PacketMenuSlotHandler getHandler() {
		return new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				
				if(DropletAPI.getRank(player).hasPermission(trail.getPermission())) {
					DropletAPI.setTrail(player, trail);
					Language.sendMessage(player, "donate_powers.trail.selected", Language.getText(player, trail.getDisplayName()));
					menu.close();
				}else {
					Language.sendMessage(player, "donate_powers.trail.no_perm", Language.getText(player, trail.getDisplayName()));
				}
				
				
			}
		};
	}

}
