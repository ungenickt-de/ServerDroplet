package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.PluginData;
import com.playerrealms.droplet.util.TextFileUploadUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigSelectMenu extends ChestPacketMenu {

	public ConfigSelectMenu(Player player) {
		super(54, Language.getText(player, "menu_items.config"));
		initialize(player);
	}
	
	private void initialize(Player player){
		Map<File, FileConfiguration> configs = findAllConfigs();
		
		Set<File> files = configs.keySet();
		List<File> sorted = files.stream().sorted((f1, f2) -> f1.getPath().compareTo(f2.getPath())).collect(Collectors.toList());
		
		for(File file : sorted){
			addItem(new Item(Material.PAPER).setTitle(ChatColor.GREEN+file.getPath().replaceFirst("plugins/", "")).setLore(Language.getText(player, "menu.config_edit.upload"), Language.getText(player, "menu.config_edit.download")).build(), new PacketMenuSlotHandler() {
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					if(interactionInfo.getClickType() == ClickType.SHIFT_LEFT){
						menu.close();
						try {
							String fileData = FileUtils.readFileToString(file, "UTF-8");
							
							String response = TextFileUploadUtil.uploadText(file.getName(), fileData);
							
							Language.sendMessage(player, "config_edit.upload_success", response);
						} catch (Exception e) {
							Language.sendMessage(player, "config_edit.unknown_error", e.getMessage());
						}
					}else if(interactionInfo.getClickType() == ClickType.LEFT){
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
										
										FileUtils.writeStringToFile(file, content, "UTF-8");
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
				
			});
		}
	}
	
	private Map<File, FileConfiguration> findAllConfigs(){
		Map<File, FileConfiguration> configs = new HashMap<>();
		Map<PluginData, Boolean> plugins = ServerDroplet.getInstance().getPlugins();

		File pluginsFolder = new File("plugins/");
		for(Entry<PluginData, Boolean> entry : plugins.entrySet()){
			if(entry.getValue()){
				
				File folder = new File(pluginsFolder, entry.getKey().getName());
				
				if(folder.exists()){
					configs.putAll(findConfigs(folder));
				}
			}
		}
		return configs;
	}
	
	private Map<File, FileConfiguration> findConfigs(File path){
		Map<File, FileConfiguration> configs = new HashMap<>();
		
		for(File file : path.listFiles()){
			if(file.getName().endsWith(".yml")){
				YamlConfiguration config = new YamlConfiguration();
				try {
					config.load(file);
					configs.put(file, config);
				} catch (IOException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		
		return configs;
	}

}
