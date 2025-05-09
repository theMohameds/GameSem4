package io.group9.gameManager;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.group9.CoreResources;
import locators.EnemyServiceLocator;
import locators.InventoryServiceLocator;
import services.enemy.IEnemyService;
import services.player.IPlayerService;
import locators.PlayerServiceLocator;
import services.weapon.IInventoryService;
import services.weapon.IWeapon;
import plugins.ECSPlugin;

import java.util.List;
import java.util.Optional;

public final class UIPlugin implements ECSPlugin {
    private Stage stage;
    private ProgressBar playerHP, enemyHP;
    private Label timerLbl, roundLbl, splash;
    private RoundManager rm;
    private Container<Actor> slot1Container, slot2Container;
    private Label slot1Lbl, slot2Lbl;
    private Image slot1Img, slot2Img;
    private Drawable borderDefault, borderSelected;

    IInventoryService inv = InventoryServiceLocator.getInventoryService();
    private final IPlayerService playerSvc = PlayerServiceLocator.get();
    private final IEnemyService enemySvc = EnemyServiceLocator.get();

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
        rm.freezeAllBodies();
    }
    @Override public int getPriority() { return 6; }

    private void buildStage() {
        stage = new Stage(new ScreenViewport());

        // fonts & skin setup
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/Oldtimer.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter big = new FreeTypeFontGenerator.FreeTypeFontParameter();
        big.size = 30; big.borderWidth = 2; big.borderColor = Color.BLACK;
        BitmapFont font14 = gen.generateFont(big);

        FreeTypeFontGenerator.FreeTypeFontParameter sm = new FreeTypeFontGenerator.FreeTypeFontParameter();
        sm.size = 15; sm.borderWidth = 1; sm.borderColor = Color.BLACK;
        BitmapFont font12 = gen.generateFont(sm);
        gen.dispose();

        Pixmap pm = new Pixmap(1, 8, Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        TextureRegionDrawable white = new TextureRegionDrawable(new TextureRegion(new Texture(pm)));
        pm.dispose();

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = white.tint(Color.DARK_GRAY);
        barStyle.knobBefore  = white.tint(Color.RED);

        Skin skin = new Skin();
        skin.add("round-font", font14);
        skin.add("timer-font", font12);
        skin.add("round",  new Label.LabelStyle(font14, Color.WHITE));
        skin.add("timer",  new Label.LabelStyle(font12, Color.WHITE));
        skin.add("splash", new Label.LabelStyle(font14, Color.WHITE));
        skin.add("default-horizontal", barStyle);

        // health bars, timer & round labels
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
        timerLbl.setAlignment(Align.center);
        timerLbl.setPosition((W - timerLbl.getWidth())/2, barY - 33);
        timerLbl.pack();
        stage.addActor(timerLbl);

        roundLbl = new Label("ROUND 1", skin, "round");
        roundLbl.setAlignment(Align.center);
        roundLbl.setPosition((W - roundLbl.getWidth())/2, barY - 8);
        roundLbl.pack();
        stage.addActor(roundLbl);

        splash = new Label("", skin, "splash");
        splash.setAlignment(Align.center);
        splash.setFillParent(true);
        splash.setVisible(false);
        stage.addActor(splash);

        // INVENTORY
        Color semiGray = Color.LIGHT_GRAY.cpy(); semiGray.a = 0.5f;
        Label.LabelStyle emptySlotStyle = new Label.LabelStyle(font12, semiGray);

        slot1Lbl = new Label("1", emptySlotStyle);
        slot1Lbl.setAlignment(Align.center);
        slot2Lbl = new Label("2", emptySlotStyle);
        slot2Lbl.setAlignment(Align.center);

        slot1Img = new Image();
        slot1Img.setScaling(Scaling.fit);
        slot2Img = new Image();
        slot2Img.setScaling(Scaling.fit);

        Pixmap borderPixmap = new Pixmap(34,34, Format.RGBA8888);
        borderPixmap.setColor(Color.WHITE);
        borderPixmap.drawRectangle(0,0,34,34);
        TextureRegionDrawable slotBorder = new TextureRegionDrawable(new TextureRegion(new Texture(borderPixmap)));
        borderPixmap.dispose();

        borderDefault   = slotBorder;
        borderSelected  = slotBorder.tint(Color.YELLOW);

        slot1Container = new Container<Actor>(slot1Lbl);
        slot1Container.setBackground(borderDefault);
        slot1Container.setSize(34,34);

        slot2Container = new Container<Actor>(slot2Lbl);
        slot2Container.setBackground(borderDefault);
        slot2Container.setSize(34,34);

        Table invTable = new Table();
        invTable.setFillParent(true);
        invTable.bottom().padBottom(10);
        invTable.add(slot1Container).padRight(10);
        invTable.add(slot2Container).padLeft(10);
        stage.addActor(invTable);
    }

    private class HudSys extends EntitySystem {
        HudSys(int priority) { super(priority); }

        @Override public void update(float dt) {
            playerHP.setValue(playerSvc.getHealth());
            enemyHP.setValue(enemySvc.getHealth());
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
                if ("FIGHT".equals(cue) && splash.getActions().size == 0) {
                    splash.addAction(Actions.sequence(
                        Actions.fadeOut(1.5f),
                        Actions.visible(false)
                    ));
                }
            } else {
                splash.setVisible(false);
            }

            Entity player = playerSvc.getPlayerEntity();
            if (inv != null && player != null) {
                List<IWeapon> invList = inv.getInventory(player);
                Optional<IWeapon> cw = inv.getCurrentWeapon(player);
                int selected = cw
                    .flatMap(w -> {
                        int idx = invList.indexOf(w);
                        return idx >= 0 ? Optional.of(idx) : Optional.empty();
                    })
                    .orElse(-1);

                updateSlot(slot1Container, slot1Lbl, slot1Img, invList, 0, selected == 0);
                updateSlot(slot2Container, slot2Lbl, slot2Img, invList, 1, selected == 1);
            }
        }

        private void updateSlot(
            Container<Actor> container,
            Label label,
            Image image,
            List<IWeapon> invList,
            int index,
            boolean isSelected
        ) {
            if (index < invList.size()) {
                TextureRegion sprite = invList.get(index).getSprite();
                image.setDrawable(new TextureRegionDrawable(sprite));
                container.setActor(image);
            } else {
                container.setActor(label);
            }
            container.setBackground(isSelected ? borderSelected : borderDefault);
        }
    }

    private static class StageSys extends EntitySystem {
        private final Stage stage;
        StageSys(Stage stage, int priority) {
            super(priority);
            this.stage = stage;
        }
        @Override public void update(float dt) {
            stage.act(dt);
            stage.draw();
        }
    }
}
