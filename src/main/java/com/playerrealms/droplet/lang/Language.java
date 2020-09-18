package com.playerrealms.droplet.lang;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.playerrealms.droplet.ServerDroplet;

import net.md_5.bungee.api.ChatColor;

public class Language {

	private static final Map<UUID, String> playerLanguages = new HashMap<>();
	
	private static final Map<String, Language> registeredLanguage = new HashMap<>();
	
	private static final String DEFAULT_LANGUAGE = "ja_jp";
	
	private Map<String, String> text;
	
	private String languageName;
	
	private Language(FileConfiguration config, String languageName) {
		
		this.languageName = languageName;
		
		text = new HashMap<>();
		
		for(String key : config.getKeys(true)){
			Object value = config.get(key);
			
			if(value instanceof String){
				text.put(key, ChatColor.translateAlternateColorCodes('&', value.toString()));
			}
		}
		
		this.languageName = languageName;
		
	}
	
	public static Language getLanguage(String name){
		return registeredLanguage.get(name);
	}
	
	public static void registerLanguage(Reader reader, String name){
		Language language = new Language(YamlConfiguration.loadConfiguration(reader), name);
	
		if(registeredLanguage.containsKey(language.getLanguageName())){
			registeredLanguage.get(language.getLanguageName()).text.putAll(language.text);
		}else{
			registeredLanguage.put(language.getLanguageName(), language);
		}
		
	}
	
	protected static void setLanguage(Player player, String lang){
		playerLanguages.put(player.getUniqueId(), lang);
		ServerDroplet.getInstance().getLogger().info(player.getName()+" is using language "+lang);
	}
	
	public static void sendMessage(Player player, String key, Object... values){
		getLanguage(player.getUniqueId()).send(player, key, values);
	}
	
	public static String getText(Player player, String key, Object... values){
		return getLanguage(player.getUniqueId()).getText(key, values);
	}
	
	public static boolean hasKey(Player player, String key){
		return getLanguage(player.getUniqueId()).text.containsKey(key);
	}
	
	public static Language getLanguage(UUID uuid){
		String lang = playerLanguages.get(uuid);
		
		if(!registeredLanguage.containsKey(lang)){
			return getDefault();
		}
		
		return registeredLanguage.get(lang);
	}
	
	public static String getLocale(Player player){
		Language lang = getLanguage(player.getUniqueId());
		
		return lang.getLanguageName();
	}
	
	private static Language getDefault(){
		return registeredLanguage.get(DEFAULT_LANGUAGE);
	}
	
	public String getLanguageName() {
		return languageName;
	}
	
	public Collection<String> getKeys(){
		return text.keySet();
	}
	
	public void send(Player player, String key, Object... values){
		player.sendMessage(getText(key, values));
	}
	
	public boolean hasKey(String key){
		return text.containsKey(key);
	}
	
	public String getText(String key, Object... values){
		if(!text.containsKey(key)){
			if(this != getDefault()){
				return getDefault().getText(key, values);
			}
			return key;
		}
		
		String line = text.get(key);
		
		for(int i = 0; i < values.length;i++){
			line = line.replaceAll("%"+i, values[i].toString());
		}
		
		return line;
	}
	
}
