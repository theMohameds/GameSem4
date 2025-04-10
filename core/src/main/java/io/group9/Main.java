package io.group9;

import com.badlogic.gdx.Game;

import java.net.URISyntaxException;

public class Main extends Game {
    FirstScreen firstScreen;

    @Override
    public void create() {
        try {
            firstScreen = new FirstScreen();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        setScreen(firstScreen);
    }
}
