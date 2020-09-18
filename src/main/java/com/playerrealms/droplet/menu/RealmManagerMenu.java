package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.conversation.ChangeMOTDPrompt;
import com.playerrealms.droplet.conversation.ChangeRPPrompt;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.menu.shop.ConfigureServerShopMenu;
import com.playerrealms.droplet.redis.JedisAPI;
import com.playerrealms.droplet.util.UploadLog;
import com.playerrealms.droplet.util.UploadUtil;
import com.playerrealms.droplet.util.UploadUtil.UploadResult;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealmManagerMenu extends ChestPacketMenu implements PacketMenuSlotHandler {

	private static final AtomicBoolean retrievingEarnings = new AtomicBoolean(false);
	
	private static long lastUpload = 0;
	
	private static final Material[] serverIconOptions = new Material[] {
			Material.DIAMOND_ORE,
			Material.COAL_ORE,
			Material.EMERALD_ORE,
			Material.GOLD_ORE,
			Material.REDSTONE_ORE,
			Material.IRON_ORE,
			Material.LAPIS_ORE,
			Material.QUARTZ_ORE,
			Material.LOG,
			Material.DIAMOND,
			Material.COAL,
			Material.EMERALD_BLOCK,
			Material.WOOD_SWORD,
			Material.DIAMOND_SWORD,
			Material.GOLD_SWORD,
			Material.IRON_SWORD,
			Material.GOLD_PICKAXE,
			Material.DIAMOND_PICKAXE,
			Material.GOLD_BLOCK,
			Material.IRON_PICKAXE,
			Material.STONE_PICKAXE,
			Material.WOOD_PICKAXE,
			Material.GOLD_AXE,
			Material.IRON_AXE,
			Material.STONE_AXE,
			Material.WOOD_AXE,
			Material.STONE_SWORD,
			Material.DIAMOND_AXE,
			Material.ANVIL,
			Material.DIRT,
			Material.GRASS,
			Material.BEACON,
			Material.MINECART,
			Material.EXPLOSIVE_MINECART,
			Material.PRISMARINE_CRYSTALS,
			Material.NETHER_BRICK
	};
	
	public RealmManagerMenu(Player player) {
		super(54, "Realm Settings");
		
		addGeneralHandler(this);
		init(player);
		
		setClickSound(Sound.BLOCK_NOTE_BASS);
		
	}
	
	private void init(Player player) {

		String rp = DropletAPI.getThisServer().getResourcePack();

		if (rp == null) {
			rp = Language.getText(player, "menu_items.realm.resource_pack.none");
		}

		Item support = new Item(Material.EMPTY_MAP).setTitle(ChatColor.WHITE + "Generate Code").setLore(ChatColor.RED + "Use when asked for support.");
		if (DropletAPI.getRank(player).hasPermission("playerrealms.manage")) {
			support = new Item(Material.EMPTY_MAP).setTitle(ChatColor.WHITE + "View Log").setLore(ChatColor.RED + "Staff only.");
		}
		addItem(5, 6, support.build());
		addItem(5, 3, new Item(Material.CHEST).setTitle(Language.getText(player, "menu_items.realm.plugins.title")).setLore(Language.getText(player, "menu_items.realm.plugins.lore")).build());
		addItem(5, 4, new Item(Material.IRON_INGOT).setTitle(Language.getText(player, "menu_items.realm.maxplayers.set")).build(), new PacketMenuSlotHandler() {

			@Override
			public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
				menu.close();

				AnvilPacketMenu anvil = new AnvilPacketMenu();
				anvil.setDefaultText("15");
				anvil.setResult(new Item(Material.ANVIL).build());
				anvil.setHandler(new AnvilPacketMenuHandler() {
					@Override
					public void onResult(String text, Player player) {
						try {
							int maxPlayers = Integer.parseInt(text);
							ServerInformation server = DropletAPI.getThisServer();
							int defaultMax = 15;
							if (server.isUltraPremium()) {
								defaultMax = 50;
							} else if (server.isPremium()) {
								defaultMax = 25;
							}
							if (maxPlayers > defaultMax) {
								Language.sendMessage(player, "menu_items.realm.maxplayers.over");
								return;
							}
							if (maxPlayers <= 0) {
								Language.sendMessage(player, "menu_items.realm.maxplayers.zero");
								return;
							}
							DropletAPI.setMetadata(server.getName(), "maxplayers", String.valueOf(maxPlayers));
							Language.sendMessage(player, "menu_items.realm.maxplayers.success");
						} catch (NumberFormatException e) {
							Language.sendMessage(player, "menu_items.realm.maxplayers.number");
						}
					}
				});
				anvil.open(player);
			}
		});
		addItem(3, 3, new Item(Material.WORKBENCH).setTitle(Language.getText(player, "menu_items.realm.resource_pack.title")).setLore(Language.getText(player, "menu_items.realm.resource_pack.lore", rp)).build());
		addItem(4, 4, new Item(Material.COMMAND).setTitle(Language.getText(player, "menu_items.realm.edit_game_rules")).build());
		addItem(3, 5, new Item(Material.PAPER).setTitle(Language.getText(player, "menu_items.realm.config")).build());
		addItem(6, 4, new Item(Material.BOOK).setTitle(Language.getText(player, "menu_items.realm.change_motd.title")).setLore(Language.getText(player, "menu_items.realm.change_motd.lore.1"), Language.getText(player, "menu_items.realm.change_motd.lore.2"), "",
				Language.getText(player, "menu_items.realm.change_motd.lore.3")).build());

		if (DropletAPI.getThisServer().isPremium()) {
			addItem(7, 3, new Item(Material.EMERALD_BLOCK).setTitle(Language.getText(player, "menu_items.realm.icon.title")).setLore(Language.getText(player, "menu_items.realm.icon.lore.1"), "", Language.getText(player, "menu_items.realm.icon.lore.unlocked")).build());
		} else {
			addItem(7, 3, new Item(Material.EMERALD_BLOCK).setTitle(Language.getText(player, "menu_items.realm.icon.title")).setLore(Language.getText(player, "menu_items.realm.icon.lore.1"), "", Language.getText(player, "menu_items.realm.icon.lore.locked")).build());
		}

		addItem(7, 5, new Item(Material.ENDER_CHEST).setTitle(Language.getText(player, "menu_items.realm.configure_shop.title")).setLore(Language.getText(player, "menu_items.realm.configure_shop.lore")).build());

		addItem(9, 6, new Item(Material.DIAMOND_AXE).setTitle(Language.getText(player, "menu_items.realm.restart")).build());

		Item danger = new Item(Material.WATER_BUCKET).setTitle(Language.getText(player, "menu_items.realm.danger.disabled.title")).setLore(Language.getText(player, "menu_items.realm.danger.disabled.lore.1"), Language.getText(player, "menu_items.realm.danger.disabled.lore.2"), Language.getText(player, "menu_items.realm.danger.disabled.lore.3"));
		if (DropletAPI.getThisServer().getMetadata().getOrDefault("danger", "false").equals("true")) {
			danger = new Item(Material.LAVA_BUCKET).setTitle(Language.getText(player, "menu_items.realm.danger.enabled.title")).setLore(Language.getText(player, "menu_items.realm.danger.enabled.lore.1"), Language.getText(player, "menu_items.realm.danger.disabled.lore.2"), Language.getText(player, "menu_items.realm.danger.disabled.lore.3"));
		}

		long ultraLeft = 0;

		if (DropletAPI.getThisServer().getMetadata().containsKey("ultra_time")) {
			ultraLeft = Long.parseLong(DropletAPI.getThisServer().getMetadata().get("ultra_time"));
			ultraLeft -= System.currentTimeMillis();
		}
		if (!ServerDroplet.getInstance().getMenuPermission().checkUUID(player.getUniqueId().toString())) {
			addItem(9, 5, danger.build());
			if (DropletAPI.getThisServer().isPremium()) {

				addItem(4, 1, new Item(Material.GOLDEN_APPLE).setTitle(Language.getText(player, "menu_items.realm.featured_server.title")).setLore(
						Language.getText(player, "menu_items.realm.featured_server.lore.1"),
						"",
						Language.getText(player, "menu_items.realm.premium.lore.2"),
						"",
						Language.getText(player, "menu_items.realm.featured_server.lore.2"),
						Language.getText(player, "menu_items.realm.featured_server.lore.3")).build());

				addUltraPremiumItem(player, ultraLeft, 5, 1);

				addItem(6, 1, new Item(Material.TOTEM).setTitle(Language.getText(player, "menu_items.realm.premium.title")).setLore(
						Language.getText(player, "menu_items.realm.premium.lore.1"),
						"",
						Language.getText(player, "menu_items.realm.premium.lore.2"),
						"",
						Language.getText(player, "menu_items.realm.premium.lore.3"),
						Language.getText(player, "menu_items.realm.premium.lore.4"),
						Language.getText(player, "menu_items.realm.premium.lore.5"),
						Language.getText(player, "menu_items.realm.premium.lore.6"),
						Language.getText(player, "menu_items.realm.premium.lore.7"),
						Language.getText(player, "menu_items.realm.premium.lore.8")).build());
			} else {

				addUltraPremiumItem(player, ultraLeft, 4, 1);

				addItem(6, 1, new Item(Material.TOTEM).setTitle(Language.getText(player, "menu_items.realm.premium.title")).setLore(
						Language.getText(player, "menu_items.realm.premium.lore.1"),
						"",
						Language.getText(player, "menu_items.realm.premium.lore.2"),
						"",
						Language.getText(player, "menu_items.realm.premium.lore.3"),
						Language.getText(player, "menu_items.realm.premium.lore.4"),
						Language.getText(player, "menu_items.realm.premium.lore.5"),
						Language.getText(player, "menu_items.realm.premium.lore.6"),
						Language.getText(player, "menu_items.realm.premium.lore.7"),
						Language.getText(player, "menu_items.realm.premium.lore.8")).build());
			}

			addItem(4, 6, new Item(Material.SKULL_ITEM).setSkullType(Item.SkullType.PLAYER).setTitle(Language.getText(player, "menu_items.realm.menu_permission.title")).setLore(
					Language.getText(player, "menu_items.realm.menu_permission.lore.1"),
					Language.getText(player, "menu_items.realm.menu_permission.lore.2"),
					Language.getText(player, "menu_items.realm.menu_permission.lore.3"),
					Language.getText(player, "menu_items.realm.menu_permission.lore.4"),
					Language.getText(player, "menu_items.realm.menu_permission.lore.5")).build());
			addItem(1, 4, new Item(Material.FLINT_AND_STEEL).setTitle(Language.getText(player, "menu_items.realm.reset_op")).build());
			addItem(9, 2, new Item(Material.GRASS).setTitle(Language.getText(player, "menu_items.realm.download")).build());
			if (DropletAPI.getThisServer().isUltraPremium() || DropletAPI.getTax() <= 0) {
				addItem(5, 5, new Item(Material.GOLD_NUGGET).setTitle(Language.getText(player, "menu_items.realm.earnings.title"))
						.setLore(Language.getText(player, "menu_items.realm.earnings.lore.1"),
								"",
								Language.getText(player, "menu_items.realm.earnings.lore.2"),
								ChatColor.GRAY.toString() + DropletAPI.getThisServer().getEarnings()).build());
			} else {
				addItem(5, 5, new Item(Material.GOLD_NUGGET).setTitle(Language.getText(player, "menu_items.realm.earnings.title"))
						.setLore(Language.getText(player, "menu_items.realm.earnings.lore.1"),
								"",
								Language.getText(player, "menu_items.realm.earnings.lore.2"),
								ChatColor.GRAY.toString() + DropletAPI.getThisServer().getEarnings(),
								"",
								Language.getText(player, "menu_items.realm.earnings.lore.tax_1", DropletAPI.getTax()),
								Language.getText(player, "menu_items.realm.earnings.lore.tax_2", DropletAPI.getTax())).build());
			}
		}
/*
		int gems = DropletAPI.getThisServersGems();
		if(gems > 0) {
			addItem(5, 6, new Item(Material.EMERALD).setTitle(Language.getText(player, "menu_items.realm.gem_earnings.title"))
					.setLore(Language.getText(player, "menu_items.realm.gem_earnings.lore.1"),
							"", 
							Language.getText(player, "menu_items.realm.gem_earnings.lore.2"), 
							ChatColor.GRAY.toString()+DropletAPI.getThisServer().getEarnings()).build());
		}
*/
		if (!DropletAPI.getThisServer().isClosedForDevelopment()) {
			addItem(1, 1, new Item(Material.LIME_SHULKER_BOX).setTitle(Language.getText(player, "menu_items.realm.dev.opened"))
					.setLore(
							Language.getText(player, "menu_items.realm.dev.lore.1"),
							Language.getText(player, "menu_items.realm.dev.lore.2"),
							Language.getText(player, "menu_items.realm.dev.lore.3", DropletAPI.getThisServer().getName())).build());
		} else {
			addItem(1, 1, new Item(Material.RED_SHULKER_BOX).setTitle(Language.getText(player, "menu_items.realm.dev.closed"))
					.setLore(
							Language.getText(player, "menu_items.realm.dev.lore.1"),
							Language.getText(player, "menu_items.realm.dev.lore.2"),
							Language.getText(player, "menu_items.realm.dev.lore.3", DropletAPI.getThisServer().getName())).build());
		}
		
/*
		int coinBonus = (int) (DropletAPI.getThisServer().getCoinMultiplier() * 100);
		
		if(coinBonus > 0){
			addItem(8, new Item(Material.EMERALD).setTitle(Language.getText(player, "menu_items.realm.coin_bonus.title")).setLore(
					Language.getText(player, "menu_items.realm.coin_bonus.status", coinBonus), "",
					Language.getText(player, "menu_items.realm.coin_bonus.note"),
					ChatColor.AQUA+Language.getText(player, "generic.store_url")).build());
		}else{
			addItem(8, new Item(Material.EMERALD).setTitle(Language.getText(player, "menu_items.realm.coin_bonus.title")).setLore(
					Language.getText(player, "menu_items.realm.coin_bonus.status", Language.getText(player, "menu_items.realm.coin_bonus.none")), "",
					Language.getText(player, "menu_items.realm.coin_bonus.note"),
					ChatColor.AQUA+Language.getText(player, "generic.store_url")).build());
		}
*/
		if(!ServerDroplet.getInstance().getMenuPermission().checkUUID(player.getUniqueId().toString())) {
			if (DropletAPI.getThisServer().isUltraPremium()) {
				long cooldown = Long.parseLong(DropletAPI.getThisServer().getMetadata().getOrDefault("lastcm", "0"));
				String cmcooldown = JedisAPI.getValue("cm_cooldown");
				if (cmcooldown == null) {
					cmcooldown = String.valueOf(System.currentTimeMillis());
				}
				long cdTime = Long.valueOf(cmcooldown) * 1000 * 60;
				if (System.currentTimeMillis() - cooldown > cdTime) {

					addItem(8, new Item(Material.DIAMOND)
							.setTitle(Language.getText(player, "koukoku.title"))
							.setLore(Language.getText(player, "koukoku.lore.1"), Language.getText(player, "koukoku.lore.2")).build(), new PacketMenuSlotHandler() {

						@Override
						public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
							DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "lastcm", String.valueOf(System.currentTimeMillis()));
							JedisAPI.publish("server_cm", DropletAPI.getThisServer().getName());
							menu.close();
						}

					});

				} else {
					addItem(8, new Item(Material.DIAMOND)
							.setTitle(Language.getText(player, "koukoku.title"))
							.setLore(Language.getText(player, "koukoku.lore.1"), Language.getText(player, "koukoku.lore.2"), "", Language.getText(player, "koukoku.lore.cooldown", (cdTime - (System.currentTimeMillis() - cooldown)) / 60000))
							.build());
				}

			} else {
				addItem(8, new Item(Material.DIAMOND)
						.setTitle(Language.getText(player, "koukoku.title"))
						.setLore(Language.getText(player, "koukoku.lore.1"), Language.getText(player, "koukoku.lore.2"), Language.getText(player, "koukoku.lore.require")).build(), new PacketMenuSlotHandler() {

					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						Language.sendMessage(player, "koukoku.purchase");
					}
				});
			}
		}
		
		if(Bukkit.getPluginManager().isPluginEnabled("Skript")){
			addItem(9, 4, new Item(Material.BOOK_AND_QUILL).setTitle(Language.getText(player, "menu_items.realm.skript")).build());
		}

		if(Bukkit.getPluginManager().isPluginEnabled("MassiveCore")){
			addItem(9, 3, new Item(Material.PAPER).setTitle(Language.getText(player, "menu_items.realm.massive")).setAmount(2).build());
		}
		
		String lang = DropletAPI.getThisServer().getLanguage();
		
		ItemStack banner = getLanguageBanner(lang);
		
		ItemMeta meta = banner.getItemMeta();
		
		meta.setDisplayName(Language.getText(player, "menu_items.language.title"));
		meta.setLore(Arrays.asList(new String[] {
				Language.getText(player, "menu_items.language.lore.1"),
				Language.getText(player, "menu_items.language.lore.2")
		}));
		
		
		createUpdateTask(() -> {
			long startTime = DropletAPI.getThisServer().getStartTime();
			if(startTime == 0){
				startTime = System.currentTimeMillis();
			}
			long onlineTime = System.currentTimeMillis() - startTime;
			
			long hours = onlineTime / 1000 / 60 / 60;
			long minutes = (onlineTime - (hours * 1000 * 60 * 60)) / 1000 / 60;
			long seconds = (onlineTime - (hours * 1000 * 60 * 60) - (minutes * 1000 * 60)) / 1000;
			
			addItem(1, 2, 
					new Item(Material.TORCH).setTitle(Language.getText(player, "menu_items.online_time"))
					.setLore(ChatColor.GREEN.toString()+
							StringUtils.leftPad(String.valueOf(hours), 2, '0')+":"+
							StringUtils.leftPad(String.valueOf(minutes), 2, '0')+":"+
							StringUtils.leftPad(String.valueOf(seconds), 2, '0')).build());

		}, 5, 20);
		
		addItem(1, 2, new Item(Material.TORCH).setTitle(Language.getText(player, "menu_items.online_time")).build());
		
		banner.setItemMeta(meta);
		
		addItem(1, 6, banner);
		
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getLanguageBanner(String lang){
		
		ItemStack banner = new Item(Material.BANNER).build();
		
		BannerMeta meta = (BannerMeta) banner.getItemMeta();
		
		if(lang.equals("en_us")){
			meta.setBaseColor(DyeColor.WHITE);
			meta.addPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_SMALL));
			meta.addPattern(new Pattern(DyeColor.BLUE, PatternType.SQUARE_TOP_RIGHT));
		}else if(lang.equals("ja_jp")){
			meta.setBaseColor(DyeColor.WHITE);
			meta.addPattern(new Pattern(DyeColor.RED, PatternType.CIRCLE_MIDDLE));
		}else{
			meta.setBaseColor(DyeColor.PINK);
		}
		
		banner.setItemMeta(meta);
		
		return banner;
	}
	
	private void addUltraPremiumItem(Player player, long ultraLeft, int x, int y) {
		

		long daysLeft = TimeUnit.MILLISECONDS.toDays(ultraLeft);
		long hoursLeft = TimeUnit.MILLISECONDS.toHours(ultraLeft) - daysLeft * 24;
		long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(ultraLeft) - (daysLeft * 24 + hoursLeft) * 60;
		
		if(ultraLeft > 0 || DropletAPI.getThisServer().isUltraPremium()) {
			if(ultraLeft <= 0 && DropletAPI.getThisServer().isUltraPremium()) {//Bought through buycraft
				addItem(x, y, new Item(Material.NETHER_STAR).setTitle(Language.getText(player, "ultra_premium_purchase.purchase")).setLore(
						ChatColor.GREEN.toString()+ChatColor.BOLD+"UNLOCKED!",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.1"),
						"",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.2"),
						"",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.3", DropletAPI.getThisServersGems()),
						Language.getText(player, "ultra_premium_purchase.purchase_lore.4"),
						Language.getText(player, "ultra_premium_purchase.purchase_lore.5")).build());
			}else {
				addItem(x, y, new Item(Material.NETHER_STAR).setTitle(Language.getText(player, "ultra_premium_purchase.purchase")).setLore(
						ChatColor.GREEN.toString()+ChatColor.BOLD+"UNLOCKED!",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.1"),
						"",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.2"),
						"",
						Language.getText(player, "ultra_premium_purchase.purchase_lore.3", DropletAPI.getThisServersGems()),
						Language.getText(player, "ultra_premium_purchase.purchase_lore.4"),
						Language.getText(player, "ultra_premium_purchase.purchase_lore.5"),
						Language.getText(player, "ultra_premium_purchase.purchase_lore.6", daysLeft, hoursLeft, minutesLeft)).build());
			}
		}else {
			addItem(x, y, new Item(Material.COAL).setTitle(Language.getText(player, "ultra_premium_purchase.purchase")).setLore(
					Language.getText(player, "ultra_premium_purchase.purchase_lore.1"),
					"",
					Language.getText(player, "ultra_premium_purchase.purchase_lore.2"),
					"",
					Language.getText(player, "ultra_premium_purchase.purchase_lore.3", DropletAPI.getThisServersGems()),
					
					Language.getText(player, "ultra_premium_purchase.purchase_lore.5")).build());
		}
	}
	
	@Override
	public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
		if(interactionInfo.getItem().getType() == Material.CHEST){
			PluginManagerMenu pluginMenu = new PluginManagerMenu(player);
			cancelUpdateTask();
			pluginMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.FLINT_AND_STEEL){
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F);
			Bukkit.getOperators().forEach(op -> op.setOp(false));
			player.setOp(true);
			Language.sendMessage(player, "menu.realm.op_reset");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user "+player.getName()+" add *");
		}else if(interactionInfo.getItem().getType() == Material.COMMAND){
			GameRuleManagerMenu gameRuleMenu = new GameRuleManagerMenu(player.getWorld());
			cancelUpdateTask();
			gameRuleMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.PAPER && interactionInfo.getItem().getAmount() == 1) {
			FolderBrowseMenu folderBrowser = new FolderBrowseMenu(new File("plugins"), player);
			cancelUpdateTask();
			folderBrowser.open(player);
		}else if(interactionInfo.getItem().getType() == Material.PAPER && interactionInfo.getItem().getAmount() == 2){
			MassiveCoreEditMenu massivemenu = new MassiveCoreEditMenu(new File("mstore"), player);
			cancelUpdateTask();
			massivemenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.BOOK){
			menu.close();
			
			player.sendMessage("");
			Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(new ChangeMOTDPrompt()).withEscapeSequence("cancel").buildConversation(player);
			player.beginConversation(conversation);
			player.sendMessage("");
		}else if(interactionInfo.getItem().getType() == Material.EMERALD_BLOCK){
			
			if(!DropletAPI.getThisServer().isPremium()){
				Language.sendMessage(player, "menu.realm.server_icon_fail");
				return;
			}

			SelectIconMenu iconMenu = new SelectIconMenu(serverIconOptions);

			cancelUpdateTask();
			iconMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.TOTEM){
			
			PremiumMenu premiumMenu = new PremiumMenu(player);
			cancelUpdateTask();
			premiumMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.GOLDEN_APPLE){
			
			FeaturedMenu featuredMenu = new FeaturedMenu(player);
			
			cancelUpdateTask();
			
			featuredMenu.open(player);
			
		}else if(interactionInfo.getItem().getType() == Material.DIAMOND_AXE){
			DropletAPI.restartServer(DropletAPI.getThisServer().getName());
		}else if(interactionInfo.getItem().getType() == Material.ENDER_CHEST){
			ConfigureServerShopMenu shopMenu = new ConfigureServerShopMenu(player);
			cancelUpdateTask();
			shopMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.GOLD_NUGGET){

			if(retrievingEarnings.get()) {
				return;
			}
			
			retrievingEarnings.set(true);
			
			int earnings = DropletAPI.getThisServer().getEarnings();
			int loss = 0;
			
			double tax = DropletAPI.getTax();
			
			if(tax > 0){
				tax /= 100D;
				loss = (int) (earnings * tax);
			}
			
			final int calculated = earnings - loss;
			
			final int lossCalculated = loss;
			
			if(calculated == 0){
				Language.sendMessage(player, "menu.realm.earnings.fail");
			}else{
				Language.sendMessage(player, "menu.realm.earnings.claiming");
				menu.close();
				if(!ServerDroplet.isClientActive()){
					Language.sendMessage(player, "menu.realm.earnings.failed");
					return;
				}
				DropletAPI.setEarnings(DropletAPI.getThisServer(), 0, code -> {
					
					retrievingEarnings.set(false);
					
					int coins = DropletAPI.getCoins(player) + calculated;
					
					DropletAPI.setCoins(player, coins);
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
					Language.sendMessage(player, "menu.realm.earnings.claimed", String.valueOf(calculated));
					Language.sendMessage(player, "menu.realm.earnings.tax", String.valueOf(lossCalculated));//Fucking java
					new RealmManagerMenu(player).open(player);
					
					DropletAPI.setTaxedMoney(DropletAPI.getThisServer(), lossCalculated + DropletAPI.getThisServer().getTaxedMoney());
					
				});
			}
			
		}else if(interactionInfo.getItem().getType() == Material.RED_SHULKER_BOX){
			addItem(interactionInfo.getSlot(), new Item(Material.GRAY_SHULKER_BOX).setTitle(ChatColor.GRAY+"......").build());
			DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "indev", "false", code -> {
				clearInventory();
				init(player);
			});
		}else if(interactionInfo.getItem().getType() == Material.LIME_SHULKER_BOX){
			addItem(interactionInfo.getSlot(), new Item(Material.GRAY_SHULKER_BOX).setTitle(ChatColor.GRAY+"......").build());
			DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "indev", "true", code -> {
				clearInventory();
				init(player);
			});
		}else if(interactionInfo.getItem().getType() == Material.BANNER){
			ChestPacketMenu selectLang = new ChestPacketMenu(9, "Select Language");
			
			ItemStack english = getLanguageBanner("en_us");
			
			ItemMeta meta = english.getItemMeta();
			meta.setDisplayName(Language.getText(player, "language.en_us"));
			english.setItemMeta(meta);
			
			selectLang.addItem(0, english);
			
			ItemStack japanese = getLanguageBanner("ja_jp");
			
			meta = japanese.getItemMeta();
			meta.setDisplayName(Language.getText(player, "language.ja_jp"));
			japanese.setItemMeta(meta);
			selectLang.addItem(1, japanese);
			
			selectLang.addGeneralHandler(new PacketMenuSlotHandler() {
				
				@Override
				public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
					if(interactionInfo.getSlot() == 0){
						DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "lang", "en_us", code -> {
							RealmManagerMenu manager = new RealmManagerMenu(player);
							manager.open(player);
						});
						
					}else if(interactionInfo.getSlot() == 1){
						DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "lang", "ja_jp", code -> {
							RealmManagerMenu manager = new RealmManagerMenu(player);
							manager.open(player);
						});
					}
				}
			});
			cancelUpdateTask();
			
			selectLang.open(player);
/*		}else if(interactionInfo.getItem().getType() == Material.WRITTEN_BOOK){
			FunctionEditMenu editMenu = new FunctionEditMenu(new File(player.getWorld().getWorldFolder(), "data/functions"));
			
			editMenu.open(player);*/
		}else if(interactionInfo.getItem().getType() == Material.BOOK_AND_QUILL){
			
			SkriptEditMenu skriptMenu = new SkriptEditMenu();
			
			skriptMenu.open(player);
		}else if(interactionInfo.getItem().getType() == Material.COAL) {
			
			if(DropletAPI.getThisServersGems() >= 1000) {
				menu.close();
				Language.sendMessage(player, "ultra_premium_purchase.please_wait");
				if(!ServerDroplet.isClientActive()){
					Language.sendMessage(player, "ultra_premium_purchase.failed2");
					return;
				}
				if(DropletAPI.createGemTransaction(DropletAPI.getThisServer().getUUID(), "Server "+DropletAPI.getThisServer().getName(), -1000, "Bought ultra premium")) {
					
					/*long time = Long.parseLong(DropletAPI.getThisServer().getMetadata().getOrDefault("ultra_time", "0"));
					
					if(time == 0) {
						time = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);
					}else {
						time += TimeUnit.DAYS.toMillis(30);
					}*/

					long time = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);

					if(!ServerDroplet.isClientActive()){
						Language.sendMessage(player, "ultra_premium_purchase.failed2");
						return;
					}

					ServerInformation server = DropletAPI.getThisServer();

					if(server.getName() == null || server.getName().isEmpty()){
						Language.sendMessage(player, "ultra_premium_purchase.failed2");
						return;
					}
					
					DropletAPI.setMetadata(server.getName(), "ultra_time", String.valueOf(time));
				
					long premium = server.getPremiumLeft();
					
					premium += TimeUnit.DAYS.toMillis(30);
					
					premium += System.currentTimeMillis();
					
					DropletAPI.setMetadata(server.getName(), "premiumtime", String.valueOf(premium));
					
					Language.sendMessage(player, "ultra_premium_purchase.done");
					
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
					new RealmManagerMenu(player).open(player);
				}else {
					Language.sendMessage(player, "ultra_premium_purchase.failed");
				}
			}
			
		}else if(interactionInfo.getItem().getType() == Material.GRASS){
			
			if(System.currentTimeMillis() - lastUpload < 60000){
				long seconds = System.currentTimeMillis() - lastUpload;
				seconds /= 1000;
				seconds = 60 - seconds;
				player.sendMessage(Language.getText(player, "upload.cooldown", seconds));
				return;
			}
			
			lastUpload = System.currentTimeMillis();
			
			menu.close();
			
			World world = player.getWorld();
			
			player.sendMessage(Language.getText(player, "upload.saving"));
			
			world.save();
			
			File folder = world.getWorldFolder();
			
			File out = new File("world.zip");

			player.sendMessage(Language.getText(player, "upload.zipping"));
			
			ZipUtil.pack(folder, out);
			
			try {
				player.sendMessage(Language.getText(player, "upload.uploading"));
				UploadResult result = UploadUtil.uploadFile(out);
				
				if(result.isSuccess()){
					player.sendMessage(Language.getText(player, "upload.url", result.getUrl()));
				}else{
					player.sendMessage(Language.getText(player, "upload.fail")+" "+result.getError()+" "+result.getMessage());
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
				player.sendMessage(Language.getText(player, "upload.fail"));
			}
			
			out.delete();
			
		}else if(interactionInfo.getItem().getType() == Material.WORKBENCH) {
			menu.close();
			
			if(DropletAPI.getThisServer().isUltraPremium()) {
				player.sendMessage("");
				Conversation conversation = new ConversationFactory(ServerDroplet.getInstance()).withFirstPrompt(new ChangeRPPrompt()).withEscapeSequence("cancel").buildConversation(player);
				player.beginConversation(conversation);
				player.sendMessage("");
			}else {
				Language.sendMessage(player, "menu.realm.resource_pack_fail");
			}
			

		}else if(interactionInfo.getItem().getType() == Material.EMPTY_MAP) {
			menu.close();
			boolean bypa = false;
			if(DropletAPI.getRank(player).hasPermission("playerrealms.manage")){
				bypa = true;
			}
			String result = UploadLog.uploadlog(bypa);
			if (result != null) {
				if(result != "rateLimit") {
					player.sendMessage(ChatColor.GREEN + "Your server code has been generated. Code: " + result);
					player.sendMessage(ChatColor.GREEN + "You can use this code to facilitate support.");
				}else{
					player.sendMessage(ChatColor.RED + "Can be used only once every 5 minutes.");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Failed to generate server code.");
			}
		}else if(interactionInfo.getItem().getType() == Material.WATER_BUCKET) {
			addItem(interactionInfo.getSlot(), new Item(Material.BUCKET).setTitle(ChatColor.GRAY+"......").build());
			DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "danger", "true", code -> {
				clearInventory();
				init(player);
			});
		}else if(interactionInfo.getItem().getType() == Material.LAVA_BUCKET) {
			addItem(interactionInfo.getSlot(), new Item(Material.BUCKET).setTitle(ChatColor.GRAY+"......").build());
			DropletAPI.setMetadata(DropletAPI.getThisServer().getName(), "danger", "false", code -> {
				clearInventory();
				init(player);
			});
		}else if(interactionInfo.getItem().getType() == Material.SKULL_ITEM) {
			RealmPermissionMenu permissionMenu = new RealmPermissionMenu(ServerDroplet.getInstance().getMenuPermission(), player);
			permissionMenu.open(player);
		}
	}
}
