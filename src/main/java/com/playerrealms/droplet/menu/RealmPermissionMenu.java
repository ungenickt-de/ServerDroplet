package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.nirvana.menu.menus.PageMenuEntry;
import com.nirvana.menu.menus.PagedMenu;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.MojangAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RealmPermissionMenu extends PagedMenu {
    private UUID viewer;

    public RealmPermissionMenu(RealmMenuPermission menuPermission, Player viewer){
        super(convert(menuPermission), "Permission Setting");
        this.viewer = viewer.getUniqueId();
    }

    private void init(Player viewer){
        addItem(0, new Item(Material.ANVIL).setTitle(ChatColor.GOLD + Language.getText(viewer, "menu_items.realm.menu_permission.item")).build(), (player, menu, interactionInfo) -> {
            AnvilPacketMenu anvil = new AnvilPacketMenu();
            anvil.setDefaultText("Minecraft ID");
            anvil.setClickSound(Sound.BLOCK_LEVER_CLICK);
            anvil.setResult(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
            anvil.setHandler((text, pl) -> {
                Player add = Bukkit.getPlayer(text);
                if(add == null){
                    Language.sendMessage(player, "menu_items.realm.menu_permission.error");
                    return;
                }
                if(DropletAPI.getThisServer().getOwner().equals(add.getUniqueId()) ){
                    Language.sendMessage(player, "menu_items.realm.menu_permission.error");
                    return;
                }
                if(ServerDroplet.getInstance().getMenuPermission().checkUUID(add.getUniqueId().toString())){
                    Language.sendMessage(player, "menu_items.realm.menu_permission.error");
                    return;
                }
                ServerDroplet.getInstance().getMenuPermission().addUUID(add.getUniqueId().toString());
                ServerDroplet.getInstance().getMenuPermission().save();
                new RealmPermissionMenu(ServerDroplet.getInstance().getMenuPermission(), player).open(player);
                Language.sendMessage(player, "menu_items.realm.menu_permission.add", add.getName());
            });
            anvil.open(player);
        });
    }

    private static List<PageMenuEntry> convert(RealmMenuPermission menuPermission) {
        List<PageMenuEntry> entries = new ArrayList<>();

        for(String uuid : menuPermission.getAllowed()){
            entries.add(new RealmPermissionMenuEntry(uuid, menuPermission));
        }
        return entries;
    }

    @Override
    public void open(Player pl) {
        init(pl);
        super.open(pl);
    }

    @Override
    public void remake(boolean refreshTitle) {
        init(Bukkit.getPlayer(viewer));
        super.remake(refreshTitle);
    }

    static class RealmPermissionMenuEntry implements PageMenuEntry{

        private final String uuid;
        private final RealmMenuPermission menuPermission;
        private final String name;

        public RealmPermissionMenuEntry(String uuid, RealmMenuPermission menuPermission){
            this.uuid = uuid;
            this.menuPermission = menuPermission;
            this.name = MojangAPI.getUsername(UUID.fromString(uuid));
        }

        @Override
        public int compareTo(PageMenuEntry o){
            return 0;
        }

        @Override
        public ItemStack getItem() {
            ItemStack skull = new Item(Material.SKULL_ITEM, 1).setTitle(ChatColor.WHITE + name).setSkullType(Item.SkullType.PLAYER).setLore(ChatColor.RED + "Click to remove").build();
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(uuid);
            skull.setItemMeta(meta);
            return skull;
        }

        @Override
        public PacketMenuSlotHandler getHandler(){
            return (player, menu, interactionInfo) -> {
                menuPermission.removeUUID(uuid);
                Language.sendMessage(player, "menu_items.realm.menu_permission.remove", name);
                menu.close();
                new RealmPermissionMenu(ServerDroplet.getInstance().getMenuPermission(), player).open(player);
            };
        }
    }
}
