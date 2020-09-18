package com.playerrealms.droplet.menu.hub;

import com.backblaze.b2.client.B2ListFilesIterable;
import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentHandlers.B2ContentFileWriter;
import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2ListFileNamesRequest;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.nirvana.menu.*;
import com.playerrealms.backblaze.Backblaze;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.droplet.ServerDroplet;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.UploadUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;
import org.zeroturnaround.zip.commons.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackupMenu extends ChestPacketMenu implements PacketMenuSlotHandler {

    public static Map<UUID, Boolean> allowed = Collections.synchronizedMap(new HashMap<>());

    private List<UUID> processing = new ArrayList<>();

    private ServerInformation info;

    private String BUCKETID = "77a550481ca4d5a66bbe0512";

    public BackupMenu(Player player, ServerInformation info) {
        super(3 * 9, "Backup");
        this.info = info;

        int max = 0;

        if (info.isUltraPremium()) {
            max = 5;
        } else {
            max = 2;
        }

        setAddIndex(11);
        int backups = 0;

		if(!ServerDroplet.isBackblazeLoaded()) {
			GridFSFindIterable found = getMongoBackups(player);
			for (GridFSFile file : found) {
				backups++;
				addItem(new Item(Material.BOOK)
						.setTitle(ChatColor.YELLOW + file.getMetadata().getString("backup_name")).setLore(ChatColor.GRAY + file.getUploadDate().toString()).build(), new PacketMenuSlotHandler() {

					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						ChestPacketMenu m = new ChestPacketMenu(9, "Option");

						m.addItem(1, new Item(Material.GRASS).setTitle(Language.getText(player, "backup.restore")).build());
						m.addItem(4, new Item(Material.DIAMOND).setTitle(Language.getText(player, "backup.download")).build());
						m.addItem(7, new Item(Material.BARRIER).setTitle(Language.getText(player, "backup.delete")).build());

						m.addGeneralHandler(new PacketMenuSlotHandler() {

							@Override
							public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
								if (interactionInfo.getItem().getType() == Material.GRASS) {
									menu.close();
									restoreMongoBackup(file, player);
								} else if (interactionInfo.getItem().getType() == Material.BARRIER) {
									menu.close();
									deleteMongoBackup(file, player);
								} else if (interactionInfo.getItem().getType() == Material.DIAMOND) {
									menu.close();
									downloadMongoBackup(file, player);
								}
							}

						});

						m.open(player);

					}

				});
			}
		}else{
			B2ListFilesIterable found;
			try {
				found = getB2Backups(player);
			}catch (B2Exception e) {
			    e.printStackTrace();
				player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
				return;
			}
			for (B2FileVersion file : found) {
				backups++;
				addItem(new Item(Material.BOOK)
						.setTitle(ChatColor.YELLOW + getBackupInfo(file, true)).setLore(ChatColor.GRAY + getBackupInfo(file, false)).build(), new PacketMenuSlotHandler() {

					@Override
					public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
						ChestPacketMenu m = new ChestPacketMenu(9, "Option");
						m.addItem(1, new Item(Material.GRASS).setTitle(Language.getText(player, "backup.restore")).build());
						m.addItem(4, new Item(Material.DIAMOND).setTitle(Language.getText(player, "backup.download")).build());
						m.addItem(7, new Item(Material.BARRIER).setTitle(Language.getText(player, "backup.delete")).build());
						m.addGeneralHandler(new PacketMenuSlotHandler() {
							@Override
							public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
								if (interactionInfo.getItem().getType() == Material.GRASS) {
									menu.close();
									restoreB2Backup(file, player);
								} else if (interactionInfo.getItem().getType() == Material.BARRIER) {
									menu.close();
									deleteB2Backup(file, player);
								} else if (interactionInfo.getItem().getType() == Material.DIAMOND) {
									menu.close();
									downloadB2Backup(file, player);
								}
							}
						});
						m.open(player);
					}
				});
			}
		}

        if (!allowed.getOrDefault(player.getUniqueId(), true)) {
            addItem(0, new Item(Material.IRON_PICKAXE).setTitle(Language.getText(player, "backup.create")).setLore(Language.getText(player, "backup.please_wait")).build());
        } else if (max - backups > 0) {
            addItem(0, new Item(Material.IRON_PICKAXE).setTitle(Language.getText(player, "backup.create"))
                    .setLore(Language.getText(player, "backup.remaining", max - backups)).build(), this);
        } else if (!info.isUltraPremium()) {
            addItem(0, new Item(Material.IRON_PICKAXE).setTitle(Language.getText(player, "backup.create"))
                    .setLore(Language.getText(player, "backup.purchase", max - backups)).build());
        }


    }

    @Override
    public void open(Player pl) {
        if (!allowed.getOrDefault(pl.getUniqueId(), true)) {
            Language.sendMessage(pl, "backup.please_wait");
            pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.3F, 1F);
            return;
        }
        super.open(pl);
    }

    public GridFSFindIterable getMongoBackups(Player player) {
    	GridFSBucket bucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_backups");
    	GridFSFindIterable find = bucket.find(Filters.eq("metadata.owner", player.getUniqueId().toString()));
    	return find;
    }

    public B2ListFilesIterable getB2Backups(Player player) throws B2Exception {
		B2StorageClient client = Backblaze.getClient();
		B2ListFileNamesRequest request = B2ListFileNamesRequest
				.builder(BUCKETID)
				.setWithinFolder("server_backups/" + player.getUniqueId().toString() + "/")
				.build();
		B2ListFilesIterable find = client.fileNames(request);
		return find;
	}

	public String getBackupInfo(B2FileVersion file, boolean type) {
    	if(type) {
			Map<String, String> map = file.getFileInfo();
			String name = map.get("name");
			return name;
		}else{
    		long ts = file.getUploadTimestamp();
			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(ts));
    		return date;
		}
	}

    public void deleteMongoBackup(GridFSFile file, Player player) {
        new BackupConfirmMenu(info, player, 3, file, "DELETE").open(player);
    }

    public void deleteB2Backup(B2FileVersion file, Player player){
    	new BackupB2ConfirmMenu(info, player, 3, file, "DELETE").open(player);
	}

    public void restoreMongoBackup(GridFSFile file, Player player) {
        new BackupConfirmMenu(info, player, 3, file, "RESTORE").open(player);
    }

    public void restoreB2Backup(B2FileVersion file, Player player){
		new BackupB2ConfirmMenu(info, player, 3, file, "RESTORE").open(player);
	}

    public void downloadMongoBackup(GridFSFile file, Player player) {
        new Thread(() -> {
            if (processing.contains(player.getUniqueId())) {
                Language.sendMessage(player, "backup.processing");
                return;
            }
            Language.sendMessage(player, "backup.please_wait");
            processing.add(player.getUniqueId());
            File backupFile;
            File backupDir;
            try {

                GridFSBucket backupBucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_backups");
                backupDir = new File("./backup/");
                if (!backupDir.exists()) {
                    backupDir.mkdir();
                }
                backupFile = new File("./backup/" + file.getFilename());
                if (!backupFile.exists()) {
                    backupFile.createNewFile();
                }
                backupDir = new File("./backup/" + file.getFilename().replace(".zip", ""));
                backupBucket.downloadToStream(file.getObjectId(), new FileOutputStream(backupFile));
                ZipUtil.unpack(backupFile, backupDir);
                if (!backupFile.delete()) {
                    throw new IOException("Failed to delete server file!");
                }
                for (File junk : backupDir.listFiles()) {
                    if (!junk.getName().equals("world")) {
                        if (junk.isFile()) {
                            if (!junk.delete()) {
                                throw new IOException("Failed to delete junk file: " + junk.getName());
                            }
                        } else {
                            FileUtils.deleteDirectory(junk);
                        }
                    }
                }
                ZipUtil.pack(backupDir, backupFile);
                UploadUtil.UploadResult result = UploadUtil.uploadFile(backupFile);
                if (result.isSuccess()) {
                    player.sendMessage(Language.getText(player, "upload.url", result.getUrl()));
                } else {
                    player.sendMessage(Language.getText(player, "upload.fail") + " " + result.getError() + " " + result.getMessage());
                }
                backupFile.delete();
                FileUtils.deleteDirectory(backupDir);
                processing.remove(player.getUniqueId());
            } catch (IOException e) {
                player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
            }
        }).start();
    }

    public void downloadB2Backup(B2FileVersion file, Player player){
        new Thread(() -> {
            B2StorageClient client = Backblaze.getClient();
            if (processing.contains(player.getUniqueId())) {
                Language.sendMessage(player, "backup.processing");
                return;
            }
            Language.sendMessage(player, "backup.please_wait");
            processing.add(player.getUniqueId());
            File backupFile;
            File backupDir;
            try {
                String[] filename = file.getFileName().split("/", 0);
                backupDir = new File("./backup/");
                if (!backupDir.exists()) {
                    backupDir.mkdir();
                }
                backupFile = new File("./backup/" + filename[2]);
                if (!backupFile.exists()) {
                    backupFile.createNewFile();
                }
                backupDir = new File("./backup/" + filename[2].replace(".zip", ""));

                B2ContentFileWriter handler = B2ContentFileWriter
                        .builder(backupFile)
                        .build();
                client.downloadById(file.getFileId(), handler);
                ZipUtil.unpack(backupFile, backupDir);
                if (!backupFile.delete()) {
                    throw new IOException("Failed to delete server file!");
                }
                for (File junk : backupDir.listFiles()) {
                    if (!junk.getName().equals("world")) {
                        if (junk.isFile()) {
                            if (!junk.delete()) {
                                throw new IOException("Failed to delete junk file: " + junk.getName());
                            }
                        } else {
                            FileUtils.deleteDirectory(junk);
                        }
                    }
                }
                ZipUtil.pack(backupDir, backupFile);
                UploadUtil.UploadResult result = UploadUtil.uploadFile(backupFile);
                if (result.isSuccess()) {
                    player.sendMessage(Language.getText(player, "upload.url", result.getUrl()));
                } else {
                    player.sendMessage(Language.getText(player, "upload.fail") + " " + result.getError() + " " + result.getMessage());
                }
                backupFile.delete();
                FileUtils.deleteDirectory(backupDir);
                processing.remove(player.getUniqueId());
            } catch (IOException | B2Exception e) {
                player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
            }
        }).start();
	}

    public boolean createBackup(String backupName, Player player) {

        GridFSBucket bucket = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_files");
        GridFSBucket uploadTo = GridFSBuckets.create(ServerDroplet.getInstance().getDatabase(), "server_backups");

        GridFSFindIterable find = bucket.find(Filters.eq("filename", info.getName() + ".zip"));

        GridFSFile file = find.first();

        if (file == null) {
            return false;
        }

        allowed.put(player.getUniqueId(), false);

        UUID plId = player.getUniqueId();

        if (!ServerDroplet.isBackblazeLoaded()) {
            Bukkit.getScheduler().runTaskAsynchronously(ServerDroplet.getInstance(), new Runnable() {

                @Override
                public void run() {

                    try {
                        GridFSDownloadStream download = bucket.openDownloadStream(file.getObjectId());

                        GridFSUploadStream upload = uploadTo.openUploadStream(file.getMD5() + ".zip", new GridFSUploadOptions().metadata(new Document("owner", player.getUniqueId().toString()).append("backup_name", backupName)));

                        IOUtils.copy(download, upload);

                        download.close();
                        upload.close();
                        Language.sendMessage(player, "backup.complete");
                    } catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
                    } finally {
                        allowed.put(player.getUniqueId(), true);

                        if (Bukkit.getPlayer(plId) != null) {
                            new BackupMenu(Bukkit.getPlayer(plId), info).open(Bukkit.getPlayer(plId));
                        }
                    }
                }
            });
        } else {
            new Thread(() -> {
                if (processing.contains(player.getUniqueId())) {
                    Language.sendMessage(player, "backup.processing");
                    return;
                }
                Language.sendMessage(player, "backup.please_wait");
                processing.add(player.getUniqueId());
                File tempFile;
                File tempDir;
                B2StorageClient client = Backblaze.getClient();
                try {
                    tempDir = new File("./tmp/");
                    if (!tempDir.exists()) {
                        tempDir.mkdir();
                    }
                    tempFile = new File("./tmp/" + file.getFilename());
                    if (!tempFile.exists()) {
                        tempFile.createNewFile();
                    }
                    bucket.downloadToStream(file.getObjectId(), new FileOutputStream(tempFile));
                    B2ContentSource source = B2FileContentSource.build(tempFile);
                    B2UploadFileRequest request = B2UploadFileRequest
                            .builder(BUCKETID, "server_backups/" + player.getUniqueId() + "/" + file.getMD5() + ".zip", B2ContentTypes.B2_AUTO, source)
                            .setCustomField("name", backupName)
                            .build();
                    client.uploadSmallFile(request);
                    tempFile.delete();
                    FileUtils.deleteDirectory(tempDir);
                    processing.remove(player.getUniqueId());
                    allowed.put(player.getUniqueId(), true);
                    if (Bukkit.getPlayer(plId) != null) {
                        new BackupMenu(Bukkit.getPlayer(plId), info).open(Bukkit.getPlayer(plId));
                    }
                    Language.sendMessage(player, "backup.complete");
                } catch (IOException | B2Exception e) {
                    e.printStackTrace();
                    player.sendMessage(Language.getText(player, "response_codes.server_unknown_error"));
                }
            }).start();
        }

        return true;
    }

    @Override
    public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {

        AnvilPacketMenu m = new AnvilPacketMenu();
        m.setDefaultText(Language.getText(player, "backup.name"));
        m.setResult(new ItemStack(Material.PAPER));
        m.setHandler(new AnvilPacketMenuHandler() {

            @Override
            public void onResult(String text, Player pl) {
                createBackup(text, player);
            }
        });

        m.open(player);

    }

}
