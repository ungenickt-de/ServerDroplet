package com.playerrealms.droplet.menu.hub;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;

import net.md_5.bungee.api.ChatColor;

public class DeleteServerMenu extends ChestPacketMenu {

	public DeleteServerMenu(ServerInformation server, int round, Player player) {
		super(54, "Click the diamond " + ChatColor.DARK_RED + "(" + round + ")");
		init(server, player, round);
	}
	
	private void init(ServerInformation server, Player player, int round)
	{
		for(int i = 0; i < this.getSize(); i++)
		{
			addItem(i, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "menu_items.delete_server_confirm.cancel")).build());
		}
		Random random = new Random();
		int slot = random.nextInt(getSize());
		addItem(slot, new Item(Material.DIAMOND).setTitle(Language.getText(player, "menu_items.delete_server_confirm.confirm")).build());
		addGeneralHandler(new PacketMenuSlotHandler()
		{
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				if(interactionInfo.getSlot() == slot)
				{
					if(round == 1)
					{
						menu.close();
						DropletAPI.deleteServerData(server.getName(), code -> {
							if(code == ResponseCodes.SERVER_REMOVED)
							{
								Language.sendMessage(player, "response_codes.server_removed");
								DropletAPI.setMetadata(server.getName(), "redo", "true");
							}
							else
							{
								Language.sendMessage(player, "response_codes.server_unknown_error");
							}
						});
					}
					else
					{
						new DeleteServerMenu(server, round-1, player).open(player);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
					}
				}
				else
				{
					menu.close();
				}
			}
		});
	}

}
