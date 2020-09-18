package com.playerrealms.droplet.menu.donate;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;

import com.playerrealms.droplet.ServerDroplet;

public abstract class Trail {

	public abstract void spawn(Location loc);
	
	public abstract String getName();
	
	public abstract String getDisplayName();
	
	public abstract String serialize();
	
	public abstract void deserialize(String data);
	
	public abstract String getPermission();
	
	public abstract String getRequirement();
	
	public static Trail getTrail(String data) {
		
		String[] args = data.split(" ");
		
		String serialized = null;
		
		if(args.length > 1) {
			serialized = data.substring(data.indexOf(' ')+1);
		}
		
		Trail tr = null;
		
		if(args[0].equalsIgnoreCase("NoTrail")) {
			tr = new NoTrail();
		}else if(args[0].equalsIgnoreCase("ColorTrail")) {
			
			tr = new ColorTrail();
			
		}else if(args[0].equalsIgnoreCase("EnchantTrail")) {
			tr = new EnchantTrail();
		}else if(args[0].equalsIgnoreCase("HeartTrail")) {
			tr = new HeartTrail();
		}else if(args[0].equalsIgnoreCase("WaterTrail")) {
			tr = new WaterTrail();
		}else if(args[0].equalsIgnoreCase("FlameTrail")) {
			tr = new FlameTrail();
		}else if(args[0].equalsIgnoreCase("ImageTrail")) {
			tr = new ImageTrail();
		}else if(args[0].equalsIgnoreCase("CustomImage")) {
			tr = new CustomImageTrail();
		}
		
		if(tr != null & serialized != null) {
			try {
				tr.deserialize(serialized);
			}catch(Exception e) {
				e.printStackTrace();
				return new NoTrail();
			}
		}
		
		return tr;
	}
	
}
