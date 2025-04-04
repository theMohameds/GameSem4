package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;
import io.group9.plugins.ECSPlugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Main extends ApplicationAdapter {
    private World world;
    private Engine engine;
    private final File pluginsDir = new File(System.getProperty("user.dir"), "mods");

    @Override
    public void create() {
        engine = new Engine();
        loadPluginsFromMods(); // Load all JARs into a single class loader

        ServiceLoader<ECSPlugin> serviceLoader = loadPluginsFromClasspath();

        for (ECSPlugin plugin : serviceLoader) {
            plugin.registerSystems(engine);
            plugin.createEntities(engine);
            System.out.println("Loaded plugin: " + plugin.getClass().getName() + "\n");
        }
    }

    // Other methods (render, dispose) remain unchanged

    private void loadPluginsFromMods() {
        addJarsToClasspath(pluginsDir);
    }

    private void addJarsToClasspath(File directory) {
        List<URL> jarUrls = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] jars = directory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars != null) {
                for (File jar : jars) {
                    try {
                        URL url = jar.toURI().toURL();
                        jarUrls.add(url);
                        System.out.println("Added JAR: " + jar.getName() + "\n");
                    } catch (Exception e) {
                        System.err.println("Error loading JAR: " + jar.getName());
                        e.printStackTrace();
                    }
                }
            }
            if (!jarUrls.isEmpty()) {
                URLClassLoader classLoader = new URLClassLoader(
                    jarUrls.toArray(new URL[0]),
                    getClass().getClassLoader()
                );
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        } else {
            System.err.println("Directory not found: " + directory.getPath());
        }
    }

    private ServiceLoader<ECSPlugin> loadPluginsFromClasspath() {
        return ServiceLoader.load(ECSPlugin.class, Thread.currentThread().getContextClassLoader());
    }
}
