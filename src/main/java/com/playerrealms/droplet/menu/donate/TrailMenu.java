package com.playerrealms.droplet.menu.donate;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;

public class TrailMenu extends PagedMenu {

	public TrailMenu() {
		super(createEntries(), "Select Trail");
	}
	
	@SuppressWarnings("deprecation")
	private static List<PageMenuEntry> createEntries(){
		
		List<PageMenuEntry> entries = new ArrayList<>();
		
		entries.add(new TrailEffectEntry("None", Material.BARRIER, new NoTrail()));
		entries.add(new TrailEffectEntry("Enchant", Material.BOOK, new EnchantTrail()));
		entries.add(new TrailEffectEntry("Water", Material.WATER_BUCKET, new WaterTrail()));
		entries.add(new TrailEffectEntry("Heart", Material.RED_ROSE, new HeartTrail()));
		entries.add(new TrailEffectEntry("Fire", Material.LAVA_BUCKET, new FlameTrail()));
		entries.add(new TrailEffectEntry("Happy", Material.MAP, new ImageTrail("humm")));
		entries.add(new TrailEffectEntry("Ika", Material.MAP, new ImageTrail("squid")));
		entries.add(new TrailEffectEntry("Pokeball", Material.MAP, new ImageTrail("pokeball")));
		entries.add(new TrailEffectEntry("Masterball", Material.MAP, new ImageTrail("masterball")));
		entries.add(new CustomImageTrail.SelectCustomImageEntry());
		
		for(DyeColor color : DyeColor.values()) {
			
			float r = color.getColor().getRed();
			float g = color.getColor().getGreen();
			float b = color.getColor().getBlue();
			r /= 255F;
			g /= 255F;
			b /= 255F;
			
			entries.add(new TrailEffectEntry(color.name().toLowerCase(), Material.WOOL, color.getWoolData(), new ColorTrail(r, g, b)));
			
		}
		
		/*entries.add(new TrailEffectEntry("Red", Material.WOOL, 14, new ColorTrail(0.8F, 0, 0)));
		entries.add(new TrailEffectEntry("Green", Material.WOOL, 5, new ColorTrail(0, 0.8F, 0)));
		entries.add(new TrailEffectEntry("Blue", Material.WOOL, 11, new ColorTrail(0, 0, 0.8F)));
		entries.add(new TrailEffectEntry("", Material.WOOL, 14, new ColorTrail(1, 0, 0)));
		entries.add(new TrailEffectEntry("Red", Material.WOOL, 14, new ColorTrail(1, 0, 0)));
		entries.add(new TrailEffectEntry("Red", Material.WOOL, 14, new ColorTrail(1, 0, 0)));
		*/
		
		
		return entries;
		
	}

}
