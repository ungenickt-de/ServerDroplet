package com.playerrealms.droplet.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.nirvana.menu.Item;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;

public class PunchCardApi {

	private static Map<UUID, List<PunchDays>> cache = new HashMap<>();
	
	public static List<PunchDays> getPunchedDays(Player player, boolean checkCache) throws SQLException{
		if(checkCache) {
			if(cache.containsKey(player.getUniqueId())) {
				return cache.get(player.getUniqueId());
			}
		}
		List<QueryResult> results = DatabaseAPI.query("SELECT `day` FROM `golden_week` WHERE `uuid`=?", player.getUniqueId().toString());
		
		List<PunchDays> days = results.stream().map(result -> PunchDays.valueOf(result.get("day"))).collect(Collectors.toList());
		
		cache.put(player.getUniqueId(), days);
		
		return days;
	}
	
	public static void punchCard(Player player, PunchDays day) throws SQLException{
		if(!getPunchedDays(player, false).contains(day)) {
			DatabaseAPI.execute("INSERT INTO `golden_week` (`uuid`,`day`) VALUES (?, ?)", player.getUniqueId().toString(), day.toString());
			if(cache.containsKey(player.getUniqueId())) {
				cache.get(player.getUniqueId()).add(day);
			}
		}
	}
	
	public static PunchDays getToday() {
		
		TimeZone zone = TimeZone.getTimeZone("Asia/Tokyo");
		Calendar cal = Calendar.getInstance(zone);
		
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		if(month == 3) {
			if(day == 29) {
				return PunchDays.DAY_29;
			}else if(day == 30){
				return PunchDays.DAY_30;
			}
		}else if(month == 4) {
			if(day == 3) {
				return PunchDays.DAY_3;
			}else if(day == 4) {
				return PunchDays.DAY_4;
			}else if(day == 5) {
				return PunchDays.DAY_5;
			}else if(day == 6) {
				return PunchDays.DAY_6;
			}
		}
		
		if(isWelcomeBackEvent()){
			long startTime = Long.parseLong(JedisAPI.getCachedValue("welcome_back_start", 10000));
			
			
			startTime = System.currentTimeMillis() - startTime;
			
			long days = TimeUnit.MILLISECONDS.toDays(startTime);
			
			if(days == 0){
				return PunchDays.WELCOME_BACK_ONE;
			}else if(days == 1){
				return PunchDays.WELCOME_BACK_TWO;
			}else if(days == 2){
				return PunchDays.WELCOME_BACK_THREE;
			}else if(days == 3){
				return PunchDays.WELCOME_BACK_FOUR;
			}else if(days == 4){
				return PunchDays.WELCOME_BACK_FIVE;
			}else if(days == 5){
				return PunchDays.WELCOME_BACK_SIX;
			}
		}
		
		return PunchDays.UNKNOWN;
	}
	
	
	public static boolean isGoldenWeek() {
/*		TimeZone zone = TimeZone.getTimeZone("Asia/Tokyo");
		Calendar cal = Calendar.getInstance(zone);
		
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		if(month == 3) {
			return day >= 29 && day <= 30;
		}else if(month == 4) {
			return day >= 1 && day <= 6;
		}
		
		return month >= 4 && month <= 5;*/
		return JedisAPI.keyExists("golden_week") && JedisAPI.getValue("golden_week").equals("true");
	}
	
	public static boolean isWelcomeBackEvent(){
		return JedisAPI.keyExists("welcome_back") && JedisAPI.getCachedValue("welcome_back", 30000).equals("true");
	}

	@SuppressWarnings("deprecation")
	public static void givePunchCard(Player player, int slot, MapRenderer renderer, String cardName) {
		Player pl = player;
		
		ItemStack item = new Item(Material.MAP).setTitle(cardName).build();
		
		MapView view = Bukkit.createMap(pl.getWorld());
		view.getRenderers().clear();
		view.addRenderer(renderer);
		item.setDurability(view.getId());
		
		player.getInventory().setItem(slot, item);
	}

	public static enum PunchDays {
		DAY_29,
		DAY_30,
		DAY_3,
		DAY_4,
		DAY_5,
		DAY_6,
		WELCOME_BACK_ONE,
		WELCOME_BACK_TWO,
		WELCOME_BACK_THREE,
		WELCOME_BACK_FOUR,
		WELCOME_BACK_FIVE,
		WELCOME_BACK_SIX,
		UNKNOWN;
	}

	
}
