package com.playerrealms.droplet.lang;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class LanguagePacketListener extends PacketAdapter {

	public LanguagePacketListener(Plugin plugin) {
		super(plugin, PacketType.Play.Client.SETTINGS);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if(event.getPacketType() == PacketType.Play.Client.SETTINGS){
			String lang = event.getPacket().getStrings().read(0);
			
			Language.setLanguage(event.getPlayer(), lang.toLowerCase());
		}
	}

}
