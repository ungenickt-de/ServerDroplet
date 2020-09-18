package com.playerrealms.droplet.menu.donate;

import org.bukkit.Location;

import net.md_5.bungee.api.ChatColor;

public class NoTrail extends Trail {

	@Override
	public void spawn(Location loc) {
		
	}
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.none";
	}
	

	@Override
	public String getRequirement() {
		return ChatColor.GREEN+"None";
	}
	

	@Override
	public String getName() {
		return "NoTrail";
	}

	@Override
	public String serialize() {
		return "";
	}

	@Override
	public void deserialize(String data) {
		
	}
	
	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.none";
	}
	
}
