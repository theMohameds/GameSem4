package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import plugins.ECSPlugin;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class FirstScreen implements Screen {
    private World world;
    private Engine engine;
    private Box2DDebugRenderer debugRenderer;
    File jarLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    File pluginsDir = jarLocation.getParentFile();

    @Override
    public void show() {
        world = new World(new Vector2(0, -10), true);
        CoreResources.setWorld(world);
        engine = new Engine();
        loadPluginsFromMods(); // Load all JARs into a single class loader
        debugRenderer = new Box2DDebugRenderer();

        ServiceLoader<ECSPlugin> serviceLoader = loadPluginsFromClasspath();
        List<ECSPlugin> plugins = new ArrayList<>();
        for (ECSPlugin plugin : serviceLoader) {
            plugins.add(plugin);
        }

        plugins.sort(Comparator.comparingInt(ECSPlugin::getPriority));

        for (ECSPlugin plugin : plugins) {
            plugin.registerSystems(engine);
            plugin.createEntities(engine);
            System.out.println("Loaded plugin: " + plugin.getClass().getName() + "\n");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(1/60f * delta, 6, 2);
        engine.update(delta);
        debugRenderer.render(world, CoreResources.getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        // Adjust your camera's viewport if needed
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        // Clean up resources
        if (world != null) {
            world.dispose();
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
    }

    public FirstScreen() throws URISyntaxException {}
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
