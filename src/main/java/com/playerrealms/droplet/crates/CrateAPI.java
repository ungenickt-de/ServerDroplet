package com.playerrealms.droplet.crates;

import com.playerrealms.droplet.sql.DatabaseAPI;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrateAPI {

	public static List<Crate> getCrates(UUID player, int limit) throws SQLException{
		return DatabaseAPI.query("SELECT `crate_type`,`id` FROM `crates` WHERE `owner`=? LIMIT "+limit, player.toString())
				.stream()
				.map(qr -> new Crate(qr.get("id"), qr.get("crate_type")))
				.collect(Collectors.toList());
	}

	public static List<Crate> getCrates(UUID player, String type) throws SQLException{
		return DatabaseAPI.query("SELECT `crate_type`,`id` FROM `crates` WHERE `owner`=? AND `crate_type`=?", player.toString(), type)
				.stream()
				.map(qr -> new Crate(qr.get("id"), qr.get("crate_type")))
				.collect(Collectors.toList());
	}
	
	public static void giveCrate(UUID player, String type) throws SQLException {
		DatabaseAPI.execute("INSERT INTO `crates` (`owner`,`crate_type`) VALUES (?,?)", player.toString(), type);
	}
	
	public static void giveManyCrates(UUID player, String type, int amount) throws SQLException {
		for(int i = 0; i < amount;i++) {
			giveCrate(player, type);
		}
	}
	
	public static void removeCrate(Crate crate) throws SQLException {
		DatabaseAPI.execute("DELETE FROM `crates` WHERE `id`=?", crate.getId());
	}
}
