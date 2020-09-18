package com.playerrealms.droplet.menu.donate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.playerrealms.droplet.DropletAPI;

public class TrailRunner implements Runnable {

	@Override
	public void run() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			
			if(player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				continue;
			}
			
			Trail trail = DropletAPI.getTrail(player);
			
			if(trail != null) {
				
				trail.spawn(player.getLocation());
				
			}
			
		}
	}

}
