package org.example.s.client;


public class ConfigHandler {
    public static JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();

    public static void saveConfig() {
        JSONConfigHandler.saveConfig(config);
    }
}