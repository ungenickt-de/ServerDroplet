package com.playerrealms.droplet.menu.hub;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentHandlers.B2ContentFileWriter;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.nirvana.menu.*;
import com.playerrealms.backblaze.Backblaze;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.zeroturnaround.zip.commons.FileUtils;
import org.zeroturnaround.zip.commons.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class BackupB2ConfirmMenu extends ChestPacketMenu {
    public BackupB2ConfirmMenu(ServerInformation server, Player player, int round, B2FileVersion file, String type) {
        super(54, "Click the diamond " + ChatColor.DARK_RED + "(" + round + ")");
        init(server, player, round,file,type);
    }

    private void init(ServerInformation server, Player player, int round,B2FileVersion file,String type)
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
                                try {
                                    Backblaze.getClient().deleteFileVersion(file);
                                    Language.sendMessage(player, "backup.complete");
                                } catch (B2Exception e){
                                    e.printStackTrace();
                                    player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
                                    break;
                                }
                                new BackupMenu(player, server).open(player);
                                break;
                            case "RESTORE":
                                GridFSBucket targetBucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_files");
                                B2StorageClient client = Backblaze.getClient();
                                UUID plId = player.getUniqueId();
                                BackupMenu.allowed.put(player.getUniqueId(), false);

                                new Thread(() -> {
                                    Language.sendMessage(player, "backup.please_wait");
                                    File tempFile;
                                    File tempDir;
                                    try {
                                        String[] filename = file.getFileName().split("/", 0);
                                        tempDir = new File("./tmp/");
                                        if (!tempDir.exists()) {
                                            tempDir.mkdir();
                                        }
                                        tempFile = new File("./tmp/" + filename[2]);
                                        if (!tempFile.exists()) {
                                            tempFile.createNewFile();
                                        }
                                        tempDir = new File("./tmp/" + filename[2].replace(".zip", ""));

                                        B2ContentFileWriter handler = B2ContentFileWriter
                                                .builder(tempFile)
                                                .build();
                                        client.downloadById(file.getFileId(), handler);

                                        FileInputStream download = new FileInputStream(tempFile);
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

                                        tempFile.delete();
                                        FileUtils.deleteDirectory(tempDir);
                                        Language.sendMessage(player, "backup.complete");
                                    } catch (IOException | B2Exception e) {
                                        e.printStackTrace();
                                        player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
                                    } finally {
                                        BackupMenu.allowed.put(plId, true);
                                        if(Bukkit.getPlayer(plId) != null) {
                                            new BackupMenu(Bukkit.getPlayer(plId), server).open(Bukkit.getPlayer(plId));
                                        }
                                    }
                                }).start();
                                break;
                        }
                    }
                    else
                    {
                        new BackupB2ConfirmMenu(server, player,round-1, file,type).open(player);
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
