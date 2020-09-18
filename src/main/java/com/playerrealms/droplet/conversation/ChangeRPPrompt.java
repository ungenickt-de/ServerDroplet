package com.playerrealms.droplet.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.playerrealms.droplet.DropletAPI;

import net.md_5.bungee.api.ChatColor;

public class ChangeRPPrompt implements Prompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return ChatColor.YELLOW+"Please enter new Resource Pack URL: (Enter cancel to quit)";
	}

	@Override
	public boolean blocksForInput(ConversationContext context) {
		return true;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "rp", input);
		
		context.getForWhom().sendRawMessage(ChatColor.GRAY+"Resource Pack set to: '"+input+ChatColor.GRAY+"'");
		
		return null;
	}

}
