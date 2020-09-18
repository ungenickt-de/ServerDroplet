package com.playerrealms.droplet.rank;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;

import java.util.UUID;

public class SetRankRedis implements JedisListener {

	@Override
	public String[] getChannel() {
		return new String[] {"setrank"};
	}

	@Override
	public void onMessage(String ch, String message) {
		String[] splits = message.split(" ");
		UUID id = UUID.fromString(splits[0]);
		String rank = splits[1];
		DropletAPI.setRank(id, ServerDroplet.getInstance().getRank(rank));
	}

}
