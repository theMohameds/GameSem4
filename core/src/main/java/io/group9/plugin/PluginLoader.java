package io.group9.plugin;

import io.group9.plugins.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class PluginLoader {
    private final List<Plugin> loadedPlugins = new ArrayList<>();
    private final File rootDir = new File(System.getProperty("user.dir")).getParentFile();
    private final File pluginsDir = new File(System.getProperty("user.dir"), "mods");
    private final File pluginsDir2 = new File(rootDir, "mods"); // Removed leading slash

    public void loadPlugins() {
        //System.out.println("Current working directory: " + System.getProperty("user.dir"));
        checkAndLoadFromDirectory(pluginsDir); // Check first mods directory
        checkAndLoadFromDirectory(pluginsDir2); // Check second mods directory
    }

    private void checkAndLoadFromDirectory(File directory) {
        //System.out.println("Checking plugin directory: " + directory.getAbsolutePath());

        if (!directory.exists() || !directory.isDirectory()) {
            //System.out.println("Directory does not exist: " + directory.getAbsolutePath());
            return;
        }

        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            //System.out.println("No JAR files found in: " + directory.getAbsolutePath());
            return;
        }

        for (File jarFile : jarFiles) {
            loadJar(jarFile);
        }
    }

    private void loadJar(File jarFile) {
        //System.out.println("Found plugin JAR: " + jarFile.getName());
        try {
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());

            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
            boolean foundPlugin = false;

            for (Plugin plugin : serviceLoader) {
                //System.out.println("Loaded plugin: " + plugin.getClass().getName());
                plugin.onLoad();
                loadedPlugins.add(plugin);
                foundPlugin = true;
            }

            if (!foundPlugin) {
                //System.out.println("No valid Plugin implementations found in: " + jarFile.getName());
            }

        } catch (Exception e) {
            //System.err.println("Failed to load plugin from " + jarFile.getName());
            e.printStackTrace();
        }
    }

    // Rest of the class remains unchanged
    public void unloadPlugins() {
        for (Plugin plugin : loadedPlugins) {
            System.out.println("Unloading plugin: " + plugin.getClass().getName());
            plugin.onUnload();
        }
        loadedPlugins.clear();
    }

    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}
