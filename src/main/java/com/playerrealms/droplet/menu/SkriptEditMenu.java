package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.TextFileUploadUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class SkriptEditMenu extends PagedMenu {

	public SkriptEditMenu() {
		super(getEntries(getSkriptFolder()), "Edit Skripts");
		remake(false);
	}
	
	static File getSkriptFolder(){
		return new File("plugins/Skript/scripts");
	}
	
	static List<PageMenuEntry> getEntries(File folder){
		
		List<PageMenuEntry> entries = new ArrayList<>();
		
		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				entries.addAll(getEntries(file));
			}else if(file.getName().endsWith(".sk")){
				
				String name = file.getName().substring(0, file.getName().indexOf('.'));
				
				entries.add(new SkriptEntry(name, file));
				
			}
		}
		
		return entries;
	}

	static void openMenu(Player player){
		SkriptEditMenu skEdit = new SkriptEditMenu();
		skEdit.open(player);
	}
	
	@Override
	public void remake(boolean refreshTitle) {
		super.remake(refreshTitle);

		addItem(6,1, new Item(Material.PAPER).setTitle(ChatColor.YELLOW + "config.sk").build(), new SkriptConfigHandler("config.sk"));
		addItem(7,1, new Item(Material.PAPER).setTitle(ChatColor.YELLOW + "aliases-english.sk").build(), new SkriptConfigHandler("aliases-english.sk"));
		addItem(8,1, new Item(Material.PAPER).setTitle(ChatColor.YELLOW + "features.sk").build(), new SkriptConfigHandler("features.sk"));
		/*if(Boolean.parseBoolean(DropletAPI.getThisServer().getMetadata().getOrDefault("danger", "false"))) {
			addItem(9, 1, new Item(Material.PAPER).setTitle(ChatColor.RED + "Reset Variables.csv").build(), new PacketMenuSlotHandler() {
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					menu.close();
					SkriptDeleteMenu skriptdelete = new SkriptDeleteMenu(new File("plugins/Skript/variables.csv"), 3, player);
					skriptdelete.open(player);
				}
			});
		}*/

		addItem(5, 6, new Item(Material.MAGMA_CREAM).setTitle(ChatColor.GREEN+"Upload Skript").build(), new PacketMenuSlotHandler() {
			
			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				menu.close();

				AnvilPacketMenu anvil = new AnvilPacketMenu();
				anvil.setDefaultText("Pastebin");
				anvil.setResult(new Item(Material.ANVIL).build());
				anvil.setHandler(new AnvilPacketMenuHandler() {
					@Override
					public void onResult(String text, Player player) {
						Language.sendMessage(player, "config_edit.uploading");

						
						try{
							TextFileUploadUtil.checkLegalDownloadURL(text);
							
							String[] splits = text.split("/");
							
							if(splits.length == 0){
								throw new MalformedURLException();
							}

							String correctedUrl;
							if(text.contains("pastebin.com")) {
								correctedUrl = "https://pastebin.com/raw/" + splits[splits.length - 1];
							}else if(text.contains("paste.mcua.net")){
								String temp;
								if(text.contains("paste.mcua.net/raw/")){
									temp = text.split("paste.mcua.net/raw/")[1];
								}else if(text.contains("paste.mcua.net/v/")){
									temp = text.split("paste.mcua.net/v/")[1];
								}else{
									throw new MalformedURLException();
								}
								correctedUrl = "https://paste.mcua.net/raw/" + temp;
							}else{
								throw new MalformedURLException();
							}
							
							try{
								String content = TextFileUploadUtil.downloadText(correctedUrl);
								
								File outputFile = new File(getSkriptFolder(), splits[splits.length-1] + ".sk");

								if (!outputFile.exists()) {
									outputFile.createNewFile();
								}

								FileUtils.writeStringToFile(outputFile, content, "UTF-8");
								openMenu(player);
							}catch(IOException e){
								Language.sendMessage(player, "config_edit.fail_up");
								e.printStackTrace();
							}
							
						}catch(MalformedURLException e){
							Language.sendMessage(player, "config_edit.bad_url");
						}
					}
				});
				anvil.open(player);
			}
		});
		
	}
	
	class SkriptConfigHandler implements PacketMenuSlotHandler{
		public String name;
		public SkriptConfigHandler(String name){
			this.name = name;
		}
		@Override
		public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
			menu.close();

			AnvilPacketMenu anvil = new AnvilPacketMenu();
			anvil.setDefaultText("Pastebin");
			anvil.setResult(new Item(Material.ANVIL).build());
			anvil.setHandler(new AnvilPacketMenuHandler() {
				@Override
				public void onResult(String text, Player player) {
					Language.sendMessage(player, "config_edit.uploading");
					try{
						TextFileUploadUtil.checkLegalDownloadURL(text);

						String[] splits = text.split("/");

						if(splits.length == 0){
							throw new MalformedURLException();
						}
						String correctedUrl;
						if(text.contains("pastebin.com")) {
							correctedUrl = "https://pastebin.com/raw/" + splits[splits.length - 1];
						}else if(text.contains("paste.mcua.net")){
							String temp;
							if(text.contains("paste.mcua.net/raw/")){
								temp = text.split("paste.mcua.net/raw/")[1];
							}else if(text.contains("paste.mcua.net/v/")){
								temp = text.split("paste.mcua.net/v/")[1];
							}else{
								throw new MalformedURLException();
							}
							correctedUrl = "https://paste.mcua.net/raw/" + temp;
						}else{
							throw new MalformedURLException();
						}

						try{
							String content = TextFileUploadUtil.downloadText(correctedUrl);

							YamlConfiguration testConfig = new YamlConfiguration();
							testConfig.loadFromString(content);

							File outputFile = new File("plugins/Skript/"+name);

							if(!outputFile.exists()){
								outputFile.createNewFile();
							}

							FileUtils.writeStringToFile(outputFile, content, "UTF-8");
							Language.sendMessage(player, "config_edit.retrieve_success");
						}catch(InvalidConfigurationException e){
							Language.sendMessage(player, "config_edit.bad_syntax", e.getMessage());
						}catch(IOException e){
							Language.sendMessage(player, "config_edit.fail_up");
							e.printStackTrace();
						}
					}catch(MalformedURLException e){
						Language.sendMessage(player, "config_edit.bad_url");
					}
				}
			});
			anvil.open(player);
		}
	}

	static class SkriptEntry implements PageMenuEntry {

		private String name;
		private File file;
		
		public SkriptEntry(String name, File file) {
			this.file = file;
			this.name = name;
		}
		
		@Override
		public int compareTo(PageMenuEntry o) {
			
			if(o instanceof SkriptEntry){
				return name.compareTo(((SkriptEntry) o).name);
			}
			
			return 0;
		}

		@Override
		public ItemStack getItem() {
			return new Item(Material.PAPER).setTitle(ChatColor.YELLOW+name).setLore(ChatColor.GREEN+"Left-Click Rename", ChatColor.RED+"Shift-Left-Click Delete").build();
		}

		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					if(interactionInfo.getClickType() == ClickType.LEFT){
						AnvilPacketMenu am = new AnvilPacketMenu();
						am.setResult(new ItemStack(Material.BOOK));
						am.setDefaultText(name);
						am.setHandler(new AnvilPacketMenuHandler() {
							
							@Override
							public void onResult(String text, Player pl) {
								file.renameTo(new File(file.getParent(), text+".sk"));
								
								player.sendMessage(ChatColor.GREEN+"Skript renamed!");
								openMenu(pl);
							}
						});
						am.open(player);
					}
					if(interactionInfo.getClickType() == ClickType.SHIFT_LEFT){
						file.delete();
						player.sendMessage(ChatColor.GREEN+"Skript deleted");
						openMenu(player);
					}
				}
			};
		}
	}
}
