package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
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
    private File jarLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    private File pluginsDir = jarLocation.getParentFile();

    public FirstScreen() throws URISyntaxException {
        // Initialization in show().
    }

    @Override
    public void show() {
        Gdx.app.log("FirstScreen", "Initializing world, engine, and plugins...");
        // Create Box2D world (gravity in world units)
        world = new World(new Vector2(0, -10f), true);
        CoreResources.setWorld(world);

        // Create and set contact dispatcher.
        CoreContactDispatcher dispatcher = new CoreContactDispatcher();
        world.setContactListener(dispatcher);
        CoreResources.setContactDispatcher(dispatcher);

        engine = new Engine();
        loadPluginsFromMods();
        debugRenderer = new Box2DDebugRenderer();

        // Discover plugins via ServiceLoader.
        ServiceLoader<ECSPlugin> loader = ServiceLoader.load(ECSPlugin.class, Thread.currentThread().getContextClassLoader());
        List<ECSPlugin> plugins = new ArrayList<>();
        for (ECSPlugin plugin : loader) {
            plugins.add(plugin);
            Gdx.app.log("FirstScreen", "Found plugin: " + plugin.getClass().getName());
        }
        plugins.sort(Comparator.comparingInt(ECSPlugin::getPriority));
        for (ECSPlugin plugin : plugins) {
            plugin.registerSystems(engine);
            plugin.createEntities(engine);
            Gdx.app.log("FirstScreen", "Initialized plugin: " + plugin.getClass().getName());
        }
    }

    float accumulator = 0;
    float fixedTimeStep = 1/60f;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1); // RGBA
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        accumulator += delta;
        while (accumulator >= fixedTimeStep) {
            world.step(fixedTimeStep, 6, 2);
            accumulator -= fixedTimeStep;
        }
        engine.update(delta);
        debugRenderer.render(world, CoreResources.getCamera().combined);

    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        if (world != null) world.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }

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
                        Gdx.app.log("FirstScreen", "Loaded jar: " + jar.getName());
                    } catch (Exception e) {
                        Gdx.app.error("FirstScreen", "Error loading jar: " + jar.getName(), e);
                    }
                }
            }
            if (!jarUrls.isEmpty()) {
                URLClassLoader classLoader = new URLClassLoader(jarUrls.toArray(new URL[0]), getClass().getClassLoader());
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        } else {
            Gdx.app.error("FirstScreen", "Mods directory not found: " + directory.getAbsolutePath());
        }
    }
}

