package me.jetby.treexclans.addons.service;

import lombok.Getter;
import me.jetby.treexclans.addons.IAddonManager;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;
import me.jetby.treexclans.addons.configuration.ServiceConfiguration;
import me.jetby.treexclans.addons.configuration.ServiceConfigurationImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public class ServiceManagerImpl implements ServiceManager {

    private final JavaPlugin plugin;
    private final File dataFolder;

    private final IAddonManager addonManager;
    private final ServiceConfiguration serviceConfiguration;
    private final TreexAddonInfo addon;

    public ServiceManagerImpl(IAddonManager addonManager, File dataFolder, JavaPlugin plugin, TreexAddonInfo addon) {
        this.addonManager = addonManager;
        this.dataFolder = new File(dataFolder, addon.id());
        this.plugin = plugin;
        this.serviceConfiguration = new ServiceConfigurationImpl(this);
        this.addon = addon;
    }
}
