package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.client.redis.RedisInterface;

public class JedisInterface implements RedisInterface {

	@Override
	public void shutdown() {
		
	}

	@Override
	public void subscribe(JedisListener listener) {
		JedisAPI.registerListener(listener);
	}

	@Override
	public void publish(String ch, String msg) {
		JedisAPI.publish(ch, msg);
	}

}
