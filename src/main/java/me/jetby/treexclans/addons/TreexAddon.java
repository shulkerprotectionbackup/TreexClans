package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@Getter
public abstract class TreexAddon {

    protected TreexClans plugin;
    protected File dataFolder;
    protected String name;
    protected String author;
    protected String version;
    protected String description;

    public final void initialize(
            @NotNull TreexClans plugin,
            @NotNull File dataFolder,
            String name,
            String author,
            String version,
            String description
    ) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;

        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    public abstract void onEnable();


    public abstract void onDisable();


    public TreexClans getClansPlugin() {
        return plugin;
    }

    public FileConfiguration getConfig() {
        File configFile = new File(dataFolder, "config.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create config file: config.yml");
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }


    public void saveConfig() {
        File configFile = new File(dataFolder, "config.yml");

        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config file: config.yml");
            e.printStackTrace();
        }
    }
}