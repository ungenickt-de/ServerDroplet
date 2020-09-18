package com.playerrealms.droplet.util;

import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PluginData {

	private static final List<String> exclusives = new ArrayList<>();
	private static boolean exclusivesLoaded = false;

	private static final List<String> hiddens = new ArrayList<>();
	private static boolean hiddenLoaded = false;

	private File file;
	
	private YamlConfiguration pluginYml;
	
	private boolean exclusive;
	private boolean hidden;
	
	private PluginData() {
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PluginData) {
			return ((PluginData) obj).getName().equals(getName());
		}
		return super.equals(obj);
	}
	
	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isHidden(){
		return hidden;
	}

	public File getFile() {
		return file;
	}
	
	public YamlConfiguration getPluginYml() {
		return pluginYml;
	}
	
	public String getName(){
		if(pluginYml.getString("name") == null){
			return "Unknown";
		}
		return pluginYml.getString("name");
	}
	
	public List<String> getDependencies(){
		return pluginYml.getStringList("depend");
	}

	public static void loadExclusives() throws IOException{
		if(!exclusivesLoaded){
			InputStream is;
			try{
				URLConnection conn = DropletAPI.openApiConnection("exclusive.txt");
				is = conn.getInputStream();
			}catch (Exception e){
				is = ServerDroplet.getInstance().getResource("exclusive.txt");
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String line;
			
			while((line = reader.readLine()) != null){
				exclusives.add(line);
			}
			
			exclusivesLoaded = true;
		}
	}

	public static void loadHidden() throws IOException{
		if(!hiddenLoaded){
			InputStream is;
			try{
				URLConnection conn = DropletAPI.openApiConnection("hidden.txt");
				is = conn.getInputStream();
			}catch (Exception e){
				is = ServerDroplet.getInstance().getResource("hidden.txt");
			}
			
			if(is == null){
				is = ServerDroplet.getInstance().getResource("hidden.txt");
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			String line;

			while((line = reader.readLine()) != null){
				hiddens.add(line);
			}

			hiddenLoaded = true;
		}
	}

	public static PluginData generate(File plugin) throws IOException {
		
		PluginData data = new PluginData();
		data.file = plugin;
		
		try {
			data.pluginYml = PluginUtil.loadPluginYmlFromZip(plugin);
			if(exclusives.contains(data.getName())){
				data.exclusive = true;
			}
			if(hiddens.contains(data.getName())){
				data.hidden = true;
			}
			if(ServerDroplet.getInstance() != null)
				ServerDroplet.getInstance().getLogger().info("Created plugin data for "+plugin.getName()+", name: "+data.getName()+" exclusive ? "+data.isExclusive());
		} catch (Exception e) {
			e.printStackTrace();
			if(ServerDroplet.getInstance() != null)
				ServerDroplet.getInstance().getLogger().severe("Failed to create plugin data for "+plugin.getPath());
			return null;
		}
		
		return data;
		
	}
	
}
