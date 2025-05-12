package io.group9;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class StartScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Texture background;
    private Window optionsWindow;
    private boolean isOptionsVisible = false;
    private SelectBox<String> fpsBox;
    private SelectBox<String> resolutionBox;

    private final String[] resolutionOptions = {
        "1024x576", "1280x720", "1600x900", "1920x1080"
    };

    private final int[][] resolutionValues  = {
        {1024, 576}, {1280, 720}, {1600, 900}, {1920, 1080}
    };

    public StartScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("startScreen.jpg"));
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        SettingsManager.loadSettings();

        int savedResIndex = SettingsManager.getResolutionIndex();
        int[] res = resolutionValues[savedResIndex];
        Gdx.graphics.setWindowedMode(res[0], res[1]);
        Gdx.graphics.setForegroundFPS(SettingsManager.getTargetFps());


        Skin skin = new Skin(Gdx.files.internal("uiskin.json")); // Sørg for at denne findes i assets

        float buttonWidth = 125f;
        float buttonHeight = 35f;

        Label titleLabel = new Label("Menu", skin);
        titleLabel.setFontScale(2f);

        TextButton startButton = new TextButton("Start Game", skin);
        TextButton optionsButton = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);
        TextButton mapButton = new TextButton("Choose map", skin);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.add(titleLabel).padBottom(20).center().row();
        table.add(startButton).width(buttonWidth).height(buttonHeight).pad(10).row();
        table.add(optionsButton).width(buttonWidth).height(buttonHeight).pad(10).row();
        table.add(mapButton).width(buttonWidth).height(buttonHeight).pad(10).row();
        table.add(exitButton).width(buttonWidth).height(buttonHeight).pad(10).row();

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y){
                try{
                    game.setScreen(new FirstScreen(game));
                } catch (Exception e) {
                    Gdx.app.error("StartScreen", "Error starting game", e);
                }
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        //Opret options window (overlay)
        optionsWindow = new Window("Settings", skin);
        optionsWindow.setSize(400, 400);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() - optionsWindow.getWidth() / 2,
            Gdx.graphics.getHeight() - optionsWindow.getHeight() / 2
        );
        optionsWindow.setVisible(false);
        optionsWindow.setMovable(false);
        stage.addActor(optionsWindow);

        Table contentTable = new Table();
        contentTable.top().pad(10);

        //FPS Dropdown
        contentTable.add(new Label("FPS:", skin)).left().pad(5);
        fpsBox = new SelectBox<>(skin);
        fpsBox.setItems("30", "60", "90");
        fpsBox.setSelected(String.valueOf(SettingsManager.getTargetFps()));
        contentTable.add(fpsBox).pad(5).row();

        fpsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int fps = Integer.parseInt(fpsBox.getSelected());
                Gdx.graphics.setForegroundFPS(fps);
                SettingsManager.setTargetFps(fps); // optional: store the value
            }
        });

        // Screen resolution placeholder
        contentTable.add(new Label("Resolution:", skin)).left().pad(5);
        resolutionBox = new SelectBox<>(skin);
        resolutionBox.setItems(resolutionOptions);
        resolutionBox.setSelected(resolutionOptions[savedResIndex]);
        contentTable.add(resolutionBox).pad(5).row();

        resolutionBox.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               int index = resolutionBox.getSelectedIndex();
               SettingsManager.setResolutionIndex(index);
           }
        });
// --- RGB Sliders ---
        contentTable.add(new Label("Player Color:", skin)).left().pad(5).colspan(2).row();

        CheckBox redCheckBox = new CheckBox("Red", skin);
        CheckBox greenCheckBox = new CheckBox("Green", skin);
        CheckBox yellowCheckBox = new CheckBox("Yellow", skin);
        Table colorTable = new Table();
        colorTable.add(redCheckBox).pad(5);
        colorTable.add(greenCheckBox).pad(5);
        colorTable.add(yellowCheckBox).pad(5);
        contentTable.add(colorTable).colspan(2).row();

        String savedColor = SettingsManager.getPlayerColorName();
        switch (savedColor.toUpperCase()) {
            case "RED":
                redCheckBox.setChecked(true);
                break;
            case "GREEN":
                greenCheckBox.setChecked(true);
                break;
            case "YELLOW":
                yellowCheckBox.setChecked(true);
                break;
                default:
                    break;
        }
        redCheckBox.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               if (redCheckBox.isChecked()) {
                   greenCheckBox.setChecked(false);
                   yellowCheckBox.setChecked(false);
               }
           }
        });
        greenCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (greenCheckBox.isChecked()) {
                    redCheckBox.setChecked(false);
                    yellowCheckBox.setChecked(false);
                }
            }
        });
        yellowCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (yellowCheckBox.isChecked()) {
                    redCheckBox.setChecked(false);
                    greenCheckBox.setChecked(false);
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        optionsWindow.clear();
        optionsWindow.add(scrollPane).expand().fill().row();


        // Gem button
        TextButton saveButton = new TextButton("Save", skin);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int selectedFps = Integer.parseInt(fpsBox.getSelected());
                int selectedResIndex = resolutionBox.getSelectedIndex();
                int[] selectedRes = resolutionValues[selectedResIndex];
                String selectedColor = "WHITE";

                SettingsManager.setTargetFps(selectedFps); // store it globally
                SettingsManager.setPlayerColorName(selectedColor);
                SettingsManager.setResolutionIndex(selectedResIndex);
                SettingsManager.saveSettings();

                Gdx.graphics.setForegroundFPS(selectedFps);
                Gdx.graphics.setWindowedMode(selectedRes[0], selectedRes[1]);

                optionsWindow.setVisible(false);
                isOptionsVisible = false;
            }
        });
// --- Close Button ---
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.setVisible(false);
                isOptionsVisible = false;
            }
        });
        optionsWindow.add(saveButton).colspan(2). pad(10).row();
        optionsWindow.add(closeButton).colspan(2).pad(10).row();

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isOptionsVisible = !isOptionsVisible;
                optionsWindow.setVisible(isOptionsVisible);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (optionsWindow != null) {
            optionsWindow.setPosition(
                (width - optionsWindow.getWidth()) / 2,
                (height - optionsWindow.getHeight()) / 2
            );
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }



    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        background.dispose();
    }
}
