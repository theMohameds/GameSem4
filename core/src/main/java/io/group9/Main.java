package io.group9;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }
}
