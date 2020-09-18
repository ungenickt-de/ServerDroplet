package com.playerrealms.droplet.menu.hub;

import java.util.UUID;
import java.util.function.Consumer;

import com.playerrealms.droplet.rank.Rank;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import com.nirvana.menu.AnvilPacketMenu;
import com.nirvana.menu.AnvilPacketMenuHandler;
import com.nirvana.menu.ChestPacketMenu;
import com.nirvana.menu.Interaction;
import com.nirvana.menu.Item;
import com.nirvana.menu.PacketMenu;
import com.nirvana.menu.PacketMenuSlotHandler;
import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.WorldGeneratorType;
import com.playerrealms.droplet.Callback;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.conversation.EnterURLPrompt;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class SelectServerTypeMenu extends ChestPacketMenu implements PacketMenuSlotHandler {

	private boolean justChangeType;
	private ServerInformation target;
	
	public SelectServerTypeMenu(Player player) {
		this(player, false, null);
	}
	
	public SelectServerTypeMenu(Player player, boolean justChangeType, ServerInformation target) {
		super(27, "Select Type");
		
		this.justChangeType = justChangeType;
		this.target = target;
		
		addItem(2, 2, new Item(Material.SLIME_BLOCK).setTitle(Language.getText(player, "world_types.flat")).build());
		addItem(4, 2, new Item(Material.SAPLING).setTitle(Language.getText(player, "world_types.normal")).build());
		addItem(6, 2, new Item(Material.BEDROCK).setTitle(Language.getText(player, "world_types.void")).build());
		if(DropletAPI.getRank(player).hasPermission("playerrealms.worldtypes.upload")){ //target.isBeta()
			addItem(8, 2, new Item(Material.WATER_BUCKET).setTitle(Language.getText(player, "world_types.upload")).setLore(Language.getText(player, "world_types.uploading_warning.1"), Language.getText(player, "world_types.uploading_warning.2"), Language.getText(player, "world_types.uploading_warning.3")).build());
		}
		addGeneralHandler(this);
		
	}

	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		
		Material item = interactionInfo.getItem().getType();
		
		WorldGeneratorType worldType = null;
		
		if(item == Material.SLIME_BLOCK){
			worldType = WorldGeneratorType.FLAT;
		}else if(item == Material.SAPLING){
			worldType = WorldGeneratorType.NORMAL;
		}else if(item == Material.BEDROCK){
			worldType = WorldGeneratorType.VOID;
		}else if(item == Material.WATER_BUCKET){
			worldType = WorldGeneratorType.UPLOAD;
		}
		
		if(justChangeType) {
			
			if(worldType == WorldGeneratorType.UPLOAD) {
				Consumer<String> urlConsumer = new Consumer<String>() {
					
					@Override
					public void accept(String url) {
						DropletAPI.setMetadata(target.getName(), "wt", WorldGeneratorType.UPLOAD.getId());
						DropletAPI.setMetadata(target.getName(), "url", url);
						DropletAPI.setMetadata(target.getName(), "redo", "", new Callback() {
							
							@Override
							public void onReplyReceived(ResponseCodes code) {
								ServerMenu m = new ServerMenu(target, player);
								m.open(player);
							}
						});

					}
					
				};
				
				player.sendMessage("");
				Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(new EnterURLPrompt(urlConsumer, ChatColor.YELLOW+"Enter URL (upload to http://file.io ): ")).withEscapeSequence("cancel").buildConversation(player);
				player.beginConversation(conversation);
				player.sendMessage("");
			}else {
				DropletAPI.setMetadata(target.getName(), "wt", worldType.getId());
				DropletAPI.setMetadata(target.getName(), "redo", "", new Callback() {
					
					@Override
					public void onReplyReceived(ResponseCodes code) {
						ServerMenu m = new ServerMenu(target, player);
						m.open(player);
					}
				});
				
			}
			
		
			return;
		}
		
		if(worldType == WorldGeneratorType.UPLOAD){
			
			Consumer<String> urlConsumer = new Consumer<String>() {
				
				@Override
				public void accept(String url) {
					nameServer(player, WorldGeneratorType.UPLOAD, url);
				}
				
			};
			
			player.sendMessage("");
			Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(new EnterURLPrompt(urlConsumer, ChatColor.YELLOW+"Enter URL (upload to http://file.io ): ")).withEscapeSequence("cancel").buildConversation(player);
			player.beginConversation(conversation);
			player.sendMessage("");
			menu.close();
		}else if(worldType != null){
			nameServer(player, worldType, null);
		}
		
		
		
	}
	
	private void nameServer(Player player, WorldGeneratorType worldType, String url){
		AnvilPacketMenu anvil = new AnvilPacketMenu();
		anvil.setDefaultText(Language.getText(player, "menu_items.manage.anvil_name"));
		anvil.setResult(new Item(Material.ANVIL).build());
		anvil.setHandler(new AnvilPacketMenuHandler() 
		{
			@Override
			public void onResult(String text, Player player) {
				DropletAPI.createServer(text, code -> {
					if(code == ResponseCodes.SERVER_CREATED)
					{
						DropletAPI.setServerOwner(text, player.getUniqueId());
						DropletAPI.setMetadata(text, "wt", worldType.getId(), (code3) -> {
							if(worldType == WorldGeneratorType.UPLOAD){
								DropletAPI.setMetadata(text, "url", url);
							}
							DropletAPI.startServer(text, false, code2 -> {
								if(code2 == ResponseCodes.SERVER_STARTING){
									DropletAPI.commandServer(text, "op "+player.getName());
								}else if(code2 == ResponseCodes.MEMORY_LIMIT_REACHED){
									Language.sendMessage(player, "response_codes.memory_limit_reached");
								}
							});
							TextComponent component = new TextComponent();
							component.setText(Language.getText(player, "menu.server_menu.created"));
							component.setColor(ChatColor.GREEN);
							component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + text));
							player.spigot().sendMessage(component);
							DropletAPI.setMetadata(text, "lang", Language.getLocale(player));
							DropletAPI.saveMetadata(text);
							
						});
						
						
					}else if(code == ResponseCodes.SERVER_NAME_TAKEN)
					{
						Language.sendMessage(player, "response_codes.server_name_taken");
					}else if(code == ResponseCodes.SERVER_NAME_INVALID)
					{
						Language.sendMessage(player, "response_codes.server_name_invalid");
					}else if(code == ResponseCodes.SERVER_NAME_LENGTH_INVALID)
					{
						Language.sendMessage(player, "response_codes.server_name_length_invalid");
					}
				});
			}
		});
		anvil.open(player);
	}
	

}
