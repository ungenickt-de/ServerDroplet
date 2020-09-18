package com.playerrealms.droplet.rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.playerrealms.common.ServerType;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.tablist.TabListSortRank;

import net.md_5.bungee.api.ChatColor;

public class Rank {

	private static Map<UUID, PermissionAttachment> attachements = new HashMap<>();

	private int id;
	private String name;
	private String prefix;
	private int power;
	private int child;
	private int tabPriority;
	
	private Set<RealmPermission> permissions;
	
	public Rank(int id, int power, String name, int child, String prefix, int priority, Collection<RealmPermission> perms) {
		this.id = id;
		this.power = power;
		this.name = name;
		this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		this.child = child;
		tabPriority = priority;
		
		permissions = new HashSet<>(perms);
	}

	public TabListSortRank getSortRank(){
		return TabListSortRank.values()[tabPriority];
	}
	
	public int getTabPriority() {
		return tabPriority;
	}
	
	public Rank getChild(){
		return ServerDroplet.getInstance().getRank(child);
	}
	
	public boolean hasChild(){
		return child >= 0;
	}
	
	public int getPower() {
		return power;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public boolean hasPermission(String perm){
		for(RealmPermission rp : permissions) {
			if(rp.getPermission().equals(perm)) {
				return true;
			}
		}
		return getChild() == null ? false : getChild().hasPermission(perm);
	}
	
	public String getFormat(){
		String format = "";
		if (!ChatColor.stripColor(getPrefix()).isEmpty())
		{
			format = getPrefix() + " %s";
		}
		else
		{
			format = getPrefix() + "%s";
		}
		return format;
	}
	
	public String getPlayerName(String name){
		return getFormat().replaceAll("%s", name);
	}
	
	private void givePermissions(PermissionAttachment attachement){
		
		boolean playerServer = DropletAPI.getThisServer().getServerType() == ServerType.PLAYER;
		
		for(RealmPermission permission : permissions){
			if((playerServer && permission.isGlobal()) || !playerServer){
				attachement.setPermission(permission.getPermission(), true);
			}
		}
		
		if(hasChild()){
			Rank child = getChild();
			
			child.givePermissions(attachement);
		}
		
	}
	
	public void updatePermissionAttachement(Player player){
		PermissionAttachment attachement = null;
		if(attachements.containsKey(player.getUniqueId())){
			attachement = attachements.get(player.getUniqueId());
		}else{
			attachement = player.addAttachment(ServerDroplet.getInstance());
		}
		
		clearPermissions(attachement);
		
		givePermissions(attachement);
	}
	
	private void clearPermissions(PermissionAttachment attachement){
		for(String perm : attachement.getPermissions().keySet()){
			attachement.unsetPermission(perm);
		}
	}
	
}
