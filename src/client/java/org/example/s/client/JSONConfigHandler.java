package org.example.s.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONConfigHandler {
    private static final File CONFIG_FILE = new File(MinecraftClient.getInstance().runDirectory, "config/autologin.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class ConfigData {
        public Map<String, PlayerConfig> players = new HashMap<>(); // Ключ - ник игрока
    }

    public static class PlayerConfig {
        public String triggers = "login";
        public boolean check_enabled = true;
        public boolean currentCheck = true;
        public Map<String, String> passwords = new HashMap<>();
    }

    public static ConfigData loadConfig() {
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.getParentFile().mkdirs();
                CONFIG_FILE.createNewFile();
                saveConfig(new ConfigData());
            } catch (IOException e) {
                return new ConfigData();
            }
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return GSON.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            return new ConfigData();
        }
    }

    public static void saveConfig(ConfigData config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerConfig getCurrentPlayerConfig() {
        ConfigData config = loadConfig();
        String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
        
        return config.players.getOrDefault(currentUsername, new PlayerConfig());
    }

    public static void saveCurrentPlayerConfig(PlayerConfig playerConfig) {
        ConfigData config = loadConfig();
        String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
        config.players.put(currentUsername, playerConfig);
        saveConfig(config);
    }
}