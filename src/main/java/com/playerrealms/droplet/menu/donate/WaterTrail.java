package com.playerrealms.droplet.menu.donate;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.md_5.bungee.api.ChatColor;

public class WaterTrail extends Trail {

	@Override
	public void spawn(Location loc) {
		loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 3, 0.3, 0.3, 0.3);
		loc.getWorld().spawnParticle(Particle.WATER_DROP, loc, 3, 0.3, 0.3, 0.3);
	}

	@Override
	public String getPermission() {
		return "playerrealms.trails.water";
	}
	

	@Override
	public String getRequirement() {
		return ChatColor.AQUA+"MVP";
	}
	
	
	@Override
	public String getName() {
		return "WaterTrail";
	}

	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.water";
	}

	@Override
	public String serialize() {
		return "";
	}

	@Override
	public void deserialize(String data) {

	}

}
