package com.playerrealms.droplet.menu.donate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DonateEffectListener implements Listener {

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager().hasMetadata("donate")) {
			event.setCancelled(true);
		}
	}
	
}
