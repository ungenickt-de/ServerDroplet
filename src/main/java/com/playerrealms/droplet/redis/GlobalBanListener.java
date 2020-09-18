package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GlobalBanListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"ban"};
	}

	@Override
	public void onMessage(String ch, String message) {
		ServerDroplet.getInstance().getLogger().info("Global Ban "+message);
		String[] splits = message.split(" ");
		
		UUID id = UUID.fromString(splits[0]);
		String reason = splits[1].replace('%', ' ');
		long time = Long.parseLong(splits[2]);
		String banner = splits[3];
		
		Player pl = Bukkit.getPlayer(id);
		
		if(pl == null) {
			//ServerDroplet.getInstance().getLogger().info("Player not found ("+id+")");
			return;
		}
		
		if(pl != null) {
			long timeLeft = time - System.currentTimeMillis();
			if(timeLeft > 0) {
				reason += "\n";
				reason += Language.getText(pl, "global_ban.expire_time", formatBanTime(timeLeft, Language.getLanguage(Language.getLocale(pl))));
				
				for(Player other : Bukkit.getOnlinePlayers()) {
					Language.sendMessage(other, "global_ban.announcement", pl.getName(), banner);
				}
				pl.kickPlayer(ChatColor.RED+reason);
			}
		}
	}
	
	public static String formatBanTime(long timeLeft, Language lang) {
		String tlString = "";
		
		if(TimeUnit.DAYS.toMillis(1) <= timeLeft) {
			long units = timeLeft / TimeUnit.DAYS.toMillis(1);
			timeLeft -= units * TimeUnit.DAYS.toMillis(1);
			tlString += lang.getText("global_ban.days", units)+" ";	
		}
		if(TimeUnit.HOURS.toMillis(1) <= timeLeft) {
			long hours = timeLeft / TimeUnit.HOURS.toMillis(1);
			timeLeft -= hours * TimeUnit.HOURS.toMillis(1);
			tlString += lang.getText("global_ban.hours", hours)+" ";	
		}
		if(TimeUnit.MINUTES.toMillis(1) <= timeLeft) {
			long units = timeLeft / TimeUnit.MINUTES.toMillis(1);
			timeLeft -= units * TimeUnit.MINUTES.toMillis(1);
			tlString += lang.getText("global_ban.minutes", units)+" ";	
		}
		if(TimeUnit.SECONDS.toMillis(1) <= timeLeft) {
			long units = timeLeft / TimeUnit.SECONDS.toMillis(1);
			timeLeft -= units * TimeUnit.SECONDS.toMillis(1);
			tlString += lang.getText("global_ban.seconds", units)+" ";	
		}
		return tlString;
	}

}
