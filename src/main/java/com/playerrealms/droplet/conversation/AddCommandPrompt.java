package com.playerrealms.droplet.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.menu.shop.ServerShopItem;
import com.playerrealms.droplet.menu.shop.ShopItemEditMenu;
import com.playerrealms.droplet.menu.shop.actions.CommandShopAction;

import net.md_5.bungee.api.ChatColor;

public class AddCommandPrompt implements Prompt {

	private ServerShopItem item;
	
	public AddCommandPrompt(ServerShopItem item) {
		this.item = item;
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		return ChatColor.YELLOW+"Please enter command (@p and @a can be used): (Enter cancel to quit)";
	}

	@Override
	public boolean blocksForInput(ConversationContext context) {
		return true;
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		context.getForWhom().sendRawMessage(ChatColor.GRAY+"Added command '"+input+ChatColor.GRAY+"'");
		
		item.getPurchaseActions().add(new CommandShopAction(input));
		ServerDroplet.getInstance().getShop().save();
		
		ShopItemEditMenu menu = new ShopItemEditMenu((Player) context.getForWhom(), item);
		
		menu.open((Player) context.getForWhom());
		
		return null;
	}

}
