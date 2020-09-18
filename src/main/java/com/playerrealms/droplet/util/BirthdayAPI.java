package com.playerrealms.droplet.util;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.crypto.Data;

import com.playerrealms.droplet.sql.DatabaseAPI;
import com.playerrealms.droplet.sql.QueryResult;

public class BirthdayAPI {

	public static boolean isBirthdaySet(UUID player) throws SQLException{
		
		List<QueryResult> results = DatabaseAPI.query("SELECT * FROM `birthdays` WHERE `uuid`=?", player.toString());
		
		return results.size() > 0;
	}
	
	public static Birthday getBirthday(UUID player) throws SQLException{
		List<QueryResult> results = DatabaseAPI.query("SELECT * FROM `birthdays` WHERE `uuid`=?", player.toString());
		
		if(results.isEmpty())
			return null;
		
		Birthday birthday = new Birthday();
		
		QueryResult result = results.get(0);
		
		birthday.day = result.get("day");
		birthday.month = result.get("month");
		birthday.lastPrizeYear = result.get("last_reward_year");
		
		return birthday;
	}
	
	public static void updateBirthday(UUID player, int month, int day) throws SQLException{
		if(isBirthdaySet(player))
			return;
		
		DatabaseAPI.execute("INSERT INTO `birthdays` (`uuid`, `month`, `day`) VALUES (?, ?, ?)", player.toString(), month, day);
	}
	
	public static void updatePrizeYear(UUID player) throws SQLException{
		if(!isBirthdaySet(player))
			return;
		TimeZone zone = TimeZone.getTimeZone("Asia/Tokyo");
		Calendar cal = Calendar.getInstance(zone);
		
		int year = cal.get(Calendar.YEAR);
		
		DatabaseAPI.execute("UPDATE `birthdays` SET `last_reward_year`=? WHERE `uuid`=?", year, player.toString());
	}
	
	public static class Birthday {
		private int day, month;
		private int lastPrizeYear;
		
		public int getDay() {
			return day;
		}
		
		public int getMonth() {
			return month;
		}
		
		public boolean canClaimPrize(){
			
			TimeZone zone = TimeZone.getTimeZone("Asia/Tokyo");
			Calendar cal = Calendar.getInstance(zone);
			
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			
			if(this.month == month && this.day == day){
				
				if(lastPrizeYear != year){
					return true;
				}
				
			}
			
			return false;
			
		}
	}
	
}
