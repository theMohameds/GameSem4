package io.group9.gameManager;

import com.badlogic.ashley.core.EntitySystem;
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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.group9.CoreResources;
import services.IInventoryService;
import services.IWeapon;
import plugins.ECSPlugin;

import java.util.List;
import java.util.Optional;

public final class UIPlugin implements ECSPlugin {
    private Stage stage;
    private ProgressBar playerHP, enemyHP;
    private Label timerLbl, roundLbl, splash;
    private Label slot1Lbl, slot2Lbl;
    private RoundManager rm;

    @Override
    public void registerSystems(Engine eng) {
        buildStage();
        rm = new RoundManager(0);
        eng.addSystem(rm);
        eng.addSystem(new HudSys(900));
        eng.addSystem(new StageSys(stage, 1000));
    }

    @Override
    public void createEntities(Engine e) {
        rm.reset();
        //rm.startNextRound();
        rm.freezeAllBodies();
    }
    @Override public int getPriority() { return 6; }

    private void buildStage() {
        stage = new Stage(new ScreenViewport());
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/Oldtimer.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter big =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        big.size = 30; big.borderWidth = 2; big.borderColor = Color.BLACK;
        BitmapFont font14 = gen.generateFont(big);

        FreeTypeFontGenerator.FreeTypeFontParameter sm =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        sm.size = 15; sm.borderWidth = 1; sm.borderColor = Color.BLACK;
        BitmapFont font12 = gen.generateFont(sm);
        gen.dispose();

        Pixmap pm = new Pixmap(1, 8, Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
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

        int W = Gdx.graphics.getWidth(), H = Gdx.graphics.getHeight();
        int barY = H - 30;

        playerHP = new ProgressBar(0,100,1,false, skin);
        playerHP.setSize(200,20);
        playerHP.setPosition(10, barY);
        stage.addActor(playerHP);

        enemyHP = new ProgressBar(0,100,1,false, skin);
        enemyHP.setSize(200,20);
        enemyHP.setPosition(W - 210, barY);
        stage.addActor(enemyHP);

        timerLbl = new Label("60", skin, "timer");
        timerLbl.setPosition((W - timerLbl.getWidth())/2, barY - 33);
        timerLbl.setAlignment(Align.center);
        timerLbl.pack();
        stage.addActor(timerLbl);

        roundLbl = new Label("ROUND 1", skin, "round");
        roundLbl.setPosition((W - roundLbl.getWidth())/2, barY - 8);
        roundLbl.setAlignment(Align.center);
        roundLbl.pack();
        stage.addActor(roundLbl);

        splash = new Label("", skin, "splash");
        splash.setAlignment(Align.center);
        splash.setFillParent(true);
        splash.setVisible(false);
        stage.addActor(splash);

        // INVENTORY SLOTS
        Color semiGray = Color.LIGHT_GRAY.cpy(); semiGray.a = 0.5f;
        Label.LabelStyle slotStyle = new Label.LabelStyle(font12, semiGray);

        slot1Lbl = new Label("1", slotStyle); slot1Lbl.setAlignment(Align.center);
        slot2Lbl = new Label("2", slotStyle); slot2Lbl.setAlignment(Align.center);

        Pixmap border = new Pixmap(34,34, Format.RGBA8888);
        border.setColor(Color.WHITE);
        border.drawRectangle(0,0,34,34);
        Texture borderTex = new Texture(border);
        border.dispose();
        TextureRegionDrawable bd = new TextureRegionDrawable(new TextureRegion(borderTex));

        Container<Label> c1 = new Container<>(slot1Lbl);
        c1.setBackground(bd); c1.setSize(34,34);

        Container<Label> c2 = new Container<>(slot2Lbl);
        c2.setBackground(bd); c2.setSize(34,34);

        Table invTable = new Table();
        invTable.setFillParent(true);
        invTable.bottom().padBottom(10);
        invTable.add(c1).padRight(10);
        invTable.add(c2).padLeft(10);
        stage.addActor(invTable);
    }

    private class HudSys extends EntitySystem {
        HudSys(int p){ super(p); }
        @Override public void update(float dt) {
            // basic HUD
            playerHP.setValue(CoreResources.getPlayerHealth());
            enemyHP .setValue(CoreResources.getEnemyHealth());
            timerLbl.setText(String.format("%02d", (int)Math.ceil(rm.getRoundTimer())));
            roundLbl.setText("ROUND " + rm.getRoundNumber());
            String cue = rm.getIntroCue();
            if (cue != null) {
                if (!splash.isVisible()) {
                    splash.clearActions();
                    splash.getColor().a = 1f;
                    splash.setVisible(true);
                }
                splash.setText(cue);
                if ("FIGHT".equals(cue) && splash.getActions().size==0) {
                    splash.addAction(Actions.sequence(
                        Actions.fadeOut(1.5f),
                        Actions.visible(false)
                    ));
                }
            } else {
                splash.setVisible(false);
            }

            IInventoryService inv = CoreResources.getInventoryService();
            com.badlogic.ashley.core.Entity player = CoreResources.getPlayerEntity();
            if (inv != null && player != null) {
                List<IWeapon> invList = inv.getInventory(player);
                Optional<IWeapon> cw = inv.getCurrentWeapon(player);
                int selected = cw
                    .flatMap(w -> {
                        int idx = invList.indexOf(w);
                        return idx>=0?Optional.of(idx):Optional.empty();
                    })
                    .orElse(-1);

                slot1Lbl.setColor(selected==0?Color.YELLOW:Color.LIGHT_GRAY);
                slot2Lbl.setColor(selected==1?Color.YELLOW:Color.LIGHT_GRAY);
            }
        }
    }

    private static class StageSys extends EntitySystem {
        private final Stage st;
        StageSys(Stage s,int p){ super(p); st=s; }
        @Override public void update(float dt){
            st.act(dt);
            st.draw();
        }
    }
}
