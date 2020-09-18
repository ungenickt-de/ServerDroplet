package com.playerrealms.droplet.rank;

public class RealmPermission {

	private final String permission;
	private final boolean global;

	public RealmPermission(String permission, boolean global) {
		this.permission = permission;
		this.global = global;
	}
	
	public boolean isGlobal() {
		return global;
	}
	
	public String getPermission() {
		return permission;
	}
	
	@Override
	public int hashCode() {
		return permission.hashCode();
	}
	
}
