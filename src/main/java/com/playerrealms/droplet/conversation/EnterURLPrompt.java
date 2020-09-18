package com.playerrealms.droplet.conversation;

import java.util.function.Consumer;

import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class EnterURLPrompt implements Prompt {

	private Consumer<String> urlConsumer;
	
	private String prompt;
	
	public EnterURLPrompt(Consumer<String> urlConsumer, String promptText) {
		this.urlConsumer = urlConsumer;
		prompt = promptText;
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		return prompt;
	}

	@Override
	public boolean blocksForInput(ConversationContext context) {
		return true;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		
		UrlValidator validator = new UrlValidator();
		
		if(validator.isValid(input)){
			urlConsumer.accept(input);
		}else{
			Player pl = (Player) context.getForWhom();
			
			pl.sendMessage(ChatColor.RED+input+" is not a valid URL.");
		}
		
		
		return null;
	}

}
