package com.playerrealms.droplet.commands;

import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.util.MojangAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ServerCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {
			@Override
			public void run() {
				if(command.getName().equals("server") && sender instanceof Player){

					if(JedisAPI.keyExists("cname_connect."+((Player) sender).getUniqueId())){
						sender.sendMessage(Language.getText((Player) sender, "jump_server_deny"));
						return;
					}

					if(args.length == 0){

						List<ServerInformation> servers = DropletAPI.getPlayerServers();

						Collections.sort(servers);

						servers.removeIf(server -> server.getStatus() != ServerStatus.ONLINE);
						servers.removeIf(server -> server.isBan());

						if(servers.size() == 0){
							Language.sendMessage((Player) sender, "server_command.no_servers");
							return;
						}

						//StringBuilder str = new StringBuilder(ChatColor.YELLOW+"Servers ("+servers.size()+"): ");
						BaseComponent[] components = new BaseComponent[servers.size()+1];
						components[0] = new TextComponent("Servers ("+servers.size()+"): ");
						components[0].setColor(ChatColor.YELLOW);
						int i = 0;
						for(ServerInformation info : servers){
							StringBuilder str = new StringBuilder();
							if(info.getStatus() == ServerStatus.ONLINE){
								str.append(ChatColor.GREEN);
							}else if(info.getStatus() == ServerStatus.STARTING){
								str.append(ChatColor.YELLOW);
							}else if(info.getStatus() == ServerStatus.STOPPING){
								str.append(ChatColor.RED);
							}else{
								str.append(ChatColor.DARK_RED);
							}
							if(info.isUltraPremium()) {
								str.append(ChatColor.GOLD);
								str.append(ChatColor.BOLD);
							}
							if (info.isThirdParty()) {
								str.append(ChatColor.RESET);
								str.append(ChatColor.YELLOW);
							}
							if (info.isWhitelistEnabled()) {
								str.append(ChatColor.RESET);
								str.append(ChatColor.WHITE);
							}
							str.append(info.getName());
							if(info.getStatus() == ServerStatus.ONLINE){
								str.append(ChatColor.BLUE+"("+ChatColor.BOLD+info.getPlayersOnline()+ChatColor.BLUE+")");
							}
							str.append(" ");
							components[++i] = new TextComponent(str.toString());
							components[i].setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server "+info.getName()));

							String motd = info.getMotd();

							motd = WordUtils.wrap(motd, 30, "\n", true);

							String lastColor = ChatColor.WHITE.toString();


							ComponentBuilder builder = new ComponentBuilder(info.getName()).color(ChatColor.GREEN).append("\n");

					/*if(motd != null) {
						for (String s : motd.split("\n"))
						{
							builder.append(lastColor + s);
							builder.append("\n");
							lastColor = getFinalColor(s);
						}
					}*/

							builder.append("\n");
							builder.append(Language.getText((Player) sender, "menu_items.page_entry.online", info.getPlayersOnline(), info.getMaxPlayers()));
							builder.append("\n");
							builder.append(Language.getText((Player) sender, "menu_items.page_entry.votes", info.getVotes()));
							builder.append("\n");
							builder.append(Language.getText((Player) sender, "menu_items.page_entry.owner", MojangAPI.getUsername(info.getOwner())));

							BaseComponent[] desc = builder.create();

							components[i].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc));
						}

						sender.spigot().sendMessage(components);

						return;
					}

					String server = args[0];

					ServerInformation info = DropletAPI.getServerInfo(server);

					if(info == null){
						Language.sendMessage((Player) sender, "response_codes.server_unknown", server);
					}else if(info.getStatus() == ServerStatus.OFFLINE) {
						Language.sendMessage((Player) sender, "server_command.offline", server);
					}else if(info.isBan()){
						Language.sendMessage((Player) sender, "response_codes.server_banned", server);
					}else{
						DropletAPI.connectToServer((Player) sender, info);
					}
				}
			}
		});
		return true;
	}
	
	private static String getFinalColor(String line)
	{
		ChatColor color = ChatColor.WHITE;
		boolean bold = false;
		boolean underlined = false;
		boolean italic = false;
		boolean strikethrough = false;

		boolean next = false;

		for (char c : line.toCharArray())
		{
			if (next)
			{
				next = false;
				ChatColor co = ChatColor.getByChar(c);
				if (co == ChatColor.BOLD)
				{
					bold = true;
				}
				else if (co == ChatColor.ITALIC)
				{
					italic = true;
				}
				else if (co == ChatColor.UNDERLINE)
				{
					underlined = true;
				}
				else if (co == ChatColor.RESET)
				{
					color = ChatColor.WHITE;
					bold = false;
					underlined = false;
					italic = false;
					strikethrough = false;
				}
				else if (co == ChatColor.STRIKETHROUGH)
				{
					strikethrough = true;
				}
				else
				{
					color = co;
					bold = false;
					underlined = false;
					italic = false;
					strikethrough = false;
				}
			}
			if (c == ChatColor.COLOR_CHAR)
			{
				next = true;
			}
		}

		if(color == null){
			color = ChatColor.WHITE;
		}
		
		String co = color.toString();
		if (bold)
		{
			co += ChatColor.BOLD;
		}
		if (italic)
		{
			co += ChatColor.ITALIC;
		}
		if (underlined)
		{
			co += ChatColor.UNDERLINE;
		}
		if (strikethrough)
		{
			co += ChatColor.STRIKETHROUGH;
		}

		return co;
	}
	
}
