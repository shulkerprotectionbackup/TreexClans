package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Getter
public abstract class TreexAddon {

    private TreexAddonInfo info;
    protected TreexClans plugin;
    protected File dataFolder;
    private Logger logger;

    public final void initialize(@NotNull AddonContext context) {
        this.info = getClass().getAnnotation(TreexAddonInfo.class);
        this.plugin = context.plugin();
        if (info == null)
            throw new IllegalStateException("Класс " + getClass().getName() + " не имеет аннотации @TreexAddonInfo");

        Logger parent = plugin.getLogger();
        this.logger = new Logger("AddonLogger-" + info.id(), null) {
            @Override
            public void log(java.util.logging.Level level, String msg) {
                String prefix = "[" + info.id() + "] ";
                parent.log(level, prefix + msg);
            }

            @Override
            public void log(java.util.logging.Level level, String msg, Throwable thrown) {
                String prefix = "[" + info.id() + "] ";
                parent.log(level, prefix + msg, thrown);
            }
        };
        this.dataFolder = new File(context.plugin().getDataFolder(), "addons/" + info.id());
        if (!dataFolder.exists()) dataFolder.mkdirs();
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