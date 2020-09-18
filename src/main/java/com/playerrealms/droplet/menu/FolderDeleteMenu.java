package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class FolderDeleteMenu extends ChestPacketMenu{
    public FolderDeleteMenu(File folder, int round, Player player) {
        super(54, "Click the diamond " + ChatColor.DARK_RED + "(" + round + ")");
        init(folder, player, round);
    }

    private void init(File folder, Player player, int round)
    {
        for(int i = 0; i < this.getSize(); i++)
        {
            addItem(i, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "menu_items.realm.delete_folder.cancel")).build());
        }
        Random random = new Random();
        int slot = random.nextInt(getSize());
        addItem(slot, new Item(Material.DIAMOND).setTitle(Language.getText(player, "menu_items.realm.delete_folder.confirm")).build());
        addGeneralHandler(new PacketMenuSlotHandler()
        {
            @Override
            public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                if(interactionInfo.getSlot() == slot)
                {
                    if(round == 1)
                    {
                        try {
                            FileUtils.deleteDirectory(folder);
                        }catch (IOException e){ e.printStackTrace(); }
                        new FolderBrowseMenu(new File("plugins"), player).open(player);
                    }
                    else
                    {
                        new FolderDeleteMenu(folder, round-1, player).open(player);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                    }
                }
                else
                {
                    menu.close();
                }
            }
        });
    }

}
