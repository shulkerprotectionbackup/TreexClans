package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class AddonManager {

    private final TreexClans plugin;
    private final File addonsFolder;
    private final File addonDataFolder;
    @Getter
    private final Map<String, TreexAddon> loadedAddons = new HashMap<>();

    public AddonManager(TreexClans plugin) {
        this.plugin = plugin;
        this.addonsFolder = new File(plugin.getDataFolder(), "addons");
        this.addonDataFolder = new File(plugin.getDataFolder(), "addon-data");

        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
            LOGGER.success("Addons folder created at: " + addonsFolder.getAbsolutePath());
        }

        if (!addonDataFolder.exists()) {
            addonDataFolder.mkdirs();
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
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                Thread.currentThread().getContextClassLoader()
        );

        String mainClassName = getMainClassName(jarFile);

        if (mainClassName == null || mainClassName.isEmpty()) {
            LOGGER.warn("No main class found in addon: " + jarFile.getName());
            classLoader.close();
            return;
        }

        try {
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            if (!TreexAddon.class.isAssignableFrom(mainClass)) {
                LOGGER.warn("Main class " + mainClassName + " does not extend TreexAddon!");
                classLoader.close();
                return;
            }

            TreexAddon addon = (TreexAddon) mainClass.getDeclaredConstructor().newInstance();

            String addonName = jarFile.getName().replace(".jar", "");
            File addonData = new File(addonDataFolder, addonName);

            if (!addonData.exists()) {
                addonData.mkdirs();
            }

            addon.initialize(plugin, addonData);
            addon.onEnable();

            loadedAddons.put(addon.getName(), addon);
            LOGGER.success("Addon loaded: " + addon.getName() + " v" + addon.getVersion());

        } catch (ClassNotFoundException e) {
            LOGGER.error("Main class not found: " + mainClassName);
            classLoader.close();
        }
    }


    private String getMainClassName(File jarFile) throws Exception {
        JarFile jar = new JarFile(jarFile);
        Manifest manifest = jar.getManifest();
        jar.close();

        if (manifest == null) {
            return null;
        }

        return manifest.getMainAttributes().getValue("Main-Class");
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


    public File getAddonDataFolder(String addonName) {
        return new File(addonDataFolder, addonName);
    }
}