package com.playerrealms.droplet.commands;

import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

import static com.playerrealms.droplet.ServerDroplet.getInstance;

public class VanishCommand implements Listener {
    private static ArrayList<Player> hiddenUsers = new ArrayList<Player>();

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        if(isVanished(e.getPlayer())){
            e.setJoinMessage(null);
        }
        final Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(DropletAPI.getRank(player).hasPermission("playerrealms.vanish") && isVanished(player)){
                    vanishPlayer(player);
                }
                if(!DropletAPI.getRank(player).hasPermission("playerrealms.vanish")){
                    for (Player pl : hiddenUsers){
                        pl.hidePlayer(player);
                    }
                }
            }
        });
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e){
        if(isVanished(e.getPlayer())){
            e.setQuitMessage(null);
        }
        hiddenUsers.remove(e.getPlayer());
        e.getPlayer().removeMetadata("vanished", getInstance());
    }

   /*@EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if(event.getTarget() instanceof Player) {
            if(isVanished((Player) event.getTarget())) {
                event.setCancelled(true);
                return;
            }
            return;
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if(isVanished((Player)event.getEntity())){
                event.setCancelled(true);
                return;
            }
            return;
        } else if (event.getEntity() instanceof Player) {
            if(isVanished((Player)event.getEntity())) {
                event.setCancelled(true);
                return;
            }
            return;
        }
    }*/

    public static boolean isVanished(Player player) {
        String ToggleKey = "vanish.toggle." + player.getUniqueId();
        return JedisAPI.keyExists(ToggleKey);
    }

    public static void vanishPlayer(Player player) {
        for (Player pl : getInstance().getServer().getOnlinePlayers()){
            if(pl == player || DropletAPI.getRank(pl).hasPermission("playerrealms.vanish")){
                continue;
            }
            pl.hidePlayer(player);
        }
        JedisAPI.setKey("vanish.toggle." + player.getUniqueId(), "on");
        hiddenUsers.add(player);
        player.setMetadata("vanished", new FixedMetadataValue(getInstance(), true));
        Language.sendMessage(player, "vanish.turn_on");
    }

    public static void showPlayer(Player player) {
        for (Player pl : getInstance().getServer().getOnlinePlayers()){
            pl.showPlayer(player);
        }
        JedisAPI.removeKey("vanish.toggle." + player.getUniqueId());
        hiddenUsers.remove(player);
        player.removeMetadata("vanished", getInstance());
        Language.sendMessage(player, "vanish.turn_off");
    }
}
