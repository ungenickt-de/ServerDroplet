package com.playerrealms.droplet.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.playerrealms.droplet.DropletAPI;

import net.md_5.bungee.api.ChatColor;

public class ChangeMOTDPrompt implements Prompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return ChatColor.YELLOW+"Please enter new MOTD: (Enter cancel to quit)";
	}

	@Override
	public boolean blocksForInput(ConversationContext context) {
		return true;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if(input.length() > 100){
			input = input.substring(0, 100);
		}
		if(DropletAPI.getThisServer().isPremium()){
			input = ChatColor.translateAlternateColorCodes('&', input);
		}
		DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "motd", input);
		
		context.getForWhom().sendRawMessage(ChatColor.GRAY+"MOTD set to: '"+input+ChatColor.GRAY+"'");
		
		return null;
	}

}
