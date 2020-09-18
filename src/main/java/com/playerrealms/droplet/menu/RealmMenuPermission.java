package com.playerrealms.droplet.menu;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RealmMenuPermission {
    private List<String> allowed;
    private FileConfiguration config;
    private SaveMethod saveMethod;

    public RealmMenuPermission(FileConfiguration config, SaveMethod saveMethod) {
        allowed = new ArrayList<>();
        this.config = config;
        this.saveMethod = saveMethod;
        load();
    }

    public void load(){
        List<String> list = config.getStringList("uuids");
        for (String key : list) {
            allowed.add(key);
        }
    }

    public void save() {
        config.set("uuids", allowed);
        saveMethod.save(config);
    }

    public List<String> getAllowed() { return allowed; }

    public void addUUID(String uuid){
        allowed.add(uuid);
        save();
    }

    public void removeUUID(String uuid){
        allowed.remove(uuid);
        save();
    }

    public boolean checkUUID(String uuid){
        return allowed.contains(uuid);
    }

    public interface SaveMethod {
        /**
         * Save this file configuration
         * @param config the config to save
         */
        void save(FileConfiguration config);
    }
}
