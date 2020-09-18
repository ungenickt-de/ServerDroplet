package com.playerrealms.droplet.menu.donate;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.md_5.bungee.api.ChatColor;

public class EnchantTrail extends Trail {

	@Override
	public void spawn(Location loc) {
		loc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 4);
	}

	@Override
	public String getName() {
		return "EnchantTrail";
	}

	@Override
	public String serialize() {
		return "";
	}

	@Override
	public void deserialize(String data) {
		
	}
	
	@Override
	public String getRequirement() {
		return ChatColor.AQUA+"MVP";
	}
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.enchant";
	}
	
	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.enchant";
	}

}
