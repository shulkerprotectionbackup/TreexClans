package me.jetby.treexclans.addons;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.addons.annotations.Dependency;
import me.jetby.treexclans.addons.annotations.TreexAddonInfo;
import me.jetby.treexclans.addons.service.ServiceManagerImpl;
import me.jetby.treexclans.addons.util.VersionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.LOGGER;

/**
 * Менеджер аддонов для TreexClans.
 *
 * Этот класс отвечает за:
 * - Сканирование и загрузку JAR-файлов аддонов из папки /addons.
 * - Автоматическое обнаружение классов с аннотацией @TreexAddonInfo.
 * - Проверку зависимостей (hard/soft, версии, loadAfter/loadBefore).
 * - Топологическую сортировку для порядка загрузки/выгрузки.
 * - Включение/выключение аддонов (с обработкой ошибок).
 * - Индивидуальную загрузку/выгрузку аддонов (с проверкой зависимостей).
 *
 * <h3>Как использовать:</h3>
 * <ul>
 *   <li>В основном плагине: <code>new AddonManager(plugin).loadAddons();</code> для загрузки всех.</li>
 *   <li>Индивидуально: <code>manager.loadAddon(new File("path/to/addon.jar"));</code></li>
 *   <li>Получить аддон: <code>TreexAddon addon = manager.getAddon("myaddon");</code></li>
 *   <li>Выгрузить: <code>manager.unloadAddon("myaddon");</code> (авто-выгрузит зависимости, если возможно).</li>
 * </ul>
 *
 * <p><b>Зависимости:</b> Аддоны могут объявлять @Dependency (hard/soft), loadAfter/loadBefore в @TreexAddonInfo.
 * Топосорт использует граф зависимостей для избежания циклических ошибок.</p>
 *
 * <p><b>Жизненный цикл:</b> Загрузка → Инициализация (initialize) → Включение (onEnable) → Выключение (onDisable) → Выгрузка (unload).</p>
 *
 * @see TreexAddon
 * @see TreexAddonInfo
 * @see Dependency
 */
public final class AddonManager implements IAddonManager {

    private final TreexClans plugin;
    private final File addonsFolder;
    private final Logger logger; // Используем стандартный Logger для детального логирования

    @Getter
    private final Map<String, TreexAddon> loadedAddons = new LinkedHashMap<>(); // LinkedHashMap для порядка загрузки
    private final Map<String, URLClassLoader> classLoaders = new HashMap<>(); // Ключ: имя JAR
    private final Map<String, File> jarFiles = new HashMap<>(); // Ключ: ID аддона → JAR файл (для перезагрузки)

    public AddonManager(@NotNull TreexClans plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.addonsFolder = new File(plugin.getDataFolder(), "addons");

        if (!addonsFolder.exists()) {
            addonsFolder.mkdirs();
            LOGGER.success("Addons folder created at: " + addonsFolder.getAbsolutePath());
            logger.info("Initialized AddonManager with folder: " + addonsFolder.getAbsolutePath());
        }
    }

    /**
     * Загружает все JAR-аддоны из папки /addons.
     * Сканирует, инициализирует, проверяет зависимости, сортирует и включает.
     *
     * @see #loadAddon(File)
     */
    public void loadAddons() {
        File[] jars = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logger.info("No addons found in " + addonsFolder.getAbsolutePath());
            LOGGER.info("No addons found in " + addonsFolder.getAbsolutePath());
            return;
        }

        LOGGER.success("------------------------");
        LOGGER.info("Scanning " + jars.length + " addon(s) in folder: " + addonsFolder.getAbsolutePath());
        logger.info("Starting bulk load of " + jars.length + " JAR files");
        LOGGER.success("------------------------");

        List<Throwable> errors = new ArrayList<>();
        for (File jarFile : jars) {
            try {
                loadAddon(jarFile);
            } catch (Throwable e) {
                String msg = "Failed to load addon from " + jarFile.getName() + ": " + e.getClass().getSimpleName() + " — " + e.getMessage();
                LOGGER.error("❌ " + msg);
                logger.log(Level.SEVERE, msg, e);
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            logger.warning("Bulk load completed with " + errors.size() + " errors");
        }

        enableAll();

        LOGGER.success("------------------------");
        LOGGER.success(loadedAddons.size() + " addon(s) loaded and enabled successfully!");
        LOGGER.success("------------------------");
        logger.info("Bulk load finished: " + loadedAddons.size() + " addons active");
    }

    /**
     * Загружает один JAR-аддон.
     * Сканирует классы, находит @TreexAddonInfo, инициализирует и добавляет в loadedAddons (но не включает!).
     * Если ID уже загружен — выбрасывает IllegalStateException.
     *
     * @param jarFile Путь к JAR-файлу.
     * @return ID загруженного аддона (или null, если ничего не найдено).
     * @throws IllegalStateException Если аддон с таким ID уже загружен.
     * @throws Exception Если ошибка сканирования/загрузки.
     * @see #enableAddon(String)
     */
    @Nullable
    public String loadAddon(@NotNull File jarFile) throws Exception {
        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Invalid JAR file: " + jarFile.getAbsolutePath());
        }

        logger.info("Loading single addon from JAR: " + jarFile.getName());
        LOGGER.info("→ Loading addon file: " + jarFile.getName() + " (" + jarFile.length() + " bytes)");

        URLClassLoader loader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                plugin.getClass().getClassLoader()
        );
        classLoaders.put(jarFile.getName(), loader);

        List<Class<?>> classes = scanClassesInJar(jarFile, loader);

        logger.info("Scanned " + classes.size() + " classes in " + jarFile.getName());

        String loadedId = null;
        boolean foundAny = false;
        for (Class<?> clazz : classes) {
            TreexAddonInfo meta = clazz.getAnnotation(TreexAddonInfo.class);
            if (meta == null) {
                logger.fine("Skipping class " + clazz.getName() + " (no @TreexAddonInfo)");
                continue;
            }

            foundAny = true;
            logger.info("Found addon class: " + clazz.getName() + " (id=" + meta.id() + ", version=" + meta.version() + ")");

            if (!TreexAddon.class.isAssignableFrom(clazz)) {
                logger.warning("Class " + clazz.getName() + " has @TreexAddonInfo but does not implement/extend TreexAddon!");
                LOGGER.warn("  ⚠️  Class " + clazz.getName() + " has @TreexAddonInfo but does not extend TreexAddon!");
                continue;
            }

            if (loadedAddons.containsKey(meta.id())) {
                throw new IllegalStateException("Addon with ID '" + meta.id() + "' is already loaded!");
            }

            TreexAddon addon = (TreexAddon) clazz.getDeclaredConstructor().newInstance();
            addon.initialize(
                new AddonContext(
                    new ServiceManagerImpl(this, addonsFolder, plugin, addon.getClass().getAnnotation(TreexAddonInfo.class)),
                    logger
                )
            );

            loadedAddons.put(meta.id(), addon);
            jarFiles.put(meta.id(), jarFile);
            loadedId = meta.id();
            LOGGER.success("✅ Loaded addon: " + meta.id() + " v" + meta.version());
            logger.info("Successfully loaded addon: " + meta.id());
        }

        if (!foundAny) {
            logger.warning("No classes with @TreexAddonInfo found in " + jarFile.getName());
            LOGGER.warn("⚠️  No classes with @TreexAddonInfo found in " + jarFile.getName());
            classLoaders.remove(jarFile.getName()); // Cleanup
            return null;
        }

        return loadedId;
    }

    /**
     * Включает все загруженные аддоны (с сортировкой и проверкой зависимостей).
     *
     * @see #enableAddon(String)
     */
    public void enableAll() {
        List<TreexAddon> ordered = sortByDependencies();
        logger.info("Enabling " + ordered.size() + " addons in dependency order");

        LOGGER.info("↳ Enabling addons in dependency order (" + ordered.size() + " total)");
        for (TreexAddon addon : ordered) {
            enableAddon(addon.getInfo().id());
        }
    }

    /**
     * Включает конкретный аддон (проверяет зависимости, вызывает onEnable).
     * Если зависимости не удовлетворены — логирует ошибку и пропускает.
     *
     * @param addonId ID аддона.
     * @return true, если успешно включен.
     */
    public boolean enableAddon(@NotNull String addonId) {
        TreexAddon addon = loadedAddons.get(addonId);
        if (addon == null) {
            logger.warning("Cannot enable unknown addon: " + addonId);
            return false;
        }

        TreexAddonInfo info = addon.getInfo();
        if (!checkDependencies(info)) {
            LOGGER.error("⛔ Skipping " + info.id() + " — missing or incompatible dependencies.");
            return false;
        }

        try {
            addon.onEnable();
            LOGGER.success("✅ Enabled addon: " + info.id() + " v" + info.version());
            logger.info("Enabled addon: " + info.id());
            return true;
        } catch (Throwable e) {
            String msg = "Exception while enabling " + info.id() + ": " + e.getMessage();
            LOGGER.error("❌ " + msg);
            logger.log(Level.SEVERE, msg, e);
            return false;
        }
    }

    /**
     * Выгружает все аддоны (выключает в обратном порядке, закрывает classloaders).
     *
     * @see #unloadAddon(String)
     */
    public void unloadAll() {
        logger.info("Unloading all addons (" + loadedAddons.size() + ")");
        LOGGER.info("↳ Unloading all addons (" + loadedAddons.size() + ")");

        List<TreexAddon> reversed = new ArrayList<>(loadedAddons.values());
        Collections.reverse(reversed);

        for (TreexAddon addon : reversed) {
            unloadAddon(addon.getInfo().id());
        }

        loadedAddons.clear();
        jarFiles.clear();
        for (Map.Entry<String, URLClassLoader> entry : classLoaders.entrySet()) {
            try {
                entry.getValue().close();
                logger.fine("Closed classloader for " + entry.getKey());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to close classloader for " + entry.getKey(), e);
            }
        }
        classLoaders.clear();

        logger.info("All addons unloaded");
    }

    @Override
    public boolean disable(TreexAddon addon) {
        return unloadAddon(addon.getInfo().id());
    }

    /**
     * Выгружает конкретный аддон (вызывает onDisable, удаляет из loadedAddons, закрывает loader).
     * Проверяет, нет ли других аддонов, зависящих от него (soft/hard). Если есть — логирует предупреждение, но выгружает (риск на пользователе).
     * Автоматически выгружает рекурсивно "дочерние" аддоны (зависимые от него), если возможно.
     *
     * @param addonId ID аддона.
     * @return true, если успешно выгружен.
     */
    public boolean unloadAddon(@NotNull String addonId) {
        TreexAddon addon = loadedAddons.get(addonId);
        if (addon == null) {
            logger.warning("Cannot unload unknown addon: " + addonId);
            return false;
        }

        // Проверяем зависимые аддоны (кто зависит от этого)
        Set<String> dependents = findDependents(addonId);
        if (!dependents.isEmpty()) {
            String msg = "Addon " + addonId + " has dependents: " + dependents + ". Unloading anyway (may cause issues).";
            logger.warning(msg);
            LOGGER.warn("⚠️  " + msg);
            // Опционально: авто-выгрузить dependents сначала
            for (String depId : dependents) {
                unloadAddon(depId);
            }
        }

        try {
            addon.onDisable();
            LOGGER.info("🟡 Disabled addon: " + addonId);
            logger.info("Disabled addon: " + addonId);
        } catch (Throwable e) {
            String msg = "Error disabling " + addonId + ": " + e.getMessage();
            LOGGER.error("❌ " + msg);
            logger.log(Level.SEVERE, msg, e);
        }

        loadedAddons.remove(addonId);
        File jarFile = jarFiles.remove(addonId);
        if (jarFile != null) {
            URLClassLoader loader = classLoaders.remove(jarFile.getName());
            if (loader != null) {
                try {
                    loader.close();
                    logger.fine("Closed classloader for unloaded addon " + addonId);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to close classloader for " + addonId, e);
                }
            }
        }

        return true;
    }

    /**
     * Получает загруженный аддон по ID.
     *
     * @param addonId ID аддона.
     * @return Аддон или null, если не найден.
     */
    @Nullable @Override
    public TreexAddon getAddon(@NotNull String addonId) {
        return loadedAddons.get(addonId);
    }

    @Override
    public boolean isLoaded(@NotNull String addonId) {
        return loadedAddons.containsKey(addonId);
    }
    /**
     * Получает список всех ID загруженных аддонов.
     *
     * @return Неизменяемый список ID.
     */
    @NotNull
    public List<String> getAddonIds() {
        return Collections.unmodifiableList(new ArrayList<>(loadedAddons.keySet()));
    }

    // ===== Private Helpers =====

    /**
     * Сканирует классы в JAR с помощью JarFile stream.
     */
    private List<Class<?>> scanClassesInJar(@NotNull File jarFile, @NotNull URLClassLoader loader) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream()
                    .filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace('/', '.').replace(".class", ""))
                    .forEach(className -> {
                        try {
                            Class<?> c = loader.loadClass(className);
                            classes.add(c);
                            logger.fine("Loaded class: " + className);
                        } catch (Throwable t) {
                            logger.log(Level.FINE, "Failed to load class " + className + ": " + t.getMessage(), t);
                        }
                    });
        }
        return classes;
    }

    /**
     * Проверяет hard-зависимости (depends) для аддона.
     */
    private boolean checkDependencies(@NotNull TreexAddonInfo info) {
        boolean ok = true;
        for (Dependency dep : info.depends()) {
            TreexAddon found = getAddon(dep.id());
            if (found == null) {
                LOGGER.error("❌ Missing hard dependency for " + info.id() + ": " + dep.id() + " (required ≥ " + dep.version() + ")");
                logger.severe("Missing hard dep: " + dep.id() + " for " + info.id());
                ok = false;
                continue;
            }
            String actual = found.getInfo().version();
            if (!VersionUtil.isSatisfied(actual, dep.version())) {
                String msg = "Incompatible hard dep for " + info.id() + ": " + dep.id() + " (req ≥ " + dep.version() + ", found " + actual + ")";
                LOGGER.error("❌ " + msg);
                logger.severe(msg);
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Строит граф зависимостей и возвращает топологически отсортированный список.
     * Учитывает depends, softDepends, loadAfter, loadBefore.
     * Если цикл — логирует ошибку, но возвращает частичный порядок.
     */
    private List<TreexAddon> sortByDependencies() {
        Map<String, Set<String>> graph = buildDependencyGraph();
        logger.fine("Built dependency graph: " + graph);

        return topologicalSort(graph);
    }

    /**
     * Строит граф: ключ — ID, значение — зависимости (ребята, от которых зависит этот).
     */
    private Map<String, Set<String>> buildDependencyGraph() {
        Map<String, Set<String>> graph = new HashMap<>();
        for (TreexAddon a : loadedAddons.values()) {
            graph.put(a.getInfo().id(), new LinkedHashSet<>());
        }

        for (TreexAddon a : loadedAddons.values()) {
            TreexAddonInfo info = a.getInfo();
            String id = info.id();

            // Hard + soft depends
            for (Dependency d : info.depends()) if (graph.containsKey(d.id())) graph.get(id).add(d.id());
            for (Dependency d : info.softDepends()) if (graph.containsKey(d.id())) graph.get(id).add(d.id());

            // loadAfter: этот после того (зависит от того)
            for (String after : info.loadAfter()) if (graph.containsKey(after)) graph.get(id).add(after);

            // loadBefore: тот после этого (тот зависит от этого)
            for (String before : info.loadBefore()) if (graph.containsKey(before)) graph.get(before).add(id);
        }

        return graph;
    }

    /**
     * Топологическая сортировка (Kahn's algorithm). Обнаруживает циклы.
     */
    private List<TreexAddon> topologicalSort(Map<String, Set<String>> graph) {
        Map<String, Integer> indegree = new HashMap<>();
        graph.keySet().forEach(id -> indegree.put(id, 0));
        graph.values().forEach(deps -> deps.forEach(dep -> indegree.merge(dep, 1, Integer::sum)));

        Deque<String> queue = new ArrayDeque<>();
        indegree.forEach((id, deg) -> { if (deg == 0) queue.add(id); });

        List<TreexAddon> order = new ArrayList<>();
        int processed = 0;
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            TreexAddon addon = loadedAddons.get(id);
            if (addon != null) order.add(addon);

            for (String dep : graph.getOrDefault(id, Collections.emptySet())) {
                indegree.put(dep, indegree.get(dep) - 1);
                if (indegree.get(dep) == 0) queue.addLast(dep);
            }
            processed++;
        }

        if (processed < graph.size()) {
            // Цикл! Логируем
            Set<String> cycleNodes = indegree.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            String msg = "Dependency cycle detected involving: " + cycleNodes + ". Partial order used.";
            logger.warning(msg);
            LOGGER.warn("⚠️  " + msg);
        }

        LOGGER.error("Topological order: " + order.stream().map(a -> a.getInfo().id()).toList());
        logger.fine("Sort order: " + order.stream().map(a -> a.getInfo().id()).collect(Collectors.toList()));
        return order;
    }

    /**
     * Находит аддоны, зависящие от givenId (рекурсивно).
     */
    private Set<String> findDependents(@NotNull String givenId) {
        Set<String> dependents = new HashSet<>();
        for (TreexAddon a : loadedAddons.values()) {
            TreexAddonInfo info = a.getInfo();
            if (info.id().equals(givenId)) continue;

            // Проверяем depends/softDepends/loadBefore
            for (Dependency d : info.depends()) if (d.id().equals(givenId)) dependents.add(info.id());
            for (Dependency d : info.softDepends()) if (d.id().equals(givenId)) dependents.add(info.id());
            for (String after : info.loadAfter()) if (after.equals(givenId)) dependents.add(info.id());
        }
        return dependents;
    }
}