package com.playerrealms.droplet.menu.hub;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.nirvana.menu.*;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.zeroturnaround.zip.commons.IOUtils;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class BackupConfirmMenu extends ChestPacketMenu {
    public BackupConfirmMenu(ServerInformation server, Player player,int round, GridFSFile file,String type) {
        super(54, "Click the diamond " + ChatColor.DARK_RED + "(" + round + ")");
        init(server, player, round,file,type);
    }

    private void init(ServerInformation server, Player player, int round,GridFSFile file,String type)
    {
        for(int i = 0; i < this.getSize(); i++)
        {
            addItem(i, new Item(Material.WOOL).setData(14).setTitle(Language.getText(player, "backup.cancel")).build());
        }
        Random random = new Random();
        int slot = random.nextInt(getSize());
        addItem(slot, new Item(Material.DIAMOND).setTitle(Language.getText(player, "backup.confirm")).build());
        addGeneralHandler(new PacketMenuSlotHandler()
        {
            @Override
            public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                if(interactionInfo.getSlot() == slot)
                {
                    if(round == 1)
                    {
                        menu.close();
                        switch (type){
                            case "DELETE":
                                GridFSBucket bucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_backups");
                                bucket.delete(file.getObjectId());
                                new BackupMenu(player, server).open(player);
                                break;
                            case "RESTORE":
                                GridFSBucket targetBucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_files");
                                GridFSBucket backupBucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_backups");

                                UUID plId = player.getUniqueId();
                                BackupMenu.allowed.put(player.getUniqueId(), false);
                                Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {

                                    @Override
                                    public void run() {

                                        try {

                                            GridFSDownloadStream download = backupBucket.openDownloadStream(file.getObjectId());
                                            GridFSUploadStream upload = targetBucket.openUploadStream(server.getName()+".zip");

                                            IOUtils.copy(download, upload);

                                            download.close();
                                            upload.close();

                                            for(GridFSFile other : targetBucket.find(Filters.eq("filename", server.getName()+".zip"))) {
                                                if(!other.getObjectId().equals(upload.getObjectId())) {
                                                    targetBucket.delete(other.getObjectId());
                                                    break;
                                                }
                                            }


                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }finally {
                                            BackupMenu.allowed.put(plId, true);

                                            if(Bukkit.getPlayer(plId) != null) {
                                                new BackupMenu(Bukkit.getPlayer(plId), server).open(Bukkit.getPlayer(plId));
                                            }
                                        }
                                    }
                                });
                                break;
                        }
                    }
                    else
                    {
                        new BackupConfirmMenu(server, player,round-1, file,type).open(player);
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
