package com.playerrealms.droplet.menu.donate;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;

public class GlobalGadgetMenu extends ChestPacketMenu {
	
	
	private static final PacketMenuSlotHandler FIREWORK = new PacketMenuSlotHandler() {
		
		@Override
		public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
			
			for(Player victim : player.getWorld().getPlayers()) {
				Location loc = victim.getLocation();
				Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
				firework.setMetadata("donate", new FixedMetadataValue(ServerDroplet.getInstance(), true));
				FireworkMeta fireworkMeta = firework.getFireworkMeta();
				Random random = new Random();
				
				Color c1 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
				Color c2 = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
				
				FireworkEffect effect = FireworkEffect.builder().flicker(random.nextBoolean()).withColor(c1).withFade(c2).with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)]).trail(random.nextBoolean()).build();
				fireworkMeta.addEffect(effect);
				fireworkMeta.setPower(random.nextInt(2) + 1);
				firework.setFireworkMeta(fireworkMeta);
			}
			
		}
	};

	private static final PacketMenuSlotHandler LAVA = new PacketMenuSlotHandler() {

		@Override
		public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
			
			AtomicReference<BukkitTask> task = new AtomicReference<BukkitTask>(null);
			//Silly java I guess
			task.set(Bukkit.getScheduler().runTaskTimer(ServerDroplet.getInstance(), new Runnable() {
				
				int times = 50;
				
				@Override
				public void run() {
					if(times > 0) {
						
						player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 3);
						times--;
					}else {
						task.get().cancel();
					}
				}
			}, 5, 2));
			
			
		}
		
	};
	
	private static final PacketMenuSlotHandler HEART = new PacketMenuSlotHandler() {

		@Override
		public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
			
			AtomicReference<BukkitTask> task = new AtomicReference<BukkitTask>(null);
			//Silly java I guess
			task.set(Bukkit.getScheduler().runTaskTimer(ServerDroplet.getInstance(), new Runnable() {
				
				int times = 50;
				
				@Override
				public void run() {
					if(times > 0) {
						
						player.getWorld().spawnParticle(Particle.HEART, player.getEyeLocation(), 3);
						
						times--;
					}else {
						task.get().cancel();
					}
				}
			}, 5, 2));
			
			
		}
		
	};
	
	private static final PacketMenuSlotHandler TOTEM = new PacketMenuSlotHandler() {

		@Override
		public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
			
			AtomicReference<BukkitTask> task = new AtomicReference<BukkitTask>(null);
			//Silly java I guess
			task.set(Bukkit.getScheduler().runTaskTimer(ServerDroplet.getInstance(), new Runnable() {
				
				int times = 50;
				
				@Override
				public void run() {
					if(times > 0) {
						
						player.getWorld().spawnParticle(Particle.SLIME, player.getEyeLocation(), 3);
						
						
						times--;
					}else {
						task.get().cancel();
					}
				}
			}, 5, 2));
			
			
		}
		
	};
	
	public GlobalGadgetMenu(Player pl) {
		super(3 * 9, "Fun");
	
		createUpdateTask(new Runnable() {
			
			@Override
			public void run() {
				
				/*addAbility(translateCoord(2, 2), pl, "donate_powers.firework.title", new String[] {"donate_powers.firework.lore"} , Material.FIREWORK, "firework", TimeUnit.MINUTES.toMillis(8), FIREWORK);
			
				addAbility(translateCoord(4, 2), pl, "donate_powers.lava.title", new String[] {"donate_powers.lava.lore"} , Material.LAVA_BUCKET, "lava", TimeUnit.MINUTES.toMillis(8), LAVA);
				
				addAbility(translateCoord(6, 2), pl, "donate_powers.heart.title", new String[] {"donate_powers.heart.lore"} , Material.BONE, "heart", TimeUnit.MINUTES.toMillis(8), HEART);
				
				addAbility(translateCoord(8, 2), pl, "donate_powers.totem.title", new String[] {"donate_powers.totem.lore"} , Material.TOTEM, "totem", TimeUnit.MINUTES.toMillis(8), TOTEM);
				*/
				
				Trail current = DropletAPI.getTrail(pl);
				
				if(current == null) {
					current = new NoTrail();
				}
				
				addItem(5, 1, new Item(Material.REDSTONE).setTitle(Language.getText(pl, "donate_powers.trail.title")).setLore(Language.getText(pl, "donate_powers.trail.lore", Language.getText(pl, current.getDisplayName()))).build(), new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						TrailMenu trailMenu = new TrailMenu();
						trailMenu.open(player);
					}
				});
				
				
			}
		}, 0, 20);
		
	}
	
	private static long getLastUsed(Player player, String key) {
		String val = JedisAPI.getCachedValue("donate_power."+player.getUniqueId()+"."+key, 60000);
		return val == null ? 0L : Long.parseLong(val);
	}
	
	private void addAbility(int slot, Player pl, String langTitle, String[] langLore, Material display, String lastUsedKey, long coolDown, PacketMenuSlotHandler handler) {

		String[] lore = new String[langLore.length + 1];
		
		for(int i = 0; i < langLore.length;i++) {
			lore[i] = Language.getText(pl, langLore[i]);
		}
		
		long waitTimeLeft = coolDown - (System.currentTimeMillis() - getLastUsed(pl, lastUsedKey));
		
		if(waitTimeLeft < 1000 && waitTimeLeft > 0) {
			waitTimeLeft = 1000;
		}
		
		if(waitTimeLeft > 0) {
			lore[lore.length - 1] = Language.getText(pl, "donate_powers.cooldown", waitTimeLeft / 1000);
		}else {
			lore[lore.length - 1] = Language.getText(pl, "donate_powers.click_to_use");
		}
		
		if(waitTimeLeft > 0) {
			addItem(slot, new Item(display)
					.setTitle(Language.getText(pl, langTitle))
					.setLore(lore).build());
		}else {
			addItem(slot, new Item(display)
					.setTitle(Language.getText(pl, langTitle))
					.setLore(lore).build(), new PacketMenuSlotHandler() {
						
						@Override
						public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
							handler.onClicked(player, menu, interactionInfo);
							JedisAPI.setKey("donate_power."+player.getUniqueId()+"."+lastUsedKey, String.valueOf(System.currentTimeMillis()));
							menu.close();
						}
					});
		}
	
	}

}
