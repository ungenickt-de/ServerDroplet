package com.playerrealms.droplet.menu.hub;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;

import net.md_5.bungee.api.ChatColor;

public class ServerManagerMenu extends ChestPacketMenu {

	public ServerManagerMenu(Player player) {
		super(27, "Server Manager");
		setupMenu(player);
	}
	
	private void setupMenu(Player player)
	{
		List<ServerInformation> servers = DropletAPI.getByOwner(player);
		if(servers.isEmpty())
		{
			addItem(13, new Item(Material.ANVIL).setTitle(Language.getText(player, "menu_items.manage.create")).build(), new PacketMenuSlotHandler()
			{
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					SelectServerTypeMenu sstm = new SelectServerTypeMenu(player);
					
					sstm.open(player);	
				}
			});
			
		}else{
			for(ServerInformation info : servers){
				ChatColor color = info.getStatus() == ServerStatus.ONLINE ? ChatColor.GREEN : ChatColor.RED;
				addItem(new Item(DropletAPI.getServerIcon(info)).setTitle(color+info.getName()).setLore(ChatColor.GRAY+"Click to manage").build(), new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						new ServerMenu(info, player).open(player);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
					}
				});
			}
		}
	}

}
