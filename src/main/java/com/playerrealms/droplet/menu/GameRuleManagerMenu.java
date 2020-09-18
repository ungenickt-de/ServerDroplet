package com.playerrealms.droplet.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;

public class GameRuleManagerMenu extends ChestPacketMenu {
	
	static final List<String> BANNED = Arrays.asList(new String[] {
			"commandBlockOutput",
			"sendCommandFeedback",
			"logAdminCommands",
			"reducedDebugInfo"
	});
	
	public GameRuleManagerMenu(World world) {
		super(27, "Editting '"+world.getName()+"' Gamerules");
		initialize(world);
	}
	
	private void initialize(World world){
		
		int i = 0;
		
		for(String rule : world.getGameRules()){
			
			String value = world.getGameRuleValue(rule).toLowerCase();
			
			if(!value.equals("false") && !value.equals("true")){
				continue;
			}
			
			if(BANNED.contains(rule)){
				continue;
			}
			
			boolean bValue = Boolean.parseBoolean(value);
			
			if(bValue){
				addItem(i, new Item(Material.WATER_BUCKET).setTitle(ChatColor.GRAY+rule).setLore(ChatColor.GREEN+"Enabled \u2713").build(), new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						world.setGameRuleValue(rule, String.valueOf(!bValue));
						initialize(world);
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
					}
					
				});
			}else{
				addItem(i, new Item(Material.BUCKET).setTitle(ChatColor.GRAY+rule).setLore(ChatColor.RED+"Disabled \u2717").build(), new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						world.setGameRuleValue(rule, String.valueOf(!bValue));
						initialize(world);
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
					}
					
				});
			}
			i++;
			
		}
		
	}

}
