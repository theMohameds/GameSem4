package io.group9.plugins;

public interface Plugin {
    void onLoad(); // Called when the plugin is loaded
    void onUnload(); // Called when the plugin is removed
}
