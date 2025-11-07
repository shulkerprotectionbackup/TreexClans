package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.addons.annotations.Dependency;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;
import me.jetby.treexclans.addons.util.VersionUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.LOGGER;

public final class AddonManager {

    private final TreexClans plugin;
    private final File addonsFolder;
    private final Logger logger;
    private final boolean debug;

    @Getter
    private final Map<String, TreexAddon> loadedAddons = new LinkedHashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new HashMap<>();

    public AddonManager(@NotNull TreexClans plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.addonsFolder = new File(plugin.getDataFolder(), "addons");
        this.debug = plugin.getCfg().isDebug();

        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
            LOGGER.success("Addons folder created at: " + addonsFolder.getAbsolutePath());
        }
    }

    /**
     * Загружает все JAR-аддоны из папки /addons.
     */
    public void loadAddons() {
        File[] jars = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            LOGGER.info("No addons found in " + addonsFolder.getAbsolutePath());
            return;
        }
        if (debug) {
            LOGGER.success("------------------------");
            LOGGER.info("Scanning " + jars.length + " addon(s) in folder: " + addonsFolder.getAbsolutePath());
            LOGGER.success("------------------------");
        }

        for (File jarFile : jars) {
            if (debug) LOGGER.info("→ Found addon file: " + jarFile.getName() + " (" + jarFile.length() + " bytes)");

            try {
                loadFromJar(jarFile);
            } catch (Throwable e) {
                LOGGER.error("❌ Failed to load addon from " + jarFile.getName() + ": " + e.getClass().getSimpleName() + " — " + e.getMessage());
                e.printStackTrace();
            }
        }

        enableAll();

        LOGGER.success(loadedAddons.size() + " addon(s) loaded successfully!");
    }

    /**
     * Загружает один JAR и ищет аннотированный класс TreexAddonInfo.
     */
    private void loadFromJar(File jarFile) throws Exception {
        if (debug) LOGGER.info("↳ Opening JAR: " + jarFile.getName());

        URLClassLoader loader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                plugin.getClass().getClassLoader()
        );

        classLoaders.put(jarFile.getName(), loader);

        List<Class<?>> classes;
        try (JarFile jar = new JarFile(jarFile)) {
            classes = jar.stream()
                    .filter(e -> e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace('/', '.').replace(".class", ""))
                    .map(name -> {
                        try {
                            Class<?> c = loader.loadClass(name);
                            if (debug) LOGGER.info("  ↳ Loaded class: " + name);
                            return c;
                        } catch (Throwable t) {
                            LOGGER.warn("  ⚠️  Failed to load class " + name + " (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if (debug) LOGGER.info("↳ Total classes scanned: " + classes.size());

        boolean foundAny = false;
        for (Class<?> clazz : classes) {
            TreexAddonInfo meta = clazz.getAnnotation(TreexAddonInfo.class);
            if (meta == null) {
                if (debug) LOGGER.info("  ⤷ Skipping " + clazz.getName() + " (no @TreexAddonInfo)");
                continue;
            }

            foundAny = true;
            if (debug) LOGGER.success("  ⤷ Found addon class: " + clazz.getName());
            if (debug) LOGGER.success("     ↳ id=" + meta.id() + ", version=" + meta.version());

            if (!TreexAddon.class.isAssignableFrom(clazz)) {
                if (debug) LOGGER.warn("  ⚠️  Class " + clazz.getName() + " has @TreexAddonInfo but does not extend TreexAddon!");
                continue;
            }

            TreexAddon addon = (TreexAddon) clazz.getDeclaredConstructor().newInstance();
            addon.initialize(new AddonContext(plugin, logger, addonsFolder, loadedAddons::get));

            loadedAddons.put(meta.id(), addon);
            if (debug) LOGGER.success("✅ Registered addon: " + meta.id() + " v" + meta.version());
        }

        if (!foundAny) {
            if (debug) LOGGER.warn("⚠️  No classes with @TreexAddonInfo found in " + jarFile.getName());
        }
    }

    private void enableAll() {
        List<TreexAddon> ordered = sortByDependencies();
        if (debug) LOGGER.info("↳ Enabling addons in dependency order (" + ordered.size() + " total)");

        for (TreexAddon addon : ordered) {
            TreexAddonInfo info = addon.getInfo();

            if (!checkDependencies(info)) {
                if (debug) LOGGER.error("⛔ Skipping " + info.id() + " — missing or incompatible dependencies.");
                continue;
            }

            try {
                addon.onEnable();
                LOGGER.success("✅ Enabled addon: " + info.id() + " v" + info.version());
            } catch (Throwable e) {
                LOGGER.error("❌ Exception while enabling " + info.id() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void unloadAll() {
        LOGGER.info("↳ Unloading all addons (" + loadedAddons.size() + ")");
        List<TreexAddon> reversed = new ArrayList<>(loadedAddons.values());
        Collections.reverse(reversed);

        for (TreexAddon addon : reversed) {
            try {
                addon.onDisable();
                LOGGER.info("🟡 Disabled addon: " + addon.getInfo().id());
            } catch (Throwable e) {
                LOGGER.error("❌ Error disabling " + addon.getInfo().id() + ": " + e.getMessage());
            }
        }

        loadedAddons.clear();
        for (Map.Entry<String, URLClassLoader> entry : classLoaders.entrySet()) {
            try {
                entry.getValue().close();
                if (debug) LOGGER.info("Closed classloader for " + entry.getKey());
            } catch (IOException ignored) {}
        }
        classLoaders.clear();
    }

    private boolean checkDependencies(TreexAddonInfo info) {
        boolean ok = true;
        for (Dependency dep : info.depends()) {
            TreexAddon found = loadedAddons.get(dep.id());
            if (found == null) {
                LOGGER.error("❌ Missing dependency for " + info.id() + ": " + dep.id() + " (required ≥ " + dep.version() + ")");
                ok = false;
                continue;
            }

            String actual = found.getInfo().version();
            if (!VersionUtil.isSatisfied(actual, dep.version())) {
                LOGGER.error("❌ Incompatible dependency for " + info.id() + ": "
                        + dep.id() + " (required ≥ " + dep.version() + ", found " + actual + ")");
                ok = false;
            }
        }
        return ok;
    }

    private List<TreexAddon> sortByDependencies() {
        Map<String, Set<String>> graph = new HashMap<>();
        for (TreexAddon a : loadedAddons.values())
            graph.put(a.getInfo().id(), new LinkedHashSet<>());

        for (TreexAddon a : loadedAddons.values()) {
            TreexAddonInfo info = a.getInfo();

            for (Dependency d : info.depends())
                if (graph.containsKey(d.id())) graph.get(info.id()).add(d.id());

            for (Dependency d : info.softDepends())
                if (graph.containsKey(d.id())) graph.get(info.id()).add(d.id());

            for (String after : info.loadAfter())
                if (graph.containsKey(after)) graph.get(info.id()).add(after);

            for (String before : info.loadBefore())
                if (graph.containsKey(before)) graph.get(before).add(info.id());
        }

        if (debug) LOGGER.info("↳ Built dependency graph: " + graph);
        return topologicalSort(graph);
    }

    private List<TreexAddon> topologicalSort(Map<String, Set<String>> graph) {
        Map<String, Integer> indeg = new HashMap<>();
        for (String k : graph.keySet()) indeg.put(k, 0);
        for (Set<String> v : graph.values())
            for (String d : v) indeg.put(d, indeg.getOrDefault(d, 0) + 1);

        Deque<String> q = new ArrayDeque<>();
        indeg.forEach((k, v) -> { if (v == 0) q.add(k); });

        List<TreexAddon> result = new ArrayList<>();
        while (!q.isEmpty()) {
            String id = q.removeFirst();
            TreexAddon addon = loadedAddons.get(id);
            if (addon != null) result.add(addon);

            for (String to : graph.getOrDefault(id, Collections.emptySet())) {
                indeg.put(to, indeg.get(to) - 1);
                if (indeg.get(to) == 0) q.addLast(to);
            }
        }

        if (debug) LOGGER.info("↳ Topological order: " + result.stream().map(a -> a.getInfo().id()).toList());
        return result;
    }
}
