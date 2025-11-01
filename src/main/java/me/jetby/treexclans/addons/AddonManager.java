package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class AddonManager {

    private final TreexClans plugin;
    private final File addonsFolder;
    @Getter
    private final Map<String, TreexAddon> loadedAddons = new HashMap<>();

    public AddonManager(TreexClans plugin) {
        this.plugin = plugin;
        this.addonsFolder = new File(plugin.getDataFolder(), "addons");

        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
            LOGGER.success("Addons folder created at: " + addonsFolder.getAbsolutePath());
        }
    }

    public void loadAddons() {
        if (!addonsFolder.exists() || !addonsFolder.isDirectory()) {
            LOGGER.warn("Addons folder not found!");
            return;
        }

        File[] jarFiles = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            LOGGER.info("No addons found in addons folder");
            return;
        }

        LOGGER.success("------------------------");
        LOGGER.info("Loading " + jarFiles.length + " addon(s)...");
        LOGGER.success("------------------------");

        for (File jarFile : jarFiles) {
            try {
                loadAddon(jarFile);
            } catch (Exception e) {
                LOGGER.error("Failed to load addon: " + jarFile.getName());
                e.printStackTrace();
            }
        }

        LOGGER.success("------------------------");
        LOGGER.success(loadedAddons.size() + " addon(s) loaded successfully!");
        LOGGER.success("------------------------");
    }

    private void loadAddon(File jarFile) throws Exception {
        String jarName = jarFile.getName();
        String addonName = extractAddonName(jarName);

        File configFolder = new File(addonsFolder, addonName);

        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File addonYamlFile = new File(configFolder, "addon.yml");

        if (!addonYamlFile.exists()) {
            LOGGER.warn("addon.yml not found in: " + addonName);
            return;
        }

        YamlConfiguration addonYaml = YamlConfiguration.loadConfiguration(addonYamlFile);

        String mainClassName = addonYaml.getString("main");

        if (mainClassName == null || mainClassName.isEmpty()) {
            LOGGER.warn("No main class specified in " + addonName + "/addon.yml");
            return;
        }

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                Thread.currentThread().getContextClassLoader()
        );

        try {
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            if (!TreexAddon.class.isAssignableFrom(mainClass)) {
                LOGGER.warn("Main class " + mainClassName + " does not extend TreexAddon!");
                classLoader.close();
                return;
            }

            TreexAddon addon = (TreexAddon) mainClass.getDeclaredConstructor().newInstance();

            addon.initialize(plugin, configFolder);
            addon.onEnable();

            loadedAddons.put(addon.getName(), addon);
            LOGGER.success("Addon loaded: " + addon.getName() + " v" + addon.getVersion() + " by " + addon.getAuthor());

        } catch (ClassNotFoundException e) {
            LOGGER.error("Main class not found: " + mainClassName);
            classLoader.close();
        }
    }

    private String extractAddonName(String jarName) {
        String name = jarName.replace(".jar", "");

        int versionIndex = -1;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '-' && i + 1 < name.length() && Character.isDigit(name.charAt(i + 1))) {
                versionIndex = i;
                break;
            }
        }
        if (versionIndex != -1) {
            return name.substring(0, versionIndex);
        }

        return name;
    }

    public void unloadAllAddons() {
        for (TreexAddon addon : loadedAddons.values()) {
            try {
                addon.onDisable();
                LOGGER.info("Addon disabled: " + addon.getName());
            } catch (Exception e) {
                LOGGER.error("Error disabling addon: " + addon.getName());
                e.printStackTrace();
            }
        }
        loadedAddons.clear();
    }

    public TreexAddon getAddon(String name) {
        return loadedAddons.get(name);
    }
    public boolean isAddonLoaded(String name) {
        return loadedAddons.containsKey(name);
    }
    public List<TreexAddon> getLoadedAddons() {
        return new ArrayList<>(loadedAddons.values());
    }
}