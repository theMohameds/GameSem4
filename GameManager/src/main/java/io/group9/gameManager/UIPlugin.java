package io.group9.gameManager;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.group9.CoreResources;
import plugins.ECSPlugin;

public final class UIPlugin implements ECSPlugin {

    private Stage stage;
    private ProgressBar playerHP, enemyHP;
    private Label timerLbl, roundLbl, splash;
    private RoundManager rm;

    @Override public void registerSystems(Engine eng) {
        buildStage();
        rm = new RoundManager(0);
        eng.addSystem(rm);
        eng.addSystem(new HudSys(900));
        eng.addSystem(new StageSys(stage, 1000));
    }

    @Override public void createEntities(Engine e) { }
    @Override public int getPriority() { return 6; }

    private void buildStage() {
        stage = new Stage(new ScreenViewport());
        int W = Gdx.graphics.getWidth();
        int H = Gdx.graphics.getHeight();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Oldtimer.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter big = new FreeTypeFontGenerator.FreeTypeFontParameter();
        big.size        = 30;
        big.borderWidth = 2;
        big.borderColor = Color.BLACK;
        BitmapFont font14 = gen.generateFont(big);

        FreeTypeFontGenerator.FreeTypeFontParameter sm = new FreeTypeFontGenerator.FreeTypeFontParameter();
        sm.size        = 15;
        sm.borderWidth = 1;
        sm.borderColor = Color.BLACK;
        BitmapFont font12 = gen.generateFont(sm);

        gen.dispose();

        Pixmap pm = new Pixmap(1, 1, Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        TextureRegionDrawable white = new TextureRegionDrawable(
            new TextureRegion(new Texture(pm))
        );
        pm.dispose();

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = white.tint(Color.DARK_GRAY);
        barStyle.knobBefore = white.tint(Color.RED);

        Skin skin = new Skin();
        skin.add("round-font", font14);
        skin.add("timer-font", font12);
        skin.add("round",  new Label.LabelStyle(font14, Color.WHITE));
        skin.add("timer",  new Label.LabelStyle(font12, Color.WHITE));
        skin.add("splash", new Label.LabelStyle(font14, Color.WHITE));
        skin.add("default-horizontal", barStyle);

        int barY = H - 30;

        // Health bars
        playerHP = new ProgressBar(0, 100, 1, false, skin);
        playerHP.setSize(200, 20);
        playerHP.setPosition(10, barY);
        stage.addActor(playerHP);

        enemyHP = new ProgressBar(0, 100, 1, false, skin);
        enemyHP.setSize(200, 20);
        enemyHP.setPosition(W - 210, barY);
        stage.addActor(enemyHP);

        // Timer label
        timerLbl = new Label("60", skin, "timer");
        timerLbl.setAlignment(Align.center);
        timerLbl.pack();
        timerLbl.setPosition((W - timerLbl.getWidth()) / 2, barY - 33);
        stage.addActor(timerLbl);

        // Round counter
        roundLbl = new Label("ROUND 1", skin, "round");
        roundLbl.setAlignment(Align.center);
        roundLbl.pack();
        roundLbl.setPosition((W - roundLbl.getWidth()) / 2, barY - 8);
        stage.addActor(roundLbl);

        // READY/3-2-1/FIGHT
        splash = new Label("", skin, "splash");
        splash.setAlignment(Align.center);
        splash.setFillParent(true);
        splash.setVisible(false);
        stage.addActor(splash);
    }

    private class HudSys extends com.badlogic.ashley.core.EntitySystem {
        HudSys(int p){ super(p); }
        @Override public void update(float dt) {
            playerHP.setValue(CoreResources.getPlayerHealth());
            enemyHP .setValue(CoreResources.getEnemyHealth());

            timerLbl.setText(String.format("%02d", (int)Math.ceil(rm.getRoundTimer())));
            roundLbl.setText("ROUND " + rm.getRoundNumber());

            String cue = rm.getIntroCue();
            if (cue == null) {
                splash.setVisible(false);
            } else {
                if (!splash.isVisible()) {
                    splash.clearActions();
                    splash.getColor().a = 1f;
                    splash.setVisible(true);
                }
                splash.setText(cue);
                if ("FIGHT".equals(cue) && splash.getActions().size == 0) {
                    splash.addAction(Actions.sequence(
                        Actions.fadeOut(1.5f),
                        Actions.visible(false)
                    ));
                }
            }
        }
    }

    private static class StageSys extends com.badlogic.ashley.core.EntitySystem {
        private final Stage st;
        StageSys(Stage s, int p){ super(p); st = s; }
        @Override public void update(float dt) {
            st.act(dt);
            st.draw();
        }
    }
}
