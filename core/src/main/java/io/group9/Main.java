package io.group9;

import com.badlogic.gdx.Game;

public class Main extends Game {
    FirstScreen firstScreen;
    @Override
    public void create() {
        firstScreen = new FirstScreen();
        setScreen(firstScreen);
    }

}
