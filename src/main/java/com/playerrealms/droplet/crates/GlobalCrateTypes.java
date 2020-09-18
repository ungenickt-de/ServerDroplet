package com.playerrealms.droplet.crates;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.playerrealms.droplet.lang.Language;

public enum GlobalCrateTypes {

	COMMON("common", Material.CHEST),
	STARTER("starter", Material.JUKEBOX),
	RARE("rare", Material.STORAGE_MINECART),
	LEGENDARY("legendary", Material.ENDER_CHEST),
	GIFT("gift", Material.RED_SHULKER_BOX),
	EVENT("event", Material.HAY_BLOCK),
	UNKNOWN("unknown", Material.BARRIER),
	DONATION("donation", Material.MAGENTA_SHULKER_BOX),
	VOTE("vote", Material.POWERED_MINECART),
	ANKETO("anketo", Material.JUKEBOX),
	KINENN_2018_COMMON("ani_2018_common", Material.FIREWORK_CHARGE),
	KINENN_2018_RARE("ani_2018_rare", Material.FIREWORK),
	KINENN_2018_LEGENDARY("ani_2018_legendary", Material.TOTEM),
	CM_REWARD("cm_reward", Material.BLUE_SHULKER_BOX),
	GOLDEN_WEEK("golden_week", Material.GOLD_BLOCK),
	GEM_BOX_SMALL("gem_box_small", Material.EMERALD),
	GEM_BOX_MEDIUM("gem_box_medium", Material.EMERALD_ORE),
	GEM_BOX_LARGE("gem_box_large", Material.EMERALD_BLOCK),
	SUMMER_SMALL("summer_small", Material.MELON_SEEDS),
	SUMMER_MEDIUM("summer_medium", Material.MELON),
	SUMMER_BIG("summer_big", Material.MELON_BLOCK),
	SUMMER_SUPER("summer_super", Material.SPECKLED_MELON), 
	BIRTHDAY("birthday", Material.NETHER_STAR),
	WELCOME_BACK("welcome_back", Material.MAGMA_CREAM),
	WELCOME_BACK_SUPER("welcome_back_super", Material.TOTEM);
	private final String id;
	
	private final Material displayItem;
	
	private GlobalCrateTypes(String id, Material displayItem) {
		this.id = id;
		this.displayItem = displayItem;
	}
	
	public String getTypeString() {
		return "global:"+id;
	}
	
	public static GlobalCrateTypes getByCrate(Crate crate) {
		if(!crate.isGlobalCrate()) {
			throw new IllegalArgumentException("Only for global crates " +crate.getType()+":"+crate.getSubtype());
		}
		
		for(GlobalCrateTypes ct : values()) {
			if(ct.id.equals(crate.getSubtype())) {
				return ct;
			}
		}
		
		return GlobalCrateTypes.UNKNOWN;
	}
	
	public String getName(Player player) {
		return Language.getText(player, "crates.global_names."+id);
	}
	
	public Material getDisplayItem() {
		return displayItem;
	}

}
