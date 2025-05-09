package io.group9;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;

public class SettingsManager {
    private static int targetFps = 30;
    private static int resolutionIndex = 3;
    private static String playerColorName= "WHITE";

    public static void setTargetFps(int fps) {
        targetFps = fps;
        Gdx.graphics.setForegroundFPS(fps);
    }
    public static int getTargetFps() {
        return targetFps;
    }

    public static void setResolutionIndex(int index) {
        resolutionIndex = index;
    }

    public static int getResolutionIndex() {
        return resolutionIndex;
    }

    public static void setPlayerColorName(String colorName) {
        playerColorName = colorName;
    }

    public static String getPlayerColorName(){
        return playerColorName;
    }

    public static Color getPlayerColor(){
       Preferences prefs = Gdx.app.getPreferences("Game Settings");
       String colorName = prefs.getString("PlayerColor", "WHITE");

        switch (playerColorName.toUpperCase()){
           case "RED": return Color.RED;
           case "YELLOW": return Color.YELLOW;
           case "GREEN": return Color.GREEN;
       default: return Color.WHITE;
       }
    }

    public static void loadSettings(){
        Preferences prefs = Gdx.app.getPreferences("Game Settings");
        targetFps = prefs.getInteger("Fps", 30);
        playerColorName = prefs.getString("PlayerColor", "WHITE");
        resolutionIndex = prefs.getInteger("Resolution Index", 3);
        Gdx.graphics.setForegroundFPS(targetFps);
    }

    public static void saveSettings(){
        Preferences prefs = Gdx.app.getPreferences("Game Settings");
        prefs.putInteger("Fps", targetFps);
        prefs.putString("PlayerColor", playerColorName);
        prefs.putInteger("Resolution Index", resolutionIndex);
        prefs.flush();
    }

}
