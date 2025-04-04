package io.group9.gamemap;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.group9.plugins.RenderablePlugin;

public class GamemapPlugin implements RenderablePlugin {

    private BitmapFont font;

    @Override
    public void onLoad() {
        System.out.println("Gamemap plugin loaded!");
        // Initialize resources for rendering
        font = new BitmapFont(); // Using the default font
    }

    @Override
    public void render(SpriteBatch batch) {
        // Draw a simple message on screen
        font.draw(batch, "Gamemap Plugin Active!", 10, 400);
    }

    @Override
    public void onUnload() {
        System.out.println("Gamemap plugin unloaded!");
        if (font != null) {
            font.dispose();
        }
    }
}
