package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;
import me.jetby.treexclans.addons.service.ServiceManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Getter
public abstract class TreexAddon {

    private TreexAddonInfo info;
    private ServiceManager serviceManager;
    protected TreexClans plugin;
    protected File dataFolder;
    private Logger logger;

    public final void initialize(@NotNull AddonContext context) {
        this.info = getClass().getAnnotation(TreexAddonInfo.class);
        this.serviceManager = context.serviceManager();
        this.plugin = (TreexClans) context.serviceManager().getPlugin();
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
        this.dataFolder = serviceManager.getDataFolder();
    }

    public abstract void onEnable();
    public abstract void onDisable();

    public TreexClans getClansPlugin() {
        return plugin;
    }
}