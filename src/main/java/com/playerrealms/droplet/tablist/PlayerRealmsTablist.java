package com.playerrealms.droplet.tablist;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.rank.Rank;

public class PlayerRealmsTablist implements Listener {
	public PlayerRealmsTablist() {

/*		Bukkit.getScheduler().runTaskTimer(ServerDroplet.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				
				for(Player player : Bukkit.getOnlinePlayers()){
					updateTabListFor(player);
				}
				
			}
		}, 0, 100);*/
		
	}

	private void updateTabListFor(Player player){
		for(Player other : Bukkit.getOnlinePlayers()){
			Rank otherRank = DropletAPI.getRank(other);
			createName(otherRank.getPrefix(), "", player, other, otherRank.getSortRank());
		}
	}
	
	private void updateTabListSingle(Player player, Player other){
		Rank otherRank = DropletAPI.getRank(other);
		createName(otherRank.getPrefix(), "", player, other, otherRank.getSortRank());	
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if(player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()){
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}

		for (Player pl : Bukkit.getOnlinePlayers()) {
			updateTabListSingle(pl, player);
		}

		updateTabListFor(player);
	}
	
	private Scoreboard getScoreboard(Player pl){
		return pl.getScoreboard();
	}
	
	private void createName(String prefix, String suffix, Player observer, Player observed, TabListSortRank rank){
		
		String teamName = rank.getPrefix()+observed.getName();
		
		Scoreboard sb = getScoreboard(observer);
		
		if(teamName.length() > 16){
			teamName = teamName.substring(0, 16);
		}
		
		if(sb.getTeam(teamName) == null){
			Team newTeam = sb.registerNewTeam(teamName);
			newTeam.addEntry(observer.getName());
			newTeam.setAllowFriendlyFire(true);
		}
		
		Team team = sb.getTeam(teamName);
		if(!team.getEntries().contains(observed.getName())){
			team.addEntry(observed.getName());
		}
		
		if(!prefix.endsWith(" ") && prefix.length() < 16 && ChatColor.stripColor(prefix).length() > 0){
			prefix = prefix + " ";
		}
		
		if(!suffix.startsWith(" ") && suffix.length() < 16  && ChatColor.stripColor(suffix).length() > 0){
			suffix = " " + suffix;
		}
		
		if(prefix.length() > 16){
			prefix = prefix.substring(0, 15);
		}
		
		if(suffix.length() > 16){
			suffix = suffix.substring(0, 15);
		}
		
		
		if(!team.getPrefix().equals(prefix)){
			team.setPrefix(prefix);
		}
		
		if(!team.getSuffix().equals(suffix)){
			team.setSuffix(suffix);
		}
		
	}
	
}
