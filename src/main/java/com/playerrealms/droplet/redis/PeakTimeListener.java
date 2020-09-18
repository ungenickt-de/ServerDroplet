package com.playerrealms.droplet.redis;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;

public class PeakTimeListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"peak"};
	}

	@Override
	public void onMessage(String ch, String message) {
		if(DropletAPI.getThisServer().getServerType() == ServerType.PLAYER){
			long startTime = DropletAPI.getThisServer().getStartTime();
			
			if(System.currentTimeMillis() - startTime > 60000){
				if(Bukkit.getOnlinePlayers().size() == 0){
					Bukkit.shutdown();
				}
			}
		}
	}

}
