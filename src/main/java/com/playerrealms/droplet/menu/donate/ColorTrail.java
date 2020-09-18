package com.playerrealms.droplet.menu.donate;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;

import com.playerrealms.droplet.DropletAPI;

import net.md_5.bungee.api.ChatColor;

public class ColorTrail extends Trail {

	private float r, g, b;
	private Random random = new Random();
	
	public ColorTrail() {
		
	}
	
	public ColorTrail(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	@Override
	public void spawn(Location loc) {
		
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + random.nextFloat() - 0.5F, loc.getY() + random.nextFloat(), loc.getZ() + random.nextFloat() - 0.5F, 0, r, g, b);
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + random.nextFloat() - 0.5F, loc.getY() + random.nextFloat(), loc.getZ() + random.nextFloat() - 0.5F, 0, r, g, b);
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + random.nextFloat() - 0.5F, loc.getY() + random.nextFloat(), loc.getZ() + random.nextFloat() - 0.5F, 0, r, g, b);
	}
	
	@Override
	public String getRequirement() {
		return ChatColor.YELLOW+"VIP";
	}
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.color";
	}

	@Override
	public String getName() {
		return "ColorTrail";
	}

	@Override
	public String serialize() {
		return r+" "+g+" "+b;
	}

	@Override
	public void deserialize(String data) {
		String[] args = data.split(" ");
		
		r = Float.parseFloat(args[0]);
		g = Float.parseFloat(args[1]);
		b = Float.parseFloat(args[2]);
		
	}
	
	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.color";
	}
	
}
