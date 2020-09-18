package com.playerrealms.droplet.redis;

import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.common.RedisConstants;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.*;

public class JedisAPI {

	private static JedisPool pool;
	
	private static Map<String, CachedValue> cache = Collections.synchronizedMap(new HashMap<>());
	
	private static JedisPubSub pubSub;
	private static BukkitTask task;
	
	private static List<JedisListener> listeners;
	
	private static Map<String, String> offlineRedis = new HashMap<>();
	
	public static void setup(String host, int port, String password){
		if(pool != null){
			throw new IllegalStateException("Jedis already initialized");
		}
		
		JedisPoolConfig config = new JedisPoolConfig();
		
		config.setMaxTotal(200);
		
		pool = new JedisPool(config, host, port, 5000, password);
		
		listeners = Collections.synchronizedList(new ArrayList<>());
		
		registerListener(new TimedRewardListener());
		registerListener(new VoteListener());
		registerListener(new GiveCratesListener());
		registerListener(new OfficialGameBroadcastListener());
		registerListener(new DonatorBonusListener());
		registerListener(new ServerVoteListener());
		registerListener(new ServerCmListener());
		registerListener(new GrantGemsListener());
		//registerListener(new ReportListener());
		//registerListener(new AdminChatListener());
		//registerListener(new DonorChatListener());
		
		task = Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {
			@Override
			public void run() {
				pubSub = null;
				try (Jedis jedis = getJedis()) {
					pubSub = new JedisPubSub() {

						@Override
						public void onMessage(String channel, String message) {
							if(!ServerDroplet.getInstance().isEnabled()) {
								pubSub.unsubscribe();
								return;
							}
							try{
								if (channel.equals("cacheUpdate")) {
									synchronized (cache) {
										cache.remove(message);
									}
								}else if(channel.equals("app_login")) {
									UUID id = UUID.fromString(message);
									
									Bukkit.getScheduler().runTask(ServerDroplet.getInstance(), () -> {
										
										Player player = Bukkit.getPlayer(id);
										
										Language.sendMessage(player, "app.login");
										
									});
								}else{
									synchronized (listeners) {
										for(JedisListener jl : listeners){
											for(String ch : jl.getChannel()) {
												if(ch.equals(channel)) {
													Bukkit.getScheduler().runTask(ServerDroplet.getInstance(), () -> jl.onMessage(channel, message));
													break;
												}
											}
										}
									}
								}
							}catch(Exception e){
								e.printStackTrace();
							}
							
						}

					};
					
					jedis.subscribe(pubSub,
							"cacheUpdate",
							"timed_reward",
							"vote", 
							"rankReload",
							"listReload",
							"setrank", 
							"givecoins",
							"broadcast", 
							"peak",
							"givecrates",
							"ban", 
							"official",
							"donator_bonus", 
							"server_vote", 
							"app_login",
							"server_cm",
							"grantgems",
							RedisConstants.MANAGER_RESPONSE_CHANNEL,
							RedisConstants.MANAGER_UPDATE_CHANNEL);
				}finally{
					pubSub.unsubscribe();
				}
				ServerDroplet.getInstance().getLogger().info("Jedis pubsub ended");
			}
		});

	}
	
	public static void publish(String channel, String msg){
		if(!isValid()) {
			return;
		}
		try(Jedis jedis = getJedis()){
			jedis.publish(channel, msg);
		}
	}
	
	public static void registerListener(JedisListener listener){
		if(!isValid()) {
			return;
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public static boolean isValid(){
		if(pool == null){
			return false;
		}
		try(Jedis jedis = getJedis()){
			return jedis.isConnected();
		}
	}
	
	public static void destroy(){
		if(!isValid()) {
			return;
		}
		task.cancel();
		pool.destroy();
	}
	
	private static Jedis getJedis(){
		return pool.getResource();
	}
	
	public static void setKey(String key, String value){
		if(!isValid()) {
			offlineRedis.put(key, value);
			return;
		}
		try(Jedis jedis = getJedis()){
			jedis.set(key, value);
			synchronized (cache) {
				if(cache.containsKey(key)){
					CachedValue old = cache.get(key);
					jedis.publish("cacheUpdate", key);
					cache.put(key, old);
				}
			}
			
		}
	}
	
	public static void setKeyExpire(String key, String value, int expireTime){
		if(!isValid()) {
			offlineRedis.put(key, value);
			return;
		}
		try(Jedis jedis = getJedis()){
			jedis.setex(key, expireTime, value);
			synchronized (cache) {
				if(cache.containsKey(key)){
					CachedValue old = cache.get(key);
					jedis.publish("cacheUpdate", key);
					cache.put(key, old);
				}
			}
		}
	}
	
	public static String getValue(String key){
		if(!isValid()) {
			return offlineRedis.get(key);
		}
		try(Jedis jedis = getJedis()){
			return jedis.get(key);
		}
	}
	
	public static String getCachedValue(String key, long cacheTime){
		if(!isValid()) {
			return offlineRedis.get(key);
		}
		synchronized (cache) {
			if(cache.containsKey(key)){
				CachedValue value = cache.get(key);
				
				if(value.expireTime - System.currentTimeMillis() > 0){
					return value.value;
				}
			}
			
			String value = getValue(key);
			
			CachedValue cv = new CachedValue();
			cv.value = value;
			cv.expireTime = System.currentTimeMillis() + cacheTime;
			
			cache.put(key, cv);
			
			return value;
		}
	}
	
	public static void removeKey(String key) {
		if(!isValid()) {
			offlineRedis.remove(key);
			return;
		}
		try(Jedis jedis = getJedis()){
			jedis.del(key);
			cache.remove(key);
		}
	}

	public static long getTTL(String key, boolean ms) {
		if(!isValid()) {
			return 60000;
		}
		try(Jedis jedis = getJedis()){
			if(ms){
				return jedis.pttl(key);
			}else{
				return jedis.ttl(key);
			}
		}
	}
	
	public static boolean keyExists(String key){
		if(!isValid()) {
			return offlineRedis.containsKey(key);
		}
		if(cache.containsKey(key)) {
			return true;
		}
		try(Jedis jedis = getJedis()){
			return jedis.exists(key);
		}
	}
	
	public static void cacheKey(String key, long time) {
		getCachedValue(key, time);
	}
	
	static class CachedValue {
		private String value;
		private long expireTime;
	}


	
}
