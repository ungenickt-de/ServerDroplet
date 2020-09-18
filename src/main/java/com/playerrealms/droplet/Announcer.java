package com.playerrealms.droplet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.lang.Language;

public class Announcer implements Runnable {

	private String[] messages;
	
	private int i;
	
	public Announcer(String... messages) {
		this.messages = messages;
		i = 0;
	}

	@Override
	public void run() {
		if(DropletAPI.getThisServer().isUltraPremium()){
			return;
		}
		String msg = messages[i];
		
		for(Player pl : Bukkit.getOnlinePlayers()){
			Language.sendMessage(pl, msg);
		}
		
		i++;
		if(i >= messages.length){
			i = 0;
		}
	}
	
}
