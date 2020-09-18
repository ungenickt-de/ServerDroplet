package com.playerrealms.droplet.redis;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.crates.CrateAPI;
import com.playerrealms.droplet.crates.GlobalCrateTypes;
import com.playerrealms.droplet.lang.Language;

public class VoteListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"vote"};
	}

	@Override
	public void onMessage(String ch, String message) {
		String name = message.split(" ")[0];
		int amount = Integer.parseInt(message.split(" ")[1]);

		Player pl = Bukkit.getPlayer(name);
		if (pl != null) {
			try {
				CrateAPI.giveCrate(pl.getUniqueId(), GlobalCrateTypes.VOTE.getTypeString());
				Language.sendMessage(pl, "generic.vote", amount);
				for(Player other : Bukkit.getOnlinePlayers()) {
					Language.sendMessage(other, "generic.vote_other", DropletAPI.getRank(pl).getPlayerName(pl.getName()), Language.getText(other, "crates.name.global", GlobalCrateTypes.VOTE.getName(other)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
