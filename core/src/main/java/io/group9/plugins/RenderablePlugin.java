// File: RenderablePlugin.java
package io.group9.plugins;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface RenderablePlugin extends Plugin {  // Extends Plugin
    void render(SpriteBatch batch);
}
