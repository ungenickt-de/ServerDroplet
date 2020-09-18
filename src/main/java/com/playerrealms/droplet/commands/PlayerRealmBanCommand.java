package com.playerrealms.droplet.commands;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.util.MojangAPI;
import com.playerrealms.droplet.util.MojangAPI.NameLookupException;
import com.playerrealms.droplet.util.MojangAPI.NameNotFoundException;

import redis.clients.jedis.Jedis;

public class PlayerRealmBanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length < 3) {
			return false;
		}
		
		String playerName = args[0];
		
		int time = Integer.parseInt(args[1]);
		
		TimeUnit unit = null;
		
		String unitName = args[2];
		
		String type = args[3];
		
		int typeId = 0;
		
		if(type.equalsIgnoreCase("play")) {
			typeId = DropletAPI.BAN_TYPE_PLAY;
		}else if(type.equalsIgnoreCase("create")) {
			typeId = DropletAPI.BAN_TYPE_CREATE;
		}else if(type.equalsIgnoreCase("both")) {
			typeId = DropletAPI.BAN_TYPE_BOTH;
		}else {
			return false;
		}
		
		String reason = "BANNED!!! (no reason supplied)";
		
		if(time < 0) {
			sender.sendMessage(ChatColor.RED+"Time must be zero or positive (zero will unban)");
		}
		
		if(args.length > 4) {
			reason = "";
			for(int i = 4; i < args.length;i++) {
				reason += args[i] + " ";
			}
		}
		
		if(reason.contains("%")) {
			sender.sendMessage(ChatColor.RED+"Reason cannot contain '%'");
			return true;
		}
		
		if(unitName.equalsIgnoreCase("sec")) {
			unit = TimeUnit.SECONDS;
		}else if(unitName.equalsIgnoreCase("min")) {
			unit = TimeUnit.MINUTES;
		}else if(unitName.equalsIgnoreCase("hour")) {
			unit = TimeUnit.HOURS;
		}else if(unitName.equalsIgnoreCase("day")) {
			unit = TimeUnit.DAYS;
		}else {
			sender.sendMessage("Unknown unit "+unitName+". Valid units: sec,min,hour,day");
			return true;
		}
		
		long realTime = unit.toMillis(time) + System.currentTimeMillis();
		
		try {
			UUID uuid = MojangAPI.getUUID(playerName);
			
			DatabaseAPI.execute("UPDATE `players` SET `ban_expire_time`=?, `ban_reason`=?, `ban_moderator`=?, `ban_type`=? WHERE `uuid`=?", realTime, reason, sender.getName(), typeId, uuid.toString());
			
			sender.sendMessage(ChatColor.RED+"Banned "+playerName+" ("+uuid+") for "+time+" "+unit.name().toLowerCase());
			 
			JedisAPI.publish("ban", uuid+" "+reason.replace(' ', '%')+" "+realTime+" "+sender.getName());
			
		} catch(NameNotFoundException e) {
			sender.sendMessage(ChatColor.RED+"Could not find player named "+playerName);
		} catch (NameLookupException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.DARK_RED+"Unknown error");
		} catch (SQLException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.DARK_RED+"Unknown error (DB)");
		}
		
		
		return true;
	}
	
	private static boolean isValidNumber(String s) {
		try {
			Integer.parseInt(s);
		}catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

}
