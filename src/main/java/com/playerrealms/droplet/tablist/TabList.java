package com.playerrealms.droplet.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class TabList {

	private Map<UUID, PrefixSuffix> entries;
	
	private Map<UUID, CachedPrefixSuffix> cachedEntries;
	
	private Set<UUID> viewers;
	
	private boolean changeFound;
	
	public TabList() {
		entries = new HashMap<>();
		viewers = new HashSet();
		changeFound = false;
	}
	
	public void addViewer(Player player) {
		viewers.add(player.getUniqueId());
		
		if(player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()	) {
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}

		Scoreboard sb = player.getScoreboard();
	}
	
	private void sendAll(Scoreboard sb) {}
	
	public void addEntry(UUID uuid, PrefixSuffix ps) {
		entries.put(uuid, ps);
		cachedEntries.put(uuid, new CachedPrefixSuffix(ps));
	}
	
	public void recreateCache() {
		
	}
	
}
