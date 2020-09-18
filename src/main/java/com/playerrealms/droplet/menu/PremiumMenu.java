package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PremiumMenu extends ChestPacketMenu implements PacketMenuSlotHandler {
	
	private static final int IRON_COST = 50000 * 2;
	private static final int GOLD_COST = 95000 * 2;
	private static final int DIAMOND_COST = 132000 * 2;
	
	
	public PremiumMenu(Player pl) {
		super(54, ChatColor.GRAY+"Premium");
		init(pl);
		addGeneralHandler(this);
		setClickSound(Sound.BLOCK_NOTE_BASS);
	}
	
	private void init(Player pl){
		for(int i = 0; i < 9;i++){
			for(int j = 0; j < 6;j++){
				
				if(i == 0 || i == 8 || j == 0 || j == 5){
					addItem(i+1, j+1, new Item(Material.STAINED_GLASS_PANE).setData(1).setTitle(" ").build());
				}else if(i == 2 && j == 3){
					addItem(i + 1, j + 1, new Item(Material.IRON_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.iron")).setLore("", Language.getText(pl, "menu_items.premium.duration", 1), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", IRON_COST)).build());
				}else if(i == 4 && j == 1){
					addItem(i + 1, j + 1, new Item(Material.DIAMOND_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.diamond")).setLore("", Language.getText(pl, "menu_items.premium.duration", 3), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", DIAMOND_COST), Language.getText(pl, "menu_items.premium.percent_off", 12)).build());
				}else if(i == 6 && j == 3){
					addItem(i + 1, j + 1, new Item(Material.GOLD_BLOCK).setTitle(Language.getText(pl, "menu_items.premium.gold")).setLore("", Language.getText(pl, "menu_items.premium.duration", 2), "", Language.getText(pl, "menu_items.premium.cost_title"), Language.getText(pl, "menu_items.premium.cost", GOLD_COST), Language.getText(pl, "menu_items.premium.percent_off", 5)).build());
				}else if(i == 4 && j == 3){
					if(!DropletAPI.getThisServer().isPremium()){
						addItem(i + 1, j + 1, new Item(Material.COAL).setTitle(Language.getText(pl, "menu_items.premium.no_premium")).setLore(Language.getText(pl, "menu_items.premium.click_to_buy")).build());
					}else{
						long premiumLeft = DropletAPI.getThisServer().getPremiumLeft();
						long daysLeft = TimeUnit.MILLISECONDS.toDays(premiumLeft);
						long hoursLeft = TimeUnit.MILLISECONDS.toHours(premiumLeft) - daysLeft * 24;
						long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(premiumLeft) - (daysLeft * 24 + hoursLeft) * 60;
						if(daysLeft <= 64){
							addItem(i + 1, j + 1, new Item(Material.EMERALD)
									.setAmount((int) daysLeft)
									.setTitle(Language.getText(pl, "menu_items.premium.activated"))
									.setLore(Language.getText(pl, "menu_items.premium.time", daysLeft, hoursLeft, minutesLeft)).build());
						}else{
							addItem(i + 1, j + 1, new Item(Material.EMERALD)
									.setAmount(1)
									.addEnchantment(Enchantment.DAMAGE_ALL, 1)
									.setTitle(Language.getText(pl, "menu_items.premium.activated"))
									.setLore(Language.getText(pl, "menu_items.premium.time", daysLeft, hoursLeft, minutesLeft)).build());
						}
						
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
		int days = 0;
		int cost = 0;
		
		if(interactionInfo.getSlot() == translateCoord(3, 4)){
			days = 7;
			cost = IRON_COST;
		}else if(interactionInfo.getSlot() == translateCoord(5, 2)){
			days = 21;
			cost = DIAMOND_COST;
		}else if(interactionInfo.getSlot() == translateCoord(7, 4)){
			days = 14;
			cost = GOLD_COST;
		}
		
		if(days > 0 && cost > 0){
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
				
				DropletAPI.addPremiumTime(DropletAPI.getThisServer(), TimeUnit.DAYS.toMillis(days), code -> {
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
					Language.sendMessage(player, "menu.premium.purchased");
					new PremiumMenu(player).open(player);
				});
				
			}
		}
	}

}
