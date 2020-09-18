package com.playerrealms.droplet.menu.shop;

import org.bukkit.Material;
import org.bukkit.Sound;
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
import com.playerrealms.droplet.lang.Language;

public class ConfigureServerShopMenu extends ChestPacketMenu implements PacketMenuSlotHandler {

	public ConfigureServerShopMenu(Player player) {
		super(9, "Configure Server Shop");
		addGeneralHandler(this);
		init(player);
	}
	
	private void init(Player player){
		addItem(0, new Item(Material.CHEST).setTitle(Language.getText(player, "menu_items.shop_config.add.title")).setLore(Language.getText(player, "menu_items.shop_config.add.lore")).build());
		
		if(ServerDroplet.getInstance().getShop().getItems().size() > 0){
			addItem(1, new Item(Material.LAVA_BUCKET).setTitle(Language.getText(player, "menu_items.shop_config.remove.title")).setLore(Language.getText(player, "menu_items.shop_config.remove.lore")).build());
			addItem(2, new Item(Material.REDSTONE).setTitle(Language.getText(player, "menu_items.shop_config.edit.title")).setLore(Language.getText(player, "menu_items.shop_config.edit.lore")).build());
			addItem(3, new Item(Material.ENDER_CHEST).setTitle(Language.getText(player, "menu_items.shop_config.view.title")).setLore(Language.getText(player, "menu_items.shop_config.view.lore")).build());
		}
		
	}

	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		if(interactionInfo.getItem().getType() == Material.CHEST){
			
			AnvilPacketMenu anvil = new AnvilPacketMenu();
			anvil.setDefaultText(Language.getText(player, "menu_items.shop_config.enter_name"));
			anvil.setClickSound(Sound.BLOCK_LEVER_CLICK);
			anvil.setResult(new ItemStack(Material.APPLE, 1));
			
			anvil.setHandler(new AnvilPacketMenuHandler() {
				
				@Override
				public void onResult(String text, Player pl) {
					ServerShopItem item = new ServerShopItem();
					item.rename(text);
					ServerDroplet.getInstance().getShop().addItem(item);
					ConfigureServerShopMenu newMenu = new ConfigureServerShopMenu(pl);
					newMenu.open(pl);
				}
				
			});
			
			anvil.open(player);
			
			
		}else if(interactionInfo.getItem().getType() == Material.LAVA_BUCKET){
			
			ServerShopDeleteMenu deleteMenu = new ServerShopDeleteMenu(ServerDroplet.getInstance().getShop(), player);
			
			deleteMenu.open(player);
			
		}else if(interactionInfo.getItem().getType() == Material.REDSTONE){
			ServerShopEditMenu edit = new ServerShopEditMenu(ServerDroplet.getInstance().getShop(), player);
			
			edit.open(player);
			
		}else if(interactionInfo.getItem().getType() == Material.ENDER_CHEST){
			ServerShopMenu shop = new ServerShopMenu(ServerDroplet.getInstance().getShop(), player);
			
			shop.open(player);
		}
	}

}
