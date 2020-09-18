package com.playerrealms.droplet;

import com.nirvana.menu.*;
import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.rank.Rank;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.util.PunchCardApi;
import com.playerrealms.droplet.util.PunchCardApi.PunchDays;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DropletListener implements Listener {

	private static Map<String, String> kicks = new HashMap<>();
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent event) {
		if(DropletAPI.getThisServer().getServerType() == ServerType.PLAYER){
			Rank rank = DropletAPI.getRank(event.getPlayer());
			if (rank.hasPermission("playerrealms.bypass.admin")) {
				event.setCancelled(true);
				return;
			}
		}

		String reason = event.getReason();
		
		if(!kicks.containsKey(reason)) {
			long id = new Random().nextLong();
			String random = String.valueOf(id);
			kicks.put(random, reason);
			
			event.setCancelled(true);
			
			UUID player = event.getPlayer().getUniqueId();
			ServerDroplet.getInstance().getLogger().info("Scheduling kick for "+event.getPlayer().getName()+" with id "+random);
			Bukkit.getScheduler().runTaskLater(ServerDroplet.getInstance(), new Runnable() {
				
				@Override
				public void run() {
					Player pl = Bukkit.getPlayer(player);
					
					if(pl != null) {
						pl.kickPlayer(random);
					}
				}
			}, 5);
		}else {
			event.setReason(kicks.get(reason));
			kicks.remove(reason);
			ServerDroplet.getInstance().getLogger().info("Kicked player "+event.getPlayer().getName()+" "+reason);
		}
		
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		boolean msg = true;
		if(JedisAPI.keyExists("hiderank.toggle."+player.getUniqueId()) || JedisAPI.keyExists("vanish.toggle."+player.getUniqueId())){
			msg = false;
		}
		if (msg && player.hasPermission("playerrealms.rank.vip"))
		{
			Rank rank = DropletAPI.getRank(player);
			event.setJoinMessage(rank.getPrefix() + " " + player.getName() + ChatColor.YELLOW + " has joined the game!");
		}
		else
		{
			event.setJoinMessage(null);
		}

		if(DropletAPI.getThisServer().isUltraPremium() && DropletAPI.getThisServer().getResourcePack() != null) {
			ChestPacketMenu menu = new ChestPacketMenu(9, Language.getText(event.getPlayer(), "resource_pack.title"));
			menu.addItem(4, new Item(Material.WOOL).setData(DyeColor.GREEN.getDyeData()).setTitle(Language.getText(event.getPlayer(), "resource_pack.accept"))
					.setLore(Language.getText(event.getPlayer(), "resource_pack.yes_lore")).build(), new PacketMenuSlotHandler() {

				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					player.setResourcePack(DropletAPI.getThisServer().getResourcePack());
				}
			});
			menu.addItem(8, new Item(Material.WOOL).setData(DyeColor.RED.getDyeData()).setTitle(Language.getText(event.getPlayer(), "resource_pack.deny"))
					.setLore(Language.getText(event.getPlayer(), "resource_pack.no_lore")).build(), new PacketMenuSlotHandler() {

				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					menu.close();
				}
			});
			menu.addItem(0, new Item(Material.WOOL).setData(DyeColor.RED.getDyeData()).setTitle(Language.getText(event.getPlayer(), "resource_pack.deny"))
					.setLore(Language.getText(event.getPlayer(), "resource_pack.no_lore")).build(), new PacketMenuSlotHandler() {

				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					menu.close();
				}
			});
			Bukkit.getScheduler().runTaskLater(ServerDroplet.getInstance(), () -> menu.open(event.getPlayer()), 5);
		}
		UUID plUuid = player.getUniqueId();
		try {
			if(PunchCardApi.getPunchedDays(player, false).size() == PunchDays.values().length - 1) {
				Bukkit.getScheduler().runTask(ServerDroplet.getInstance(), () -> {
					Player pl = Bukkit.getPlayer(plUuid);
					if(pl != null) {
						pl.getWorld().spawn(pl.getLocation(), Firework.class, fw -> {

							FireworkEffect effect = FireworkEffect.builder().flicker(true).trail(false).with(Type.BALL).withColor(Color.fromRGB(255, 215, 0)).build();
							FireworkMeta meta = fw.getFireworkMeta();
							meta.clearEffects();
							meta.addEffect(effect);
							meta.setPower(0);

							fw.setFireworkMeta(meta);
							fw.detonate();

						});
					}
				});
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onCommand(ServerCommandEvent e){
		if(e.getCommand().equals("stop")){
			Bukkit.shutdown();
		}
	}
}
