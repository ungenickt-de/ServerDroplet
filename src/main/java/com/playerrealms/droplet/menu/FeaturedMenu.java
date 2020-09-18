package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class FeaturedMenu extends ChestPacketMenu implements PacketMenuSlotHandler {
	
	private static final int IRON_COST = 10000;
	private static final int GOLD_COST = (int) (0.95 * IRON_COST * 12);
	private static final int DIAMOND_COST = (int) (0.90F * IRON_COST * 24);
	
	public FeaturedMenu(Player pl) {
		super(54, ChatColor.GRAY+"Power up");
		init(pl);
		addGeneralHandler(this);
		setClickSound(Sound.BLOCK_NOTE_BASS);
	}
	
	private void init(Player pl){
		ServerInformation featured = DropletAPI.getFeaturedServer();
		
		for(int i = 0; i < 9;i++){
			for(int j = 0; j < 6;j++){
				
				if(i == 0 || i == 8 || j == 0 || j == 5){
					addItem(i+1, j+1, new Item(Material.STAINED_GLASS_PANE).setData(1).setTitle(" ").build());
				}else if(i == 2 && j == 3){
					if(featured == null)
						addItem(i + 1, j + 1, new Item(Material.IRON_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.iron")).setLore("", Language.getText(pl, "menu_items.featured.duration", 1), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", IRON_COST)).build());
				}else if(i == 4 && j == 1){
					if(featured == null)
						addItem(i + 1, j + 1, new Item(Material.DIAMOND_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.diamond")).setLore("", Language.getText(pl, "menu_items.featured.duration", 24), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", DIAMOND_COST), Language.getText(pl, "menu_items.premium.percent_off", 12)).build());
				}else if(i == 6 && j == 3){
					if(featured == null)
						addItem(i + 1, j + 1, new Item(Material.GOLD_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.gold")).setLore("", Language.getText(pl, "menu_items.featured.duration", 12), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", GOLD_COST), Language.getText(pl, "menu_items.premium.percent_off", 5)).build());
				}else if(i == 4 && j == 3){
					
					long featuredTime = DropletAPI.getFeaturedTimeLeft();
					
					if(featured == null){
						addItem(i + 1, j + 1, new Item(Material.COAL).setTitle(Language.getText(pl, "menu_items.featured.not_featured")).setLore(Language.getText(pl, "menu_items.featured.click_to_buy")).build());
					}else if(!featured.equals(DropletAPI.getThisServer())){
						long hoursLeft = TimeUnit.MILLISECONDS.toHours(featuredTime);
						long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(featuredTime) - hoursLeft * 60;
						addItem(i + 1, j + 1, new Item(Material.WATCH)
								.setAmount((int) hoursLeft)
								.setTitle(Language.getText(pl, "menu_items.featured.activated_other"))
								.setLore(Language.getText(pl, "menu_items.featured.time", hoursLeft, hoursLeft, minutesLeft)).build());
					}else{
						long hoursLeft = TimeUnit.MILLISECONDS.toHours(featuredTime);
						long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(featuredTime) - hoursLeft * 60;
						addItem(i + 1, j + 1, new Item(Material.EMERALD)
								.setAmount(Math.max((int) hoursLeft, 1))
								.setTitle(Language.getText(pl, "menu_items.featured.activated"))
								.setLore(Language.getText(pl, "menu_items.featured.time", hoursLeft, hoursLeft, minutesLeft)).build());
						
					}
				}else if(i == 1 && j == 1){
					addItem(i + 1, j + 1, new Item(Material.GOLD_NUGGET)
							.setTitle(ChatColor.LIGHT_PURPLE.toString()+ChatColor.BOLD+"Realm"+ChatColor.GOLD+ChatColor.BOLD+"Coins")
							.setLore(
									Language.getText(pl, "menu_items.premium.coins", DropletAPI.getCoins(pl)),
									"",
									Language.getText(pl, "menu_items.premium.need_coins"),
									Language.getText(pl, "menu_items.premium.check_store_at"),
									ChatColor.GREEN+Language.getText(pl, "generic.store_url")).build());
				}
				
			}
		}
	}

	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		int hours = 0;
		int cost = 0;
		
		if(interactionInfo.getSlot() == translateCoord(3, 4)){
			hours = 1;
			cost = IRON_COST;
		}else if(interactionInfo.getSlot() == translateCoord(5, 2)){
			hours = 24;
			cost = DIAMOND_COST;
		}else if(interactionInfo.getSlot() == translateCoord(7, 4)){
			hours = 12;
			cost = GOLD_COST;
		}
		
		if(DropletAPI.getFeaturedServer() != null){
			new FeaturedMenu(player).open(player);
			return;
		}
		
		if(hours > 0 && cost > 0){
			int money = DropletAPI.getCoins(player);
			
			if(money < cost){
				Language.sendMessage(player, "menu.premium.need_coins");
			}else{
				close();
				Language.sendMessage(player, "menu.premium.purchasing");

				if(!ServerDroplet.isClientActive()){
					Language.sendMessage(player, "menu.premium.failed");
					return;
				}

				DropletAPI.setCoins(player, money - cost);
				
				DropletAPI.setFeaturedServer(DropletAPI.getThisServer(), hours * 60 * 60);
				
				DropletAPI.giveCoinBonus(DropletAPI.getThisServer(), 0.5D, TimeUnit.HOURS.toMillis(hours));
				
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
				Language.sendMessage(player, "menu.premium.purchased");
				new FeaturedMenu(player).open(player);
				
				
				
			}
		}
	}

}
