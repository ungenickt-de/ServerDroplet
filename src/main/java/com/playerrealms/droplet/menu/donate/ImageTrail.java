package com.playerrealms.droplet.menu.donate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import com.playerrealms.droplet.ServerDroplet;

public class ImageTrail extends Trail {

	private static Map<String, BufferedImage> cache = new HashMap<>();

	private static BufferedImage getImage(String name) {
		if(cache.containsKey(name)) {
			return cache.get(name);
		}
		
		try {
			BufferedImage image = ImageIO.read(ServerDroplet.getInstance().getResource("images/"+name+".png"));
			
			cache.put(name, image);
			
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private String image;
	
	private int delay;
	
	public ImageTrail() {
		
	}
	
	
	public ImageTrail(String img) {
		image = img;
	}
	
	@Override
	public String getPermission() {
		return "playerrealms.trails.image";
	}
	

	@Override
	public String getRequirement() {
		return ChatColor.DARK_RED+"PRO";
		
	}
	
	
	@Override
	public void spawn(Location loc) {
		
		if(delay < 10) {
			delay++;
			return;
		}
		if(delay > 13) {

			delay = 0;
		}else {
			delay++;
		}
		
		BufferedImage image = getImage(this.image);

		loc = loc.clone().subtract(1.0F * image.getWidth() * 0.15F / 2, 0, 1.0F * image.getHeight() * 0.15F / 2);
		
		if(image != null) {
			
			for(int i = 0; i < image.getWidth();i ++) {
				for(int j = 0; j < image.getHeight();j ++) {
					
					int rgba = image.getRGB(i, j);
					
					java.awt.Color color = new java.awt.Color(rgba, true);
					
					float r = ((float) color.getRed()) / 255F;
					float g = ((float) color.getGreen()) / 255F;
					float b = ((float) color.getBlue()) / 255F;
					float a = ((float) color.getAlpha()) / 255F;
					
					if(a < 1) {
						continue;
					}
					
					float oX = ((float) i) * 0.15F;
					float oY = ((float) j) * 0.15F;
					
					loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + oX, loc.getY() + 0.1F, loc.getZ() + oY, 0, r, g, b);
					
				}
			}
			
		}
		
	}

	@Override
	public String getName() {
		return "ImageTrail";
	}

	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.image";
	}

	@Override
	public String serialize() {
		return image;
	}

	@Override
	public void deserialize(String data) {
		image = data;
	}

}
