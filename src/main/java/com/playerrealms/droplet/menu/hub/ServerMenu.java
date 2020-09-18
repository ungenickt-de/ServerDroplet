package com.playerrealms.droplet.menu.hub;

import com.nirvana.menu.*;
import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class ServerMenu extends ChestPacketMenu {

	public ServerMenu(ServerInformation server, Player player) {
		this(server, player, false);
	}
	
	public ServerMenu(ServerInformation server, Player player, boolean debug) {
		super(9, server.getName());
		if(debug){
			setupDebug(server, player);
		}else{
			setupMenu(server, player);
		}
	}
	
	public void setupDebug(ServerInformation server, Player player){
		int color = 0;
		
		if(server.getStatus() == ServerStatus.ONLINE){
			color = 14;
		}else if(server.getStatus() == ServerStatus.OFFLINE){
			color = 5;
		}else{
			color = 4;
		}
		
		addItem(0, new Item(Material.WOOL).setData(color).setTitle(ChatColor.AQUA+"Status").setLore(server.getStatus().name()).build());
		
		addItem(1, new Item(Material.ARROW).setTitle(ChatColor.GREEN+"Force Start").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				DropletAPI.startServer(server.getName(), true, code -> {
					if(code == ResponseCodes.SERVER_STARTING)
					{
						Language.sendMessage(player, "menu.server_menu.server_starting");
						DropletAPI.setMetadata(server.getName(), "redo", "");
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
					}
					else if(code == ResponseCodes.SERVER_ALREADY_RUNNING)
					{
						Language.sendMessage(player, "menu.server_menu.already_online");
					}else if(code == ResponseCodes.MEMORY_LIMIT_REACHED){
						Language.sendMessage(player, "response_codes.memory_limit_reached");
					}
					else 
					{
						Language.sendMessage(player, "menu.server_menu.failed");
					}
				});
			}
		});
		
		addItem(2, new Item(Material.FLINT).setTitle(ChatColor.RED+"Stop Gracefully").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				DropletAPI.stopServer(server.getName(), false, code -> {
					if(code == ResponseCodes.SERVER_STOPPED)
					{
						Language.sendMessage(player, "menu.server_menu.server_stopped");
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
					}
					else if(code == ResponseCodes.SERVER_NOT_RUNNING)
					{
						Language.sendMessage(player, "menu.server_menu.server_not_running");
					}
					else
					{
						Language.sendMessage(player, "menu.server_menu.could_not_stop_server");
					}
				});
			}
		});
		
		addItem(3, new Item(Material.TOTEM).setTitle(ChatColor.GOLD+"Set Featured").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				DropletAPI.setFeaturedServer(server);
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
			}
		});
		
		addItem(4, new Item(Material.DIAMOND_AXE).setTitle("Restart").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				DropletAPI.restartServer(server.getName());
			}
		});
		
		addItem(5, new Item(Material.GOLD_NUGGET).setTitle(ChatColor.YELLOW+"Add Coin Bonus").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				
				AnvilPacketMenu a1 = new AnvilPacketMenu();
				a1.setDefaultText("Enter Hours");
				a1.setResult(new ItemStack(Material.GOLD_NUGGET));
				a1.setHandler(new AnvilPacketMenuHandler() {
					
					@Override
					public void onResult(String text, Player pl) {
						try {
							int hours = Integer.parseInt(text);
							
							AnvilPacketMenu a2 = new AnvilPacketMenu();
							a2.setDefaultText("Enter Multiplier 0.5=+50%");
							a2.setResult(new ItemStack(Material.DIAMOND));
							a2.setHandler(new AnvilPacketMenuHandler() {
								
								@Override
								public void onResult(String text, Player pl) {
									try {
										double bonus = Double.parseDouble(text);
										
										long bonusTime = server.getCoinMultiplierTimeLeft();
										
										bonusTime += TimeUnit.HOURS.toMillis(hours);
										
										bonusTime += System.currentTimeMillis();
										
										DropletAPI.setMetadata(server.getName(), "multi", String.valueOf(bonus));
										DropletAPI.setMetadata(server.getName(), "multitime", String.valueOf(bonusTime));
										
									} catch (NumberFormatException e) {
										Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
									}
								}
							});
							
							Bukkit.getScheduler().runTaskLater(ServerDroplet.getInstance(), () -> a2.open(pl), 5);
							
						} catch (NumberFormatException e) {
							Language.sendMessage(pl, "menu.edit_store_item.invalid_price");
						}
					}
				});
				
				a1.open(player);
			}
			
		});
		
		addItem(6, new Item(Material.APPLE).setTitle(ChatColor.YELLOW+"Information").setLore(
				ChatColor.GREEN+"TPS "+server.getTPS(),
				ChatColor.YELLOW+"Coin Bonus: "+((int) (server.getCoinMultiplier() * 100))+"% "+TimeUnit.MILLISECONDS.toMinutes(server.getCoinMultiplierTimeLeft())+" minutes",
				ChatColor.GOLD+"Premium Time Left: "+TimeUnit.MILLISECONDS.toMinutes(server.getPremiumLeft())+" minutes",
				ChatColor.DARK_GREEN+"Earnings: "+server.getEarnings(),
				ChatColor.AQUA+"Port: "+server.getPort()).build());
		
		addItem(7, new Item(Material.OBSIDIAN).setTitle(ChatColor.LIGHT_PURPLE+"Connect Current").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				for(Player other : Bukkit.getOnlinePlayers()){
					if(other == player){
						continue;
					}
					DropletAPI.connectToServer(other, server);
				}
			}
		});
		
		addItem(8, new Item(Material.FLINT_AND_STEEL).setTitle(ChatColor.DARK_RED+"Kill Server").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				DropletAPI.stopServer(server.getName(), true, code -> {
					if(code == ResponseCodes.SERVER_FORCE_STOPPED)
					{
						Language.sendMessage(player, "menu.server_menu.server_stopped");
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
					}
					else if(code == ResponseCodes.SERVER_NOT_RUNNING)
					{
						Language.sendMessage(player, "menu.server_menu.server_not_running");
					}
					else
					{
						Language.sendMessage(player, "menu.server_menu.could_not_stop_server");
					}
				});
			}
		});
		
	}
	
	public void setupMenu(ServerInformation server, Player player)
	{
		if(server.isBan()){
			addItem(4, new Item(Material.BARRIER).setTitle(ChatColor.RED + "Banned")
					.setLore(ChatColor.GRAY + "Your server has been banned.",
							ChatColor.GRAY + "Reason: "+server.getBanReason()).build(), new PacketMenuSlotHandler() {
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					close();
					player.sendMessage(ChatColor.RED+"Your server has been banned.");
					player.sendMessage(ChatColor.GRAY+"Reason: "+server.getBanReason());
					player.sendMessage(ChatColor.GRAY+"If you think this error is wrong, please contact the staff.");
				}
			});
			return;
		}
		
		if(server.isThirdParty()){
			addItem(4, new Item(Material.BEACON).setTitle(ChatColor.LIGHT_PURPLE+"Third-Party")
					.setLore(ChatColor.GRAY+"Your server is third party",
							ChatColor.GRAY+"Use the code: "+ChatColor.RED+server.getThirdPartyCode(),
							ChatColor.GRAY+"to link your server to PlayerIslands").build());
			
			addItem(8, new Item(Material.BARRIER).setTitle(ChatColor.RED+"Revert to Regular Island").setLore(ChatColor.GRAY+"Convert your server", ChatColor.GRAY+"back to a regular island").build(), new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					clearInventory();
					updateItemsForViewers();
					DropletAPI.setMetadata(server.getName(), "thirdparty", "", code -> {
						player.sendMessage(ChatColor.GREEN+"Your server is no longer third party.");
						ServerMenu sm = new ServerMenu(server, player);
						
						sm.open(player);
					});
					DropletAPI.setMetadata(server.getName(), "code", "");
				}
				
			});
			
			return;
		}
		
		if(server.getStatus() == ServerStatus.ONLINE)
		{
			addItem(1, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "menu_items.stop_server")).build(), new PacketMenuSlotHandler()
			{
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					DropletAPI.stopServer(server.getName(), false, code -> {
						if(code == ResponseCodes.SERVER_STOPPED)
						{
							Language.sendMessage(player, "menu.server_menu.server_stopped");
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
						}
						else if(code == ResponseCodes.SERVER_NOT_RUNNING)
						{
							Language.sendMessage(player, "menu.server_menu.server_not_running");
						}
						else
						{
							Language.sendMessage(player, "menu.server_menu.could_not_stop_server");
						}
					});
					menu.close();
				}
			});
		}
		else if(server.getStatus() == ServerStatus.OFFLINE)
		{
			addItem(1, new Item(Material.WOOL).setData(5).setTitle(Language.getText(player, "menu_items.start_server")).build(), new PacketMenuSlotHandler()
			{
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					DropletAPI.startServer(server.getName(), false, code -> {
						if(code == ResponseCodes.SERVER_STARTING)
						{
							Language.sendMessage(player, "menu.server_menu.server_starting");
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
						}
						else if(code == ResponseCodes.SERVER_ALREADY_RUNNING)
						{
							Language.sendMessage(player, "menu.server_menu.already_online");
						}else if(code == ResponseCodes.MEMORY_LIMIT_REACHED){
							Language.sendMessage(player, "response_codes.memory_limit_reached");
						}
						else 
						{
							Language.sendMessage(player, "menu.server_menu.failed");
						}
					});
					menu.close();
				}
			});
		}else if(server.getStatus() == ServerStatus.STARTING){
			addItem(1, new Item(Material.WOOL).setData(4).setTitle(Language.getText(player, "menu_items.server_starting.title")).setLore(Language.getText(player, "menu_items.server_starting.lore")).build());
		/*
			addItem(2, new Item(Material.FLINT_AND_STEEL).setTitle(Language.getText(player, "menu_items.kill_server.title")).setLore(Language.getText(player, "menu_items.kill_server.lore")).build(), new  PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					DropletAPI.stopServer(server.getName(), true);
					menu.close();
					player.playSound(player.getLocation(), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1F, 1F);
				}
			});*/
			
		}else if(server.getStatus() == ServerStatus.STOPPING){
			addItem(1, new Item(Material.WOOL).setData(1).setTitle(Language.getText(player, "menu_items.server_stopping.title")).setLore(Language.getText(player, "menu_items.server_stopping.lore")).build());
		}
		
		if(server.getStatus() == ServerStatus.OFFLINE){
			addItem(3, new Item(Material.PAPER).setTitle(Language.getText(player, "menu_items.rename_realm.title")).setLore(Language.getText(player, "menu_items.rename_realm.lore")).build(), 
					new PacketMenuSlotHandler() {
						
						@Override
						public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
							AnvilPacketMenu anvil = new AnvilPacketMenu();
							anvil.setDefaultText(server.getName());
							anvil.setResult(new ItemStack(Material.PAPER, 1));
							anvil.setHandler(new AnvilPacketMenuHandler() {
								
								@Override
								public void onResult(String text, Player pl) {
									text = ChatColor.stripColor(text);
									if(ServerDroplet.getBlockedServerNames().contains(text.toLowerCase())){
										Language.sendMessage(player, "response_codes.server_name_taken");
										return;
									}
									DropletAPI.renameRealm(server.getName(), text, (code) -> {
										if(code == ResponseCodes.SERVER_RENAMED){
											Language.sendMessage(player, "response_codes.server_renamed");
										}else if(code == ResponseCodes.SERVER_NAME_TAKEN)
										{
											Language.sendMessage(player, "response_codes.server_name_taken");
										}else if(code == ResponseCodes.SERVER_NAME_INVALID)
										{
											Language.sendMessage(player, "response_codes.server_name_invalid");
										}else if(code == ResponseCodes.SERVER_NAME_LENGTH_INVALID)
										{
											Language.sendMessage(player, "response_codes.server_name_length_invalid");
										}else{
											Language.sendMessage(player, "response_codes.server_unknown_error");
										}
									});
								}
							});
							anvil.open(player);
						}
					});
		}
		
		
		Item deleteItem = new Item(Material.BARRIER).setTitle(Language.getText(player, "menu_items.delete_server.title"));
		
		if(server.isPremium()){
			deleteItem.addLore(Language.getText(player, "menu_items.delete_server.lore.1"), Language.getText(player, "menu_items.delete_server.lore.2"), Language.getText(player, "menu_items.delete_server.lore.3"));
		}
		
		Item changeWorldType = new Item(Material.GRASS).setTitle(Language.getText(player, "menu_items.change_world.title"));
		
		String status = "";
		
		if(server.areCommandBlocksEnabled()){
			status = Language.getText(player, "menu_items.toggle_command.lore.enabled");
		}else{
			status = Language.getText(player, "menu_items.toggle_command.lore.disabled");
		}
		
		addItem(6, new Item(Material.COMMAND)
				.setTitle(Language.getText(player, "menu_items.toggle_command.title"))
				.setLore(Language.getText(player, "menu_items.toggle_command.lore.1", status)).build(),
				new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						if(DropletAPI.getServerInfo(server.getName()).areCommandBlocksEnabled()){
							DropletAPI.toggleCommandBlocks(server, false, code -> setupMenu(DropletAPI.getServerInfo(server.getName()), player));
						}else{
							DropletAPI.toggleCommandBlocks(server, true, code -> setupMenu(DropletAPI.getServerInfo(server.getName()), player));
						}
					}
					
				});
		
		
		String fstatus = "";
		
		if(server.areFlightEnabled()){
			fstatus = Language.getText(player, "menu_items.toggle_flight.lore.disabled");
		}else{
			fstatus = Language.getText(player, "menu_items.toggle_flight.lore.enabled");
		}
		
		addItem(5, new Item(Material.FEATHER)
				.setTitle(Language.getText(player, "menu_items.toggle_flight.title"))
				.setLore(Language.getText(player, "menu_items.toggle_flight.lore.1", fstatus)).build(),
				new PacketMenuSlotHandler() {
					
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						if(DropletAPI.getServerInfo(server.getName()).areFlightEnabled()){
							DropletAPI.toggleAllowFlight(server, false, code -> setupMenu(DropletAPI.getServerInfo(server.getName()), player));
						}else{
							DropletAPI.toggleAllowFlight(server, true, code -> setupMenu(DropletAPI.getServerInfo(server.getName()), player));
						}
					}
					
				});

		
		if(server.getMetadata().getOrDefault("redo", "false").equals("true")) {
			addItem(8, changeWorldType.build(), new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					new SelectServerTypeMenu(player, true, server).open(player);
				}
			});
		}else {
			if(server.getStatus() == ServerStatus.OFFLINE) {
				addItem(8, deleteItem.build(), new PacketMenuSlotHandler() {
					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						menu.close();
						new DeleteServerMenu(server, 3, player).open(player);
					}
				});
			}
		}
		
		
		
		if(server.getStatus() == ServerStatus.ONLINE){
			addItem(8, new Item(Material.IRON_DOOR).setTitle(Language.getText(player, "menu_items.goto_server")).build(), new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					DropletAPI.connectToServer(player, server);
				}
			});
		}else if(server.getStatus() == ServerStatus.OFFLINE) {
			if(DropletAPI.getRank(player).hasPermission("playerrealms.backup")) {
				addItem(7, new Item(Material.CHEST)
						.setTitle(Language.getText(player, "backup.title"))
						.setLore(Language.getText(player, "backup.lore"))
						.build(), new PacketMenuSlotHandler() {
							
							@Override
							public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
								BackupMenu bm = new BackupMenu(player, server);
								bm.open(player);
							}
						});
			}
		}
	}

}
