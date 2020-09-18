package com.playerrealms.droplet;

import java.util.concurrent.TimeUnit;

import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerType;

public class DailyTimeLimitManager {

	public static int getRemainingMinutes() {
		
		ServerInformation info = DropletAPI.getThisServer();
		
		
		return 0;
		
	}
	
	public static int getMaxMinutes() {
		ServerInformation info = DropletAPI.getThisServer();
		
		if(info.isPremium() || info.isUltraPremium() || info.isOfficial() || info.getServerType() == ServerType.HUB) {
			return -1;
		}
		
		return 0;
	}
	
}
