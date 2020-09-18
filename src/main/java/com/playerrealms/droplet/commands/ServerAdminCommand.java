package com.playerrealms.droplet.commands;

import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.DropletAPI.ConsoleContract;
import com.playerrealms.droplet.lang.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class ServerAdminCommand implements CommandExecutor {
	private Map<String, ConsoleContract> contracts;
	
	static final String[] FUN_MESSAGES = new String[] {
		"Placing trees",
		"Upgrading dungeons",
		"Forging weapons",
		"Decorating houses",
		"Watering flowers",
		"Expanding ocean",
		"Chatting with villagers",
		"Milking cows",
		"Placing skeletons on spiders",
		"Test-riding horses",
		"Armoring up skeletons",
		"Shining diamonds",
		"Simulating rain",
		"Simulating snow",
		"Heating up the sun",
		"Re-lighting torches"
	};
	
	public ServerAdminCommand() {
		contracts = new HashMap<>();
	}
	
	public static String getRandomQuote(){
		Random r = new Random();
		
		return FUN_MESSAGES[r.nextInt(FUN_MESSAGES.length)];
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("sadmin")){
			
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED+"Console can't.");
				return true;
			}
			
			if(!DropletAPI.getRank(((Player)sender)).hasPermission("playerrealms.manage")) {
				Bukkit.dispatchCommand(sender, "realm");
				return true;
			}
			
			if(args.length == 0){
				args = new String[] {"help"};
			}
			
			Player player = (Player) sender;
			
			if(args[0].equalsIgnoreCase("create")){
				
				if(args.length < 3){
					Language.sendMessage(player, "sadmin.create");
					return true;
				}
				String name = args[1];
				String type = args[2];
				
				DropletAPI.createServer(name, type, code -> {
					if(code == ResponseCodes.SERVER_NAME_INVALID){
						Language.sendMessage(player, "response_codes.server_name_invalid");
					}else if(code == ResponseCodes.SERVER_NAME_LENGTH_INVALID){
						Language.sendMessage(player, "response_codes.server_name_length_invalid");
					}else if(code == ResponseCodes.SERVER_NAME_TAKEN){
						Language.sendMessage(player, "response_codes.server_name_taken");
					}else if(code == ResponseCodes.SERVER_CREATED){
						Language.sendMessage(player, "response_codes.server_created");
					}else if(code == ResponseCodes.UNKNOWN_SERVER_TYPE){
						Language.sendMessage(player, "response_codes.server_type_unknown", type);
					}
				});
				
			}else if(args[0].equalsIgnoreCase("delete")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.delete");
					return true;
				}
				String name = args[1];
				
				DropletAPI.deleteServer(name, code -> {
					if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", name);
					}else if(code == ResponseCodes.SERVER_REMOVED){
						Language.sendMessage(player, "response_codes.server_removed");
					}
				});
				
			}else if(args[0].equalsIgnoreCase("stop")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.stop");
					return true;
				}
				String name = args[1];
				boolean force = false;
				
				if(args.length > 2){
					force = args[2].equalsIgnoreCase("force");
				}
				
				DropletAPI.stopServer(name, force, code -> {
					if(code == ResponseCodes.SERVER_NOT_RUNNING){
						Language.sendMessage(player, "response_codes.server_not_running");
					}else if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", name);
					}else if(code == ResponseCodes.SERVER_FORCE_STOPPED){
						Language.sendMessage(player, "response_codes.server_force_stopped");
					}else if(code == ResponseCodes.SERVER_STOPPED){
						Language.sendMessage(player, "response_codes.server_stopped");
					}
				});
				
			}else if(args[0].equalsIgnoreCase("start")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.start");
					return true;
				}
				
				String name = args[1];
				
				DropletAPI.startServer(name, true, code -> {
					if(code == ResponseCodes.SERVER_ALREADY_RUNNING){
						Language.sendMessage(player, "response_codes.server_already_running");
					}else if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", name);
					}else if(code == ResponseCodes.MEMORY_LIMIT_REACHED){
						sender.sendMessage(ChatColor.RED+"Memory limit reached");
					}else if(code == ResponseCodes.SERVER_STARTING){
						Language.sendMessage(player, "response_codes.server_starting");
						DropletAPI.listenToConsole(name, (str, contract) -> {
							if(str.startsWith("Preparing")){
								if(str.contains("spawn area")){
									sender.sendMessage(ChatColor.GRAY+getRandomQuote());
								}else{
									sender.sendMessage(ChatColor.BLUE+str);
								}
							}
							if(str.contains("Done")){
								contract.cancelContract();
								sender.sendMessage(ChatColor.GREEN+str.substring(0, str.indexOf('!')));
							}
						});
					}
				});
				
			}else if(args[0].equalsIgnoreCase("command")){
				if(args.length < 3){
					Language.sendMessage(player, "sadmin.command");
					return true;
				}
				
				String name = args[1];
				String cmd = "";
				for(int i = 2; i < args.length;i++){
					cmd += args[i] + " ";
				}
				
				DropletAPI.commandServer(name, cmd, code -> {
					if(code == ResponseCodes.SERVER_COMMAND_EXECUTED){
						Language.sendMessage(player, "response_codes.server_command_executed");
					}else if(code == ResponseCodes.SERVER_NOT_RUNNING){
						Language.sendMessage(player, "response_codes.server_not_running");
					}else if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", name);
					}
				});
			}else if(args[0].equalsIgnoreCase("shutdown")){
				DropletAPI.shutdown();
			}else if(args[0].equalsIgnoreCase("listen")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.listen.fail");
					return true;
				}
				
				String name = args[1];
				
				
				
				ConsoleContract cc = DropletAPI.listenToConsole(name, (str, contract) -> {
					sender.sendMessage(str);
				});
				
				ConsoleContract cc2 = contracts.put(sender.getName(), cc);
				
				Language.sendMessage(player, "sadmin.listen.success");
				
				if(cc2 != null) {
					cc2.cancelContract();
				}
			}else if(args[0].equalsIgnoreCase("unlisten")){

				ConsoleContract cc2 = contracts.remove(sender.getName());
				
				if(cc2 != null) {
					cc2.cancelContract();
					Language.sendMessage(player, "sadmin.unlisten.success");
				}else{
					Language.sendMessage(player, "sadmin.unlisten.fail");
				}
			}else if(args[0].equalsIgnoreCase("dataset")){
				
				if(args.length < 4){
					Language.sendMessage(player, "sadmin.dataset");
					return true;
				}
				
				String serverName = args[1];
				String key = args[2];
				String value = args[3];
				
				if(value.equals("{empty}")){
					value = "";
				}
				
				DropletAPI.setMetadata(serverName, key, value, code -> {
					if(code == ResponseCodes.METADATA_SET){
						Language.sendMessage(player, "response_codes.metadata_set");
					}else if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", serverName);
					}
				});
			}else if(args[0].equalsIgnoreCase("data")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.data.fail");
					return true;
				}
				
				String serverName = args[1];
				
				ServerInformation info = DropletAPI.getServerInfo(serverName);
				
				if(info != null){
					
					Map<String, String> data = info.getMetadata();
					
					Language.sendMessage(player, "sadmin.data.success");
					
					for(String key : data.keySet()){
						Language.sendMessage(player, "sadmin.data.value", key, data.get(key));
					}
					
				}else{
					Language.sendMessage(player, "response_codes.server_unknown", serverName);
				}
			}else if(args[0].equalsIgnoreCase("datasave")){
				if(args.length < 2){
					Language.sendMessage(player, "sadmin.datasave");
					return true;
				}
				
				String serverName = args[1];
				
				DropletAPI.saveMetadata(serverName, code -> {
					if(code == ResponseCodes.UNKNOWN_SERVER){
						Language.sendMessage(player, "response_codes.server_unknown", serverName);
					}else if(code == ResponseCodes.UNKNOWN_ERROR){
						Language.sendMessage(player, "response_codes.unknown_error");
					}else if(code == ResponseCodes.METADATA_SAVED){
						Language.sendMessage(player, "response_codes.metadata_save");
					}
				});
			}else if(args[0].equalsIgnoreCase("economy")){
				if(args.length < 4){
					Language.sendMessage(player, "sadmin.economy.fail");
					return true;
				}
				String op = args[1];
				String playerName = args[2];
				int amount = 0;
				Player pl = Bukkit.getPlayer(playerName);
				try {
					amount = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					Language.sendMessage(player, "sadmin.economy.fail");
					return true;
				}
				
				if(pl == null){
					Language.sendMessage(player, "generic.unknown_player", playerName);
					return true;
				}
				
				if(op.equalsIgnoreCase("give")){
					DropletAPI.setCoins(pl, DropletAPI.getCoins(pl)+amount);
					Language.sendMessage(player, "sadmin.economy.success");
				}else if(op.equalsIgnoreCase("take")){
					DropletAPI.setCoins(pl, DropletAPI.getCoins(pl)-amount);
					Language.sendMessage(player, "sadmin.economy.success");
				}else{
					Language.sendMessage(player, "sadmin.economy.fail");
				}
			}else if(args[0].equalsIgnoreCase("shutdownempty")){
				
				int amount = 0;
				
				if(args.length > 2){
					try {
						amount = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						
					}
				}
				
				final int wontChange = amount;
				
				DropletAPI.getPlayerServers().stream().filter(server -> server.getStatus() == ServerStatus.ONLINE).filter(server -> server.getPlayersOnline() <= wontChange).forEach(server -> {
					DropletAPI.stopServer(server.getName(), false);
					sender.sendMessage(ChatColor.GREEN+"Shutting down "+server.getName());
				});
			}else if(args[0].equalsIgnoreCase("startrandom")){
				if(args.length < 2){
					sender.sendMessage(ChatColor.RED+"/sadmin startrandom [amount]");
					return true;
				}
				
				int amount = 1;
				
				try{
					amount = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){
					sender.sendMessage(ChatColor.RED+"Invalid number "+args[1]);
					return true;
				}
				
				List<ServerInformation> servers = DropletAPI.getPlayerServers().stream().filter(server -> server.getStatus() ==  ServerStatus.OFFLINE).collect(Collectors.toList());
				
				int started = 0;
				
				for(int i = 0; i < amount;i++){
					if(servers.size() == 0){
						break;
					}
					
					String name = servers.remove(0).getName();
					
					DropletAPI.startServer(name, true);
					started++;
					sender.sendMessage(ChatColor.GREEN+"Started "+name);
				}
				
				sender.sendMessage(ChatColor.GREEN+"Started "+started+" servers");
			}else if(args[0].equalsIgnoreCase("tax")){
				
				if(args.length < 2){
					sender.sendMessage(ChatColor.RED+"/sa tax [set / total] <rate>");
				}else{
					String cmd = args[1];
					
					if(cmd.equalsIgnoreCase("total")){
						sender.sendMessage(ChatColor.GREEN+"Total taxed money: "+DropletAPI.getTotalTaxed()+" coins");
					}else if(cmd.equalsIgnoreCase("set")){
						int amount = Integer.parseInt(args[2]);
						
						DropletAPI.setTax(amount);
					
						sender.sendMessage(ChatColor.GREEN+"Tax set to "+amount+"%");
					}
				}
				
			}else if(args[0].equalsIgnoreCase("help")){
				Language.sendMessage(player, "sadmin.help.1");
				Language.sendMessage(player, "sadmin.help.2");
				Language.sendMessage(player, "sadmin.help.3");
				Language.sendMessage(player, "sadmin.help.4");
				Language.sendMessage(player, "sadmin.help.5");
				Language.sendMessage(player, "sadmin.help.6");
				Language.sendMessage(player, "sadmin.help.7");
				Language.sendMessage(player, "sadmin.help.8");
				Language.sendMessage(player, "sadmin.help.9");
				Language.sendMessage(player, "sadmin.help.10");
				Language.sendMessage(player, "sadmin.help.11");
				Language.sendMessage(player, "sadmin.help.12");
				Language.sendMessage(player, "sadmin.help.13");
				Language.sendMessage(player, "sadmin.help.14");
			}else{
				Language.sendMessage(player, "sadmin.unknown", args[0]);
			}
			
		}
		
		return true;
	}

}
