package io.group9;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import io.group9.plugin.PluginLoader;
import io.group9.plugins.Plugin;
import io.group9.plugins.RenderablePlugin;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private PluginLoader pluginLoader;

    @Override
    public void create() {
        batch = new SpriteBatch();
        //image = new Texture("libgdx.png");

        // Initialize and load plugins
        pluginLoader = new PluginLoader();
        pluginLoader.loadPlugins();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        // Draw the base image
        //batch.draw(image, 140, 210);
        // Render each plugin that implements RenderablePlugin
        for (Plugin plugin : pluginLoader.getLoadedPlugins()) {
            if (plugin instanceof RenderablePlugin) {
                ((RenderablePlugin) plugin).render(batch);
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        image.dispose();
        batch.dispose();
        pluginLoader.unloadPlugins();
    }
}
