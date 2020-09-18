package com.playerrealms.droplet.menu.donate;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.md_5.bungee.api.ChatColor;

public class HeartTrail extends Trail {

	@Override
	public void spawn(Location loc) {
		loc.getWorld().spawnParticle(Particle.HEART, loc, 1, 0.3, 0.1, 0.3);
	}

	@Override
	public String getName() {
		return "HeartTrail";
	}
	

	@Override
	public String getRequirement() {
		return ChatColor.AQUA+"MVP";
	}
	
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.heart";
	}

	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.hearts";
	}

	@Override
	public String serialize() {
		return "";
	}

	@Override
	public void deserialize(String data) {

	}

}
