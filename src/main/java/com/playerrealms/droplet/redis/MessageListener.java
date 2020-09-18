package com.playerrealms.droplet.redis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.lang.Language;

public class MessageListener implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"broadcast"};
	}

	@Override
	public void onMessage(String ch, String message) {
		String[] args = message.split(" ");
		Object[] o_args = new Object[args.length - 1];
		for(int i = 1; i < args.length;i++) {
			o_args[i-1] = args[i];
		}
		for(Player pl : Bukkit.getOnlinePlayers()){
			pl.sendMessage(Language.getText(pl, args[0], o_args));
		}
	}
}
