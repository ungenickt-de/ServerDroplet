package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;

public class VoteConfirmMenu extends ChestPacketMenu implements PacketMenuSlotHandler{

	private int voteSlot;
	private boolean voted;
	
	public VoteConfirmMenu(Player player) {
		super(45, "Vote");
		voted = false;
		voteSlot = new Random().nextInt(getSize());
		for(int i = 0; i < getSize();i++) {
			if(i == voteSlot) {
				addItem(i, new Item(Material.WOOL).setData(5).setTitle(Language.getText(player, "vote_confirm.vote")).build());
			}else {
				addItem(i, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "vote_confirm.cancel")).build());
			}
		}
		addGeneralHandler(this);
	}
	
	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		if(interactionInfo.getSlot() == voteSlot && !voted) {
			menu.close();
			voted = true;
			ServerInformation info = DropletAPI.getThisServer();
			int votes = info.getVotes();
			DropletAPI.setMetadata(info.getName(), "votes", String.valueOf(votes + 1));
			DropletAPI.saveMetadata(info.getName());
			Language.sendMessage(player, "hub.vote.success", info.getName());
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			String jedisKey = "servervote." + player.getUniqueId() + ".time";
			JedisAPI.setKey(jedisKey, String.valueOf(System.currentTimeMillis()));
			JedisAPI.publish("server_vote", player.getUniqueId()+" "+info.getName());
		}else{
			menu.close();
		}
	}
}
