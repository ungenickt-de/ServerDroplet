package com.playerrealms.droplet.menu;

import com.nirvana.menu.*;
import com.playerrealms.droplet.DropletAPI;
import com.playerrealms.droplet.lang.Language;
import com.playerrealms.droplet.util.TextFileUploadUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MassiveCoreEditMenu extends ChestPacketMenu {

    private File rootFolder;

    private String path;

    private boolean transfer;

    private UUID viewer;

    private Boolean dangermode = Boolean.parseBoolean(DropletAPI.getThisServer().getMetadata().getOrDefault("danger", "false"));

    public MassiveCoreEditMenu(File rootFolder, String path, Player player) {
        super(45, rootFolder.getName()+"/"+path);
        this.rootFolder = rootFolder;
        this.path = path;
        transfer = false;
        viewer = player.getUniqueId();
        initialize(player);
    }

    public void openMenu(Player player) {
        MassiveCoreEditMenu folderBrowser = new MassiveCoreEditMenu(new File("mstore"), player);
        folderBrowser.open(player);
    }

    @Override
    public void close() {
        super.close();
        if(!transfer) {
            Player pl = Bukkit.getPlayer(viewer);
            if(pl != null) {
                new RealmManagerMenu(pl).open(pl);
            }
        }
    }

    public MassiveCoreEditMenu(File rootFolder, Player player) {
        super(45, rootFolder.getName());
        this.rootFolder = rootFolder;
        path = "";
        initialize(player);
    }

    public void initialize(Player player) {
        if(!path.isEmpty()) {
            addItem(new Item(Material.ENDER_CHEST).setTitle(ChatColor.RED+"...").build(), new PacketMenuSlotHandler() {
                @Override
                public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                    clearInventory();
                    path = "";
                    initialize(player);
                }
            });
            addItem(new Item(Material.BOOK_AND_QUILL).setTitle(ChatColor.GREEN+Language.getText(player, "file_browser.create")).build(), new PacketMenuSlotHandler() {
                @Override
                public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                    AnvilPacketMenu ap = new AnvilPacketMenu();
                    ap.setDefaultText(".json");
                    ap.setResult(new ItemStack(Material.BOOK_AND_QUILL, 1));
                    ap.setHandler(new AnvilPacketMenuHandler() {
                        @Override
                        public void onResult(String text, Player pl) {
                            if(!text.endsWith(".json")) {
                                text = text + ".json";
                            }
                            if(text.contains("/")) {
                                pl.sendMessage("Invalid file name");
                                return;
                            }
                            File newFile = new File(getPath(), text);
                            try {
                                newFile.createNewFile();
                                MassiveCoreEditMenu fbm = new MassiveCoreEditMenu(rootFolder, path, player);
                                fbm.open(pl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    ap.open(player);
                }
            });
        }

        for(File dir : getDirectories()) {
            Item def = new Item(Material.CHEST).setTitle(ChatColor.YELLOW+dir.getName());
            if(dangermode){
                def.setLore(Language.getText(player, "menu.config_edit.delete"));
            }
            addItem(def.build(), new PacketMenuSlotHandler() {
                @Override
                public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                    if(interactionInfo.getClickType() == ClickType.LEFT) {
                        clearInventory();
                        updateItemsForViewers();
                        path += dir.getName() + "/";
                        initialize(player);
                    }else if(dangermode && interactionInfo.getClickType() == ClickType.SHIFT_RIGHT){
                        menu.close();
                        new FolderDeleteMenu(dir, 3, player).open(player);
                    }
                }
            });
        }

        for(File file : getFiles()) {
            Item paper = new Item(Material.PAPER).setTitle(ChatColor.GREEN+file.getName()).setLore(Language.getText(player, "menu.config_edit.upload"), Language.getText(player, "menu.config_edit.download"));
            if(dangermode){
                paper = new Item(Material.PAPER).setTitle(ChatColor.GREEN+file.getName()).setLore(Language.getText(player, "menu.config_edit.upload"), Language.getText(player, "menu.config_edit.download"), Language.getText(player, "menu.config_edit.delete"));
            }
            addItem(paper.build(), new PacketMenuSlotHandler() {
                @Override
                public void onClicked(Player player, PacketMenu menu, Interaction interactionInfo) {
                    if(interactionInfo.getClickType() == ClickType.SHIFT_LEFT){
                        menu.close();
                        try {
                            String fileData = FileUtils.readFileToString(file, "UTF-8");
                            String response = TextFileUploadUtil.uploadText(file.getName(), fileData);
                            Language.sendMessage(player, "config_edit.upload_success", response);
                        } catch (Exception e) {
                            Language.sendMessage(player, "config_edit.unknown_error", e.getMessage());
                        }
                    }else if(interactionInfo.getClickType() == ClickType.LEFT){
                        menu.close();
                        AnvilPacketMenu anvil = new AnvilPacketMenu();
                        anvil.setDefaultText("Pastebin");
                        anvil.setResult(new Item(Material.ANVIL).build());
                        anvil.setHandler(new AnvilPacketMenuHandler() {
                            @Override
                            public void onResult(String text, Player player) {
                                Language.sendMessage(player, "config_edit.uploading");

                                try{
                                    TextFileUploadUtil.checkLegalDownloadURL(text);

                                    String[] splits = text.split("/");

                                    if(splits.length == 0){
                                        throw new MalformedURLException();
                                    }

                                    String correctedUrl;
                                    if(text.contains("pastebin.com")) {
                                        correctedUrl = "https://pastebin.com/raw/" + splits[splits.length - 1];
                                    }else if(text.contains("paste.mcua.net")){
                                        String temp;
                                        if(text.contains("paste.mcua.net/raw/")){
                                            temp = text.split("paste.mcua.net/raw/")[1];
                                        }else if(text.contains("paste.mcua.net/v/")){
                                            temp = text.split("paste.mcua.net/v/")[1];
                                        }else{
                                            throw new MalformedURLException();
                                        }
                                        correctedUrl = "https://paste.mcua.net/raw/" + temp;
                                    }else{
                                        throw new MalformedURLException();
                                    }

                                    try{
                                        String content = TextFileUploadUtil.downloadText(correctedUrl);
                                        FileUtils.writeStringToFile(file, content, "UTF-8");
                                        Language.sendMessage(player, "config_edit.retrieve_success");
                                    }catch(IOException e){
                                        Language.sendMessage(player, "config_edit.fail_up");
                                        e.printStackTrace();
                                    }

                                }catch(MalformedURLException e){
                                    Language.sendMessage(player, "config_edit.bad_url");
                                }
                            }
                        });
                        anvil.open(player);
                    }else if(dangermode && interactionInfo.getClickType() == ClickType.SHIFT_RIGHT){
                        file.delete();
                        player.sendMessage(ChatColor.GREEN+"File deleted.");
                        openMenu(player);
                    }
                }
            });
        }
    }

    public File getPath() {
        if(path.isEmpty()) {
            return rootFolder;
        }
        return new File(rootFolder, path);
    }

    public List<File> getDirectories() {
        List<File> files = new ArrayList<>();
        for(File file : getPath().listFiles()) {
            if(file.isDirectory()) {
                File next = new File(file, "BLOCKED");
                if(!next.exists()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public List<File> getFiles() {
        List<File> files = new ArrayList<>();
        for(File file : getPath().listFiles()) {
            if(!file.getName().endsWith(".json")) {
                continue;
            }
            if(!file.isDirectory()) {
                files.add(file);
            }
        }
        return files;
    }

    boolean isJSONValid(String json){
        try{
            new JSONObject(json);
        }catch (JSONException e){
            try{
                new JSONArray(json);
            }catch (JSONException ex){
                return false;
            }
        }
        return true;
    }
}
