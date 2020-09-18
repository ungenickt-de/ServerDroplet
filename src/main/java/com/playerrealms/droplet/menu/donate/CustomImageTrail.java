package com.playerrealms.droplet.menu.donate;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import com.nirvana.menu.Interaction;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.conversation.EnterURLPrompt;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.CappedInputStream;
import com.playerrealms.droplet.util.CappedInputStream.CapExceededError;

import net.md_5.bungee.api.ChatColor;

public class CustomImageTrail extends Trail {

	private BufferedImage image;
	private int delay;
	
	private Location lastLocation;
	
	public CustomImageTrail() {
		delay = 0;
	}
	
	@Override
	public void spawn(Location loc) {

		if(lastLocation == null) {
			lastLocation = loc.clone();
		}else if(loc.distance(lastLocation) > 1F) {
			delay = 5;
			lastLocation = loc.clone();
			return;
		}
		
		if(delay < 10) {
			delay++;
			return;
		}
		if(delay > 13) {

			delay = 0;
		}else {
			delay++;
		}
		
		if(image == null) {
			return;
		}

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
					
					float MIN = 1F / 255F;
					
					r = Math.max(r, MIN);
					g = Math.max(g, MIN);
					b = Math.max(b, MIN);
					
					float oX = ((float) i) * 0.15F;
					float oY = ((float) j) * 0.15F;
					
					loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + oX, loc.getY() + 0.1F, loc.getZ() + oY, 0, r, g, b);
					
				}
			}
			
		}
		
	}

	@Override
	public String getName() {
		return "CustomImage";
	}

	@Override
	public String getDisplayName() {
		return "donate_powers.trail.names.custom";
	}

	@Override
	public String serialize() {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			ImageIO.write(image, "PNG", bos);
			
			return Base64.getEncoder().encodeToString(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return "";
	}

	@Override
	public void deserialize(String data) {
		
		if(data.length() == 0) {
			return;//It failed to upload I guess
		}
		
		byte[] buffer = Base64.getDecoder().decode(data);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		
		try {
			image = ImageIO.read(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String getPermission() {
		return "playerrealms.trails.custom";
	}

	@Override
	public String getRequirement() {
		return ChatColor.GOLD+"LEGEND";
	}
	
	public static class SelectCustomImageEntry extends TrailEffectEntry {

		public SelectCustomImageEntry() {
			super("Custom Image", Material.EMPTY_MAP, new CustomImageTrail());
		}
		
		@Override
		public PacketMenuSlotHandler getHandler() {
			return new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					menu.close();
					CustomImageTrail trail = new CustomImageTrail();
					
					if(DropletAPI.getRank(player).hasPermission(trail.getPermission())) {
						EnterURLPrompt prompt = new EnterURLPrompt(url -> {
							
							try {
								URL actualURL = new URL(url);
								
								CappedInputStream cappedInputStream = new CappedInputStream(actualURL.openStream(), 5 * 1024);//5kb
								
								BufferedImage image = ImageIO.read(cappedInputStream);
								
								try {
									cappedInputStream.close();
								}catch(Exception e) {}
								
								if(image.getWidth() > 32 || image.getHeight() > 32) {
									
									Language.sendMessage(player, "donate_powers.trail.image_size_bad");
									image.flush();
									
								}else {
									trail.image = image;
									
									DropletAPI.setTrail(player, trail);
								}
								
								
							} catch(CapExceededError e) {
								player.sendMessage(ChatColor.DARK_RED+"Image too big!");
							} catch (Exception e) {
								e.printStackTrace();
								player.sendMessage(ChatColor.DARK_RED+"Error, try again. "+e.getMessage());
							}
							
							
							
						}, ChatColor.YELLOW+"Enter URL: ");
						
						player.sendMessage("");
						Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(prompt).withEscapeSequence("cancel").buildConversation(player);
						player.beginConversation(conversation);
						player.sendMessage("");
						Language.sendMessage(player, "donate_powers.trail.selected", Language.getText(player, trail.getDisplayName()));
						menu.close();
					}else {
						Language.sendMessage(player, "donate_powers.trail.no_perm", Language.getText(player, trail.getDisplayName()));
					}
					
					
				}
			};
		}
		
	}

}
