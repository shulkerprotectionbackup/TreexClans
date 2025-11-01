package me.jetby.treexclans.addons;

import me.jetby.treexclans.TreexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;


public abstract class TreexAddon {

    protected TreexClans plugin;
    protected File dataFolder;

    public final void initialize(@NotNull TreexClans plugin, @NotNull File dataFolder) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;

        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }


    public abstract String getName();


    public abstract String getVersion();


    public String getAuthor() {
        return "Unknown";
    }


    public String getDescription() {
        return "No description provided";
    }


    public abstract void onEnable();


    public abstract void onDisable();


    public TreexClans getPlugin() {
        return plugin;
    }

    public File getDataFolder() {
        return dataFolder;
    }


    public FileConfiguration getConfig(@NotNull String fileName) {
        File configFile = new File(dataFolder, fileName);

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create config file: " + fileName);
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }


    public void saveConfig(@NotNull String fileName, @NotNull FileConfiguration config) {
        File configFile = new File(dataFolder, fileName);

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config file: " + fileName);
            e.printStackTrace();
        }
    }

    public File createFile(@NotNull String fileName) throws IOException {
        File file = new File(dataFolder, fileName);

        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }


    public File createFolder(@NotNull String folderName) {
        File folder = new File(dataFolder, folderName);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }


    public File getFile(@NotNull String path) {
        return new File(dataFolder, path);
    }
}