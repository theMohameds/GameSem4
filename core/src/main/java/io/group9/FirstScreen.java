package io.group9;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import jdk.javadoc.internal.tool.Start;
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


    // Pause knappen
    private Stage stage;
    private Skin skin;
    private Window pauseWindow;
    private boolean isPaused = false;
    private Main game;
    private Texture pauseSnapshot = null;
    private Pixmap pausePixmap = null;
    private BitmapFont fpsFont;
    private Label fpsLabel;

    public FirstScreen(Main game) throws URISyntaxException {
        this.game = game;
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


        //pause knappen
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        isPaused = false;

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        //FPS label setup
        fpsFont = new BitmapFont();
        Label.LabelStyle fpsStyle = new Label.LabelStyle(fpsFont, com.badlogic.gdx.graphics.Color.WHITE);
        fpsLabel = new Label("FPS: 0", fpsStyle);
        fpsLabel.setFontScale(1.1f);
        fpsLabel.setPosition(10,10);
        stage.addActor(fpsLabel);

        TextButton pasueButton = new TextButton("Pasue", skin);
        pasueButton.setPosition(10, Gdx.graphics.getHeight() - 50);
        pasueButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
                BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
                if (pausePixmap != null) pausePixmap.dispose();
                if (pauseSnapshot != null) pauseSnapshot.dispose();
                pausePixmap = pixmap;
                pauseSnapshot = new Texture(pixmap);

                isPaused = true;
                pauseWindow.setVisible(true);
            }
        });
        stage.addActor(pasueButton);

        //Pause overlay
        pauseWindow = new Window("", skin);
        pauseWindow.setSize(300, 200);
        pauseWindow.setPosition(
            (Gdx.graphics.getWidth() - pauseWindow.getWidth())/2,
            (Gdx.graphics.getHeight() - pauseWindow.getHeight())/2
        );
        pauseWindow.setMovable(false);
        pauseWindow.setVisible(false);

        TextButton resumeButton = new TextButton("Continue", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        resumeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                pauseWindow.setVisible(false);
                isPaused = false;
            }
        });

        //Exit knp
        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new StartScreen(game));
            }
        });

        pauseWindow.add(resumeButton).width(125f).height(35f).pad(10).row();
        pauseWindow.add(exitButton).width(125f).height(35f).pad(10).row();

        stage.addActor(pauseWindow);
    }

    float accumulator = 0;
    float fixedTimeStep = 1/60f;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1); // RGBA
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused) {
            accumulator += delta;
            while (accumulator >= fixedTimeStep) {
                world.step(fixedTimeStep, 6, 2);
                accumulator -= fixedTimeStep;
            }
            engine.update(delta);
        }
        stage.act(Math.min(delta, 1/30f));

        if (isPaused && pauseSnapshot != null) {
            stage.getBatch().begin();
            stage.getBatch().draw(pauseSnapshot, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            stage.getBatch().end();
        } else {
            debugRenderer.render(world, CoreResources.getCamera().combined);
        }

        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        stage.draw();

    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        pauseWindow.setPosition((width - pauseWindow.getWidth())/2, (height - pauseWindow.getHeight())/2);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        if (world != null) world.dispose();
        if (debugRenderer != null) debugRenderer.dispose();

        //pasueknp
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();

        if (pausePixmap != null) pausePixmap.dispose();
        if (pauseSnapshot != null) pauseSnapshot.dispose();

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

