package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class AddonManager {

    private final TreexClans plugin;
    private final File addonsFolder;
    @Getter
    private final Map<String, TreexAddon> loadedAddons = new HashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new HashMap<>();

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

        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry yamlEntry = jar.getJarEntry("addon.yml");
            if (yamlEntry != null) {
                try (InputStream input = jar.getInputStream(yamlEntry)) {
                    Files.copy(input, addonYamlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                LOGGER.info("Extracted addon.yml from JAR: " + jarName);
            } else {
                if (!addonYamlFile.exists()) {
                    createDefaultAddonYaml(addonYamlFile, addonName, jarName);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check/extract addon.yml from JAR: " + jarName + " - " + e.getMessage());

            if (!addonYamlFile.exists()) {
                createDefaultAddonYaml(addonYamlFile, addonName, jarName);
            }
        }

        YamlConfiguration addonYaml = YamlConfiguration.loadConfiguration(addonYamlFile);

        String mainClassName = addonYaml.getString("main");
        if (mainClassName == null || mainClassName.isEmpty()) {
            LOGGER.warn("No main class specified in " + addonName + "/addon.yml");
            return;
        }

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                plugin.getClass().getClassLoader()
        );

        classLoaders.put(addonName, classLoader);

        try {
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            Class<?> treexAddonClass = TreexAddon.class;

            if (!treexAddonClass.isAssignableFrom(mainClass)) {
                LOGGER.warn("Main class " + mainClassName + " does not extend TreexAddon!");
                classLoader.close();
                classLoaders.remove(addonName);
                return;
            }

            TreexAddon addon = (TreexAddon) mainClass.getDeclaredConstructor().newInstance();
            addon.initialize(plugin, configFolder,
                    addonYaml.getString("name", "Untitled"),
                    addonYaml.getString("author", "Unknown"),
                    addonYaml.getString("version", "1.0"),
                    addonYaml.getString("description", "")
            );
            addon.onEnable();

            loadedAddons.put(addon.getName(), addon);
            LOGGER.success("Addon loaded: " + addon.getName() + " v" + addon.getVersion() + " by " + addon.getAuthor());

        } catch (ClassNotFoundException e) {
            LOGGER.error("Main class not found: " + mainClassName);
            classLoader.close();
            classLoaders.remove(addonName);
        } catch (Exception e) {
            LOGGER.error("Error loading addon: " + addonName);
            e.printStackTrace();
            classLoader.close();
            classLoaders.remove(addonName);
        }
    }

    private void createDefaultAddonYaml(File addonYamlFile, String addonName, String jarName) {
        try {
            addonYamlFile.createNewFile();

            YamlConfiguration config = new YamlConfiguration();
            config.set("main", "com.example." + addonName.toLowerCase() + "." + addonName);
            config.set("name", addonName);
            config.set("version", extractVersion(jarName));
            config.set("author", "Unknown");
            config.set("description", "");

            config.save(addonYamlFile);

            LOGGER.info("Created default addon.yml for: " + addonName);
        } catch (IOException e) {
            LOGGER.error("Failed to create addon.yml: " + addonName);
            e.printStackTrace();
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

    private String extractVersion(String jarName) {
        String name = jarName.replace(".jar", "");

        int versionIndex = -1;
        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == '-' && i + 1 < name.length() && Character.isDigit(name.charAt(i + 1))) {
                versionIndex = i;
                break;
            }
        }

        if (versionIndex != -1) {
            return name.substring(versionIndex + 1);
        }

        return "1.0";
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

        for (URLClassLoader classLoader : classLoaders.values()) {
            try {
                classLoader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        classLoaders.clear();
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