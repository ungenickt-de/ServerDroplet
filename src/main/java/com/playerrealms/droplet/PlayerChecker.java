package com.playerrealms.droplet;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.lang.Language;

public class PlayerChecker implements Runnable {

	private long aloneTime = -1;
	
	@Override
	public void run() {
		
		if(Bukkit.getOnlinePlayers().size() == 0) {
			Bukkit.shutdown();
		}
		
/*		if(Bukkit.getOnlinePlayers().size() == 1) {
			
			if(aloneTime == -1) {
				aloneTime = System.currentTimeMillis();
			}else {
				long time = System.currentTimeMillis() - aloneTime;
				
				if(TimeUnit.MINUTES.toMillis(30) <= time) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						Language.sendMessage(player, "trouble.", values);
					}
				}
			}
			
		}*/
		
	}

}
