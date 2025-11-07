package me.jetby.treexclans.addons.service;

import me.jetby.treexclans.addons.IAddonManager;
import me.jetby.treexclans.addons.configuration.ServiceConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public interface ServiceManager {

    JavaPlugin getPlugin();
    File getDataFolder();

    IAddonManager getAddonManager();
    ServiceConfiguration getServiceConfiguration();

}
