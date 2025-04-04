package io.group9.gamecamera;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.group9.plugins.RenderablePlugin;

public class GamecameraPlugin implements RenderablePlugin {

    private BitmapFont font;

    @Override
    public void onLoad() {
        System.out.println("Gamecamera plugin loaded!");
        // Initialize resources for rendering
        font = new BitmapFont(); // Using the default font
    }

    @Override
    public void render(SpriteBatch batch) {
        // Draw a simple message on screen
        font.draw(batch, "Gamecamera Plugin Active!", 300, 400);
    }

    @Override
    public void onUnload() {
        System.out.println("Gamecamera plugin unloaded!");
        if (font != null) {
            font.dispose();
        }
    }
}
