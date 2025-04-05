package io.group9;

import com.badlogic.gdx.Game;

import java.net.URISyntaxException;

public class Main extends Game {
    FirstScreen firstScreen;
    @Override
    public void create() {
        try {
            firstScreen = new FirstScreen();
            setScreen(firstScreen);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
