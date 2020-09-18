package com.playerrealms.droplet.menu.donate;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.md_5.bungee.api.ChatColor;

public class FlameTrail extends Trail {

	private Random random = new Random();
	
	@Override
	public void spawn(Location loc) {
		loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, 0.3, 0.5, 0.3, 0D);
		
		if(random.nextInt(20) == 0) {
			loc.getWorld().spawnParticle(Particle.LAVA, loc, 2, 0.3, 0.5, 0.3);
		}

	}
	

	@Override
	public String getRequirement() {
		return ChatColor.AQUA+"MVP";
	}
	
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.flame";
	}

	@Override
	public String getName() {
		return "FlameTrail";
	}

	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.flame";
	}

	@Override
	public String serialize() {
		return "";
	}

	@Override
	public void deserialize(String data) {
		
	}

}
