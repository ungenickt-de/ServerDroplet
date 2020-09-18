package com.playerrealms.droplet.menu.shop;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nirvana.menu.AnvilPacketMenu;
import com.nirvana.menu.AnvilPacketMenuHandler;
import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.conversation.AddCommandPrompt;
import com.playerrealms.droplet.lang.Language;

import net.md_5.bungee.api.ChatColor;

public class ShopItemEditMenu extends ChestPacketMenu implements PacketMenuSlotHandler {

	private static final Material[] iconOptions = new Material[] {
			Material.DIAMOND_ORE,
			Material.COAL_ORE,
			Material.EMERALD_ORE,
			Material.GOLD_ORE,
			Material.REDSTONE_ORE,
			Material.IRON_ORE,
			Material.LAPIS_ORE,
			Material.QUARTZ_ORE,
			Material.LOG,
			Material.DIAMOND,
			Material.COAL,
			Material.EMERALD_BLOCK,
			Material.WOOD_SWORD,
			Material.DIAMOND_SWORD,
			Material.GOLD_SWORD,
			Material.IRON_SWORD,
			Material.GOLD_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.GOLD_BLOCK,
			Material.IRON_PICKAXE,
			Material.STONE_PICKAXE,
			Material.WOOD_PICKAXE,
			Material.GOLD_AXE,
			Material.IRON_AXE,
			Material.STONE_AXE,
			Material.WOOD_AXE,
			Material.STONE_SWORD,
			Material.DIAMOND_AXE,
			Material.ANVIL,
			Material.DIRT,
			Material.GRASS,
			Material.BEACON,
			Material.GOLD_NUGGET,
			Material.GHAST_TEAR,
			Material.POTION,
			Material.SPLASH_POTION,
			Material.MELON,
			Material.SEEDS,
			Material.GOLD_INGOT,
			Material.IRON_INGOT,
			Material.COAL,
			Material.REDSTONE,
			Material.REDSTONE_BLOCK,
			Material.BEETROOT_SOUP,
			Material.ARROW,
			Material.BOW,
			Material.BUCKET,
			Material.LAVA_BUCKET,
			Material.WATER_BUCKET,
			Material.APPLE,
			Material.CHEST,
			Material.DIAMOND_CHESTPLATE,
			Material.IRON_CHESTPLATE,
			Material.LEATHER_CHESTPLATE,
			Material.GOLD_CHESTPLATE,
			Material.MUSHROOM_SOUP,
			Material.POTATO_ITEM,
			Material.ANVIL,
			Material.ENCHANTED_BOOK,
			Material.ENCHANTMENT_TABLE,
			Material.LOG
	};
	
	private ServerShopItem item;
	
	public ShopItemEditMenu(Player player, ServerShopItem item) {
		super(9, ChatColor.YELLOW+"Edit "+item.getName());
		this.item = item;
		addGeneralHandler(this);
		init(player);
	}

	private void init(Player player) {
		
		addItem(0, new Item(Material.PAPER).setTitle(ChatColor.YELLOW+"Change Name").build());
		addItem(1, new Item(Material.GOLD_NUGGET).setTitle(ChatColor.YELLOW+"Change Price").build());
		addItem(2, new Item(Material.IRON_NUGGET).setTitle(ChatColor.YELLOW+"Change Sale").build());
		addItem(4, new Item(item.getType()).setTitle(ChatColor.GREEN+"Change Icon").setLore(ChatColor.GRAY+"Change item icon").build());

		addItem(7, new Item(Material.COMMAND).setTitle(ChatColor.GREEN+"Add On-Purchase Command").setLore(ChatColor.GRAY+"Add a command to be executed", ChatColor.GRAY+"when this item is bought.").build());
		addItem(8, new Item(Material.COMMAND_REPEATING).setTitle(ChatColor.RED+"Remove On-Purchase Command").build());
		
		if(item.isHidden()) {
			addItem(5, new Item(Material.POTION).setTitle(ChatColor.GREEN+"Toggle Hide").setLore(Language.getText(player, "hide_item.hidden")).build(), new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					item.setHidden(false);
					ServerDroplet.getInstance().getShop().save();
					
					init(player);
				}
			});
		}else {
			addItem(5, new Item(Material.EXP_BOTTLE).setTitle(ChatColor.GREEN+"Toggle Hide").setLore(Language.getText(player, "hide_item.shown")).build(), new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					item.setHidden(true);
					ServerDroplet.getInstance().getShop().save();
					init(player);
				}
			});
		}
		
		addItem(3, new Item(Material.EMERALD).setTitle(Language.getText(player, "price_type.change")).setLore("", Language.getText(player, "price_type.change_lore", item.getPriceType().name().toLowerCase())).build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				
				ChestPacketMenu select = new ChestPacketMenu(9, "Select");
				
				select.addItem(2, new Item(Material.GOLD_INGOT).setTitle(ChatColor.GOLD+"Coin").build());
				select.addItem(4, new Item(Material.FIREWORK).setTitle(ChatColor.YELLOW+"Vote").build());
				select.addItem(6, new Item(Material.EMERALD).setTitle(ChatColor.AQUA+"Gem").addLore(
						Language.getText(player, "price_type.gems_warning.1"),
						Language.getText(player, "price_type.gems_warning.2"),
						Language.getText(player, "price_type.gems_warning.3")
						).build());
				
				PacketMenuSlotHandler global = new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						if(interactionInfo.getItem().getType() == Material.AIR)
							return;
						if(interactionInfo.getItem().getType() == Material.GOLD_INGOT) {
							item.setPriceType(PriceType.COINS);
						}else if(interactionInfo.getItem().getType() == Material.FIREWORK) {
							item.setPriceType(PriceType.VOTE);
						}else if(interactionInfo.getItem().getType() == Material.EMERALD) {
							item.setPriceType(PriceType.GEMS);
						}
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F);
						ServerDroplet.getInstance().getShop().save();
						ShopItemEditMenu newMenu = new ShopItemEditMenu(player, item);
						newMenu.open(player);
					}
				};
				
				select.addGeneralHandler(global);
				
				select.open(player);
				
			}
			
		});
		
	}

	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		if(interactionInfo.getSlot() == 0){
			AnvilPacketMenu anvil = new AnvilPacketMenu();
			anvil.setDefaultText(item.getName().replace(ChatColor.COLOR_CHAR, '&'));
			anvil.setClickSound(Sound.BLOCK_LEVER_CLICK);
			anvil.setResult(new ItemStack(item.getType(), 1));
			anvil.setHandler(new AnvilPacketMenuHandler() {
				@Override
				public void onResult(String text, Player pl) {
					item.rename(text);
					ShopItemEditMenu editMenu = new ShopItemEditMenu(pl, item);
					editMenu.open(pl);
					ServerDroplet.getInstance().getShop().save();
				}
			});
			anvil.open(player);
		}else if(interactionInfo.getSlot() == 1){
			AnvilPacketMenu anvil = new AnvilPacketMenu();
			anvil.setDefaultText(String.valueOf(item.getPrice()));
			anvil.setClickSound(Sound.BLOCK_LEVER_CLICK);
			anvil.setResult(new ItemStack(Material.GOLD_NUGGET, 1));
			
			anvil.setHandler(new AnvilPacketMenuHandler() {
				@Override
				public void onResult(String text, Player pl) {
					try{
						int cost = Integer.parseInt(text);

						if(cost >= 0 || cost > 1000001) {
							item.changePrice(cost);
						}else{
							Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
						}
					}catch(NumberFormatException e){
						Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
					}
					
					ShopItemEditMenu editMenu = new ShopItemEditMenu(pl, item);
					editMenu.open(pl);
					ServerDroplet.getInstance().getShop().save();
				}
				
			});
			
			anvil.open(player);
		}else if(interactionInfo.getSlot() == 7){
			
			menu.close();
			
			player.sendMessage("");
			Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(new AddCommandPrompt(item)).withEscapeSequence("cancel").buildConversation(player);
			player.beginConversation(conversation);
			player.sendMessage("");
			
		}else if(interactionInfo.getSlot() == 8){
			RemoveActionMenu removeMenu = new RemoveActionMenu(item, player);
			
			removeMenu.open(player);
		}else if(interactionInfo.getSlot() == 4){
			SelectShopItemIconMenu selectMenu = new SelectShopItemIconMenu(player, item, iconOptions);
			
			selectMenu.open(player);
		}else if(interactionInfo.getSlot() == 2){
			AnvilPacketMenu anvil = new AnvilPacketMenu();
			if(item.getSale() == 0){
				anvil.setDefaultText("0-100%");
			}else{
				anvil.setDefaultText(String.valueOf(item.getSale()));
			}
			
			anvil.setClickSound(Sound.BLOCK_LEVER_CLICK);
			anvil.setResult(new ItemStack(Material.GOLD_NUGGET, 1));
			
			anvil.setHandler(new AnvilPacketMenuHandler() {
				
				@Override
				public void onResult(String text, Player pl) {
					if(text.startsWith("%")){
						text = text.substring(1);
					}
					if(text.endsWith("%")){
						text = text.substring(0, text.length()-1);
					}
					try{
						int amount = Integer.parseInt(text);
						if(amount < 0 || amount > 100){
							Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
							return;
						}
						item.changeSale(amount);
					}catch(NumberFormatException e){
						Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
					}
					
					ShopItemEditMenu editMenu = new ShopItemEditMenu(pl,item);
					editMenu.open(pl);
					ServerDroplet.getInstance().getShop().save();
				}
				
			});
			
			anvil.open(player);
		}
	}
	
	

}
