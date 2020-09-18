package com.playerrealms.droplet.crates;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;

public class Crate {

	private long id;
	
	private String[] data;
	
	protected Crate(long id, String type) {
		this.id = id;
		data = type.split(":");
	}
	
	public long getId() {
		return id;
	}
	
	public String getType() {
		return getData(0, "global");
	}
	
	public String getSubtype() {
		return getData(1, "none");
	}
	
	public String getData(int index, String def) {
		if(data.length <= index) {
			return def;
		}
		return data[index];
	}
	
	public boolean isGlobalCrate() {
		return getType().equals("global");
	}
	
	public boolean isPlayerServerCrate() {
		return getType().equals("player");
	}
	
	public boolean openCrate() {
		try {
			CrateAPI.removeCrate(this);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean isLinkedServerExist() {
		if(!isPlayerServerCrate()) {
			return false;
		}
		
		return DropletAPI.getServerByUUID(UUID.fromString(getSubtype())) != null;
	}
	
	public String getName(Player player) {
		
		String sub = GlobalCrateTypes.UNKNOWN.getName(player);
		
		if(isGlobalCrate()) {
			sub = GlobalCrateTypes.getByCrate(this).getName(player);
		}
		
		if(isLinkedServerExist()) {
			sub = DropletAPI.getServerByUUID(UUID.fromString(getSubtype())).getName();
		}
		
		return Language.getText(player, "crates.name."+getType(), sub);
	}
	
	public Material getItemType() {
		if(isGlobalCrate()) {
			return GlobalCrateTypes.getByCrate(this).getDisplayItem();
		}else if(isPlayerServerCrate()) {
			return Material.CHEST;
		}
		
		return GlobalCrateTypes.UNKNOWN.getDisplayItem();
	}
	
}
