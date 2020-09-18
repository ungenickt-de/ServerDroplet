package com.playerrealms.droplet.menu.shop.actions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.menu.shop.ServerShopItemAction;

public class CommandShopAction implements ServerShopItemAction {

	private String command;
	
	public CommandShopAction(String command) {
		this.command = command;
		if(this.command.startsWith("/")){
			this.command = this.command.substring(1);
		}
	}
	
	public CommandShopAction() {
		
	}
	
	@Override
	public void doAction(Player player) {
		if(command.contains("@a")){
			for(Player other : Bukkit.getOnlinePlayers()){
				String cmd = command.replaceAll("@a", other.getName()).replaceAll("{player}", player.getName()).replaceAll("@p", player.getName());
				
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}else{
			String cmd = command.replace("{player}", player.getName()).replaceAll("@p", player.getName());
			
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
		}
		
	}

	@Override
	public String getType() {
		return "command";
	}

	@Override
	public void serialize(ConfigurationSection config) {
		config.set("command", command);
	}

	@Override
	public void deserialize(ConfigurationSection config) {
		command = config.getString("command");
	}

	@Override
	public String getItemLore(Player player) {
		String cmd = command.replace("{player}", player.getName()).replaceAll("@p", player.getName());
		return "Command : '"+cmd+"'";
	}
	
}
